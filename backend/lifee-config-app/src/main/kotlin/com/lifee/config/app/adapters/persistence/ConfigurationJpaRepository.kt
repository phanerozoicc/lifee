package com.lifee.config.app.adapters.persistence

import com.lifee.config.domain.aggregates.Configuration
import com.lifee.config.domain.repositories.ConfigurationRepository
import com.lifee.config.domain.valueobjects.ConfigId
import com.lifee.config.domain.valueobjects.Environment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

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
    private val springDataRepository: SpringDataConfigurationRepository
) : ConfigurationRepository {
    
    override fun save(configuration: Configuration): Configuration {
        val entity = ConfigurationJpaEntity.fromDomain(configuration)
        val savedEntity = springDataRepository.save(entity)
        return savedEntity.toDomain()
    }
    
    override fun findById(id: ConfigId): Configuration? {
        return springDataRepository.findById(id.toString())
            .map { it.toDomain() }
            .orElse(null)
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