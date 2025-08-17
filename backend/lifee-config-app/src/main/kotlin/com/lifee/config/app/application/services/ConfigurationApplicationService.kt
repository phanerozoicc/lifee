package com.lifee.config.app.application.services

import com.lifee.common.cqrs.commands.CommandBus
import com.lifee.common.cqrs.queries.QueryBus
import com.lifee.common.eventsourcing.ConcurrencyException
import com.lifee.common.exceptions.ApplicationException
import com.lifee.config.app.application.commands.*
import com.lifee.config.app.application.dtos.*
import com.lifee.config.app.application.queries.*
import com.lifee.config.domain.valueobjects.ConfigType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 配置应用服务
 */
@Service
class ConfigurationApplicationService(
    private val commandBus: CommandBus,
    private val queryBus: QueryBus
) {
    
    private val logger = LoggerFactory.getLogger(ConfigurationApplicationService::class.java)
    private val maxRetryAttempts = 3
    
    /**
     * 执行命令并处理并发冲突
     */
    private fun <T> executeCommandWithRetry(command: T, operation: String): Unit {
        var attemptCount = 0
        
        while (attemptCount < maxRetryAttempts) {
            try {
                commandBus.send(command)
                return
            } catch (e: ConcurrencyException) {
                attemptCount++
                logger.warn("Configuration {} conflict (attempt {}): aggregateId={}, expectedVersion={}, actualVersion={}",
                    operation, attemptCount, e.aggregateId, e.expectedVersion, e.actualVersion)
                
                if (attemptCount >= maxRetryAttempts) {
                    logger.error("Configuration {} failed after {} attempts: {}", operation, maxRetryAttempts, e.message)
                    throw ApplicationException("配置${operation}冲突，请重试", e)
                }
                
                // 延迟重试
                Thread.sleep(attemptCount * 100L)
            }
        }
    }
    
    /**
     * 创建配置
     */
    @Transactional
    fun createConfiguration(namespace: String, environment: String) {
        val command = CreateConfigurationCommand(
            namespace = namespace,
            environment = environment
        )
        executeCommandWithRetry(command, "创建")
    }
    
    /**
     * 添加配置项
     */
    @Transactional
    fun addConfigItem(
        namespace: String,
        environment: String,
        key: String,
        value: String,
        type: ConfigType,
        description: String = "",
        isEncrypted: Boolean = false
    ) {
        val command = AddConfigItemCommand(
            namespace = namespace,
            environment = environment,
            key = key,
            value = value,
            type = type,
            description = description,
            isEncrypted = isEncrypted
        )
        executeCommandWithRetry(command, "添加配置项")
    }
    
    /**
     * 更新配置项
     */
    @Transactional
    fun updateConfigItem(
        namespace: String,
        environment: String,
        key: String,
        value: String,
        description: String? = null
    ) {
        val command = UpdateConfigItemCommand(
            namespace = namespace,
            environment = environment,
            key = key,
            value = value,
            description = description
        )
        executeCommandWithRetry(command, "更新配置项")
    }
    
    /**
     * 删除配置项
     */
    @Transactional
    fun removeConfigItem(
        namespace: String,
        environment: String,
        key: String
    ) {
        val command = RemoveConfigItemCommand(
            namespace = namespace,
            environment = environment,
            key = key
        )
        executeCommandWithRetry(command, "删除配置项")
    }
    
    /**
     * 批量更新配置项
     */
    @Transactional
    fun batchUpdateConfigItems(
        namespace: String,
        environment: String,
        items: List<BatchUpdateConfigItemsCommand.ConfigItemUpdate>
    ) {
        val command = BatchUpdateConfigItemsCommand(
            namespace = namespace,
            environment = environment,
            items = items
        )
        executeCommandWithRetry(command, "批量更新配置项")
    }
    
    /**
     * 清空配置
     */
    @Transactional
    fun clearConfiguration(
        namespace: String,
        environment: String
    ) {
        val command = ClearConfigurationCommand(
            namespace = namespace,
            environment = environment
        )
        executeCommandWithRetry(command, "清空配置")
    }
    
    /**
     * 删除配置
     */
    @Transactional
    fun deleteConfiguration(
        namespace: String,
        environment: String
    ) {
        val command = DeleteConfigurationCommand(
            namespace = namespace,
            environment = environment
        )
        executeCommandWithRetry(command, "删除配置")
    }
    
    /**
     * 复制配置到环境
     */
    @Transactional
    fun copyConfigurationToEnvironment(
        sourceNamespace: String,
        sourceEnvironment: String,
        targetNamespace: String,
        targetEnvironment: String,
        overwrite: Boolean = false
    ) {
        val command = CopyConfigurationToEnvironmentCommand(
            sourceNamespace = sourceNamespace,
            sourceEnvironment = sourceEnvironment,
            targetNamespace = targetNamespace,
            targetEnvironment = targetEnvironment,
            overwrite = overwrite
        )
        executeCommandWithRetry(command, "复制配置")
    }
    
    /**
     * 获取配置详情
     */
    fun getConfiguration(configurationId: String): ConfigurationDetailDto {
        val query = GetConfigurationQuery(configurationId)
        return queryBus.send(query)
    }
    
    /**
     * 根据命名空间和环境获取配置
     */
    fun getConfigurationByNamespace(
        namespace: String,
        environment: String
    ): ConfigurationDetailDto {
        val query = GetConfigurationByNamespaceQuery(namespace, environment)
        return queryBus.send(query)
    }
    
    /**
     * 获取配置项
     */
    fun getConfigItem(
        namespace: String,
        environment: String,
        key: String
    ): ConfigItemDto {
        val query = GetConfigItemQuery(namespace, environment, key)
        return queryBus.send(query)
    }
    
    /**
     * 获取配置值
     */
    fun getConfigValue(
        namespace: String,
        environment: String,
        key: String
    ): ConfigValueDto {
        val query = GetConfigValueQuery(namespace, environment, key)
        return queryBus.send(query)
    }
    
    /**
     * 列出命名空间配置
     */
    fun listNamespaceConfigurations(namespace: String): List<ConfigurationDto> {
        val query = ListNamespaceConfigurationsQuery(namespace)
        return queryBus.send(query)
    }
    
    /**
     * 列出环境配置
     */
    fun listEnvironmentConfigurations(environment: String): List<ConfigurationDto> {
        val query = ListEnvironmentConfigurationsQuery(environment)
        return queryBus.send(query)
    }
    
    /**
     * 搜索配置项
     */
    fun searchConfigItems(
        namespace: String,
        environment: String,
        keyword: String = "",
        page: Int = 0,
        size: Int = 20
    ): ConfigItemPageDto {
        val query = SearchConfigItemsQuery(namespace, environment, keyword, page, size)
        return queryBus.send(query)
    }
    
    /**
     * 获取配置统计
     */
    fun getConfigurationStats(
        namespace: String,
        environment: String
    ): ConfigurationStatsDto {
        val query = GetConfigurationStatsQuery(namespace, environment)
        return queryBus.send(query)
    }
    
    /**
     * 获取命名空间统计
     */
    fun getNamespaceStats(namespace: String): NamespaceStatsDto {
        val query = GetNamespaceStatsQuery(namespace)
        return queryBus.send(query)
    }
    
    /**
     * 比较配置
     */
    fun compareConfigurations(
        sourceNamespace: String,
        sourceEnvironment: String,
        targetNamespace: String,
        targetEnvironment: String
    ): ConfigurationComparisonDto {
        val query = CompareConfigurationsQuery(
            sourceNamespace, sourceEnvironment,
            targetNamespace, targetEnvironment
        )
        return queryBus.send(query)
    }
}