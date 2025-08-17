package com.lifee.config.app.adapters.persistence

import com.lifee.common.eventsourcing.ConcurrencyException
import com.lifee.common.eventsourcing.EventStore
import com.lifee.common.eventsourcing.SnapshotService
import com.lifee.config.domain.aggregates.Configuration
import com.lifee.config.domain.repositories.ConfigurationRepository
import com.lifee.config.domain.valueobjects.ConfigId
import com.lifee.config.domain.valueobjects.Environment
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * Spring Data JPA 配置仓储接口
 */
interface SpringDataConfigurationRepository : JpaRepository<ConfigurationJpaEntity, String> {
    
    /**
     * 根据命名空间查找配置
     */
    fun findByNamespace(namespace: String): List<ConfigurationJpaEntity>
    
    /**
     * 根据环境查找配置
     */
    fun findByEnvironment(environment: String): List<ConfigurationJpaEntity>
    
    /**
     * 根据命名空间和环境查找配置
     */
    fun findByNamespaceAndEnvironment(namespace: String, environment: String): ConfigurationJpaEntity?
    
    /**
     * 检查配置是否存在
     */
    fun existsByNamespaceAndEnvironment(namespace: String, environment: String): Boolean
    
    /**
     * 统计配置数量
     */
    fun countByNamespace(namespace: String): Long
    
    fun countByEnvironment(environment: String): Long
    
    /**
     * 获取所有命名空间
     */
    @Query("SELECT DISTINCT c.namespace FROM ConfigurationJpaEntity c ORDER BY c.namespace")
    fun findAllNamespaces(): List<String>
    
    /**
     * 获取所有环境
     */
    @Query("SELECT DISTINCT c.environment FROM ConfigurationJpaEntity c ORDER BY c.environment")
    fun findAllEnvironments(): List<String>
    
    /**
     * 根据命名空间前缀查找配置
     */
    fun findByNamespaceStartingWith(namespacePrefix: String): List<ConfigurationJpaEntity>
    
    /**
     * 查找最近更新的配置
     */
    @Query(
        "SELECT c FROM ConfigurationJpaEntity c " +
        "WHERE c.updatedAt >= :since " +
        "ORDER BY c.updatedAt DESC"
    )
    fun findRecentlyUpdated(@Param("since") since: java.time.LocalDateTime): List<ConfigurationJpaEntity>
}

/**
 * 配置仓储JPA实现
 */
@Repository
class ConfigurationJpaRepositoryImpl(
    private val springDataRepository: SpringDataConfigurationRepository,
    private val eventStore: EventStore,
    private val snapshotService: SnapshotService
) : ConfigurationRepository {
    
    private val logger = LoggerFactory.getLogger(ConfigurationJpaRepositoryImpl::class.java)
    private val maxRetryAttempts = 3
    
    @Transactional
    override fun save(configuration: Configuration): Configuration {
        return saveWithRetry(configuration, 0)
    }
    
    /**
     * 带重试机制的保存方法
     */
    private fun saveWithRetry(configuration: Configuration, attemptCount: Int): Configuration {
        try {
            // 保存聚合根状态
            val entity = ConfigurationJpaEntity.fromDomain(configuration)
            val savedEntity = springDataRepository.save(entity)
            
            // 保存事件到事件存储
            if (configuration.hasUncommittedEvents()) {
                val events = configuration.getUncommittedEvents()
                eventStore.saveEvents(
                    configuration.getId().toString(),
                    events,
                    configuration.getVersion() - events.size
                )
                configuration.markEventsAsCommitted()
                
                // 检查是否需要创建快照
                try {
                    snapshotService.createSnapshotIfNeeded(configuration)
                } catch (e: Exception) {
                    logger.warn("Failed to create snapshot for configuration {}: {}", 
                        configuration.getId(), e.message)
                    // 快照创建失败不影响主流程
                }
            }
            
            return savedEntity.toDomain()
            
        } catch (e: ConcurrencyException) {
            logger.warn("Configuration save conflict (attempt {}): aggregateId={}, expectedVersion={}, actualVersion={}", 
                attemptCount + 1, e.aggregateId, e.expectedVersion, e.actualVersion)
            
            if (attemptCount >= maxRetryAttempts - 1) {
                logger.error("Configuration save failed after {} attempts: {}", maxRetryAttempts, e.message)
                throw e
            }
            
            // 重新加载最新版本的聚合根
            val latestConfiguration = findById(configuration.getId())
                ?: throw IllegalStateException("Configuration not found during retry: ${configuration.getId()}")
            
            // 重新应用业务逻辑（这里需要调用方重新执行业务操作）
            logger.info("Retrying configuration save (attempt {}): {}", attemptCount + 1, configuration.getId())
            
            // 延迟重试
            Thread.sleep((attemptCount + 1) * 100L)
            
            // 递归重试
            return saveWithRetry(latestConfiguration, attemptCount + 1)
            
        } catch (e: Exception) {
            logger.error("Unexpected error saving configuration: {}", configuration.getId(), e)
            throw e
        }
    }
    
    override fun findById(id: ConfigId): Configuration? {
        return try {
            // 首先尝试从快照恢复
            val snapshot = eventStore.getLatestSnapshot(id.toString())
            if (snapshot != null) {
                val configuration = Configuration.create(
                    id = id,
                    namespace = "", // 临时值，将从快照数据中恢复
                    environment = Environment.DEVELOPMENT // 临时值，将从快照数据中恢复
                )
                configuration.restoreFromSnapshot(snapshot)
                
                // 应用快照之后的事件
                val eventsAfterSnapshot = eventStore.getEventsAfterVersion(
                    id.toString(), 
                    snapshot.version
                )
                eventsAfterSnapshot.forEach { event ->
                    configuration.applyEvent(event)
                }
                
                logger.debug("Configuration {} restored from snapshot at version {}", 
                    id, snapshot.version)
                configuration
            } else {
                // 如果没有快照，从JPA加载
                springDataRepository.findById(id.toString())
                    .map { it.toDomain() }
                    .orElse(null)
            }
        } catch (e: Exception) {
            logger.warn("Failed to restore configuration {} from snapshot, falling back to JPA: {}", 
                id, e.message)
            // 快照恢复失败时回退到JPA
            springDataRepository.findById(id.toString())
                .map { it.toDomain() }
                .orElse(null)
        }
    }
    
    override fun findByNamespace(namespace: String): List<Configuration> {
        return springDataRepository.findByNamespace(namespace)
            .map { it.toDomain() }
    }
    
    override fun findByEnvironment(environment: Environment): List<Configuration> {
        return springDataRepository.findByEnvironment(environment.toString())
            .map { it.toDomain() }
    }
    
    override fun findByNamespaceAndEnvironment(
        namespace: String,
        environment: Environment
    ): Configuration? {
        return springDataRepository.findByNamespaceAndEnvironment(
            namespace,
            environment.toString()
        )?.toDomain()
    }
    
    override fun delete(configuration: Configuration) {
        springDataRepository.deleteById(configuration.id.toString())
    }
    
    override fun exists(namespace: String, environment: Environment): Boolean {
        return springDataRepository.existsByNamespaceAndEnvironment(
            namespace,
            environment.toString()
        )
    }
    
    override fun count(): Long {
        return springDataRepository.count()
    }
    
    override fun countByNamespace(namespace: String): Long {
        return springDataRepository.countByNamespace(namespace)
    }
    
    override fun countByEnvironment(environment: Environment): Long {
        return springDataRepository.countByEnvironment(environment.toString())
    }
    
    override fun findAllNamespaces(): List<String> {
        return springDataRepository.findAllNamespaces()
    }
    
    override fun findAllEnvironments(): List<Environment> {
        return springDataRepository.findAllEnvironments()
            .map { Environment.of(it) }
    }
}