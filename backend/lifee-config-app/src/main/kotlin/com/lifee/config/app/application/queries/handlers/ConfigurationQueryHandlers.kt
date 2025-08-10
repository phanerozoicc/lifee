package com.lifee.config.app.application.queries.handlers

import com.lifee.common.cqrs.queries.QueryHandler
import com.lifee.config.app.application.dtos.*
import com.lifee.config.app.application.queries.*
import com.lifee.config.domain.exceptions.*
import com.lifee.config.domain.repositories.ConfigurationRepository
import com.lifee.config.domain.services.ConfigurationDomainService
import com.lifee.config.domain.valueobjects.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 获取配置查询处理器
 */
@Component
class GetConfigurationQueryHandler(
    private val configurationRepository: ConfigurationRepository
) : QueryHandler<GetConfigurationQuery, ConfigurationDetailDto> {
    
    @Transactional(readOnly = true)
    override suspend fun handle(query: GetConfigurationQuery): ConfigurationDetailDto {
        val configId = ConfigId.of(query.configurationId)
        val configuration = configurationRepository.findById(configId)
            ?: throw ConfigurationNotFoundException(configId)
        
        return ConfigurationDetailDto.from(configuration)
    }
}

/**
 * 根据命名空间获取配置查询处理器
 */
@Component
class GetConfigurationByNamespaceQueryHandler(
    private val configurationRepository: ConfigurationRepository
) : QueryHandler<GetConfigurationByNamespaceQuery, ConfigurationDetailDto> {
    
    @Transactional(readOnly = true)
    override suspend fun handle(query: GetConfigurationByNamespaceQuery): ConfigurationDetailDto {
        val environment = Environment.of(query.environment)
        val configuration = configurationRepository.findByNamespaceAndEnvironment(
            query.namespace,
            environment
        ) ?: throw NamespaceConfigurationNotFoundException(query.namespace, environment)
        
        return ConfigurationDetailDto.from(configuration)
    }
}

/**
 * 获取配置项查询处理器
 */
@Component
class GetConfigItemQueryHandler(
    private val configurationRepository: ConfigurationRepository
) : QueryHandler<GetConfigItemQuery, ConfigItemDto> {
    
    @Transactional(readOnly = true)
    override suspend fun handle(query: GetConfigItemQuery): ConfigItemDto {
        val environment = Environment.of(query.environment)
        val configuration = configurationRepository.findByNamespaceAndEnvironment(
            query.namespace,
            environment
        ) ?: throw NamespaceConfigurationNotFoundException(query.namespace, environment)
        
        val configKey = ConfigKey.of(query.key)
        val item = configuration.getItem(configKey)
            ?: throw ConfigItemNotFoundException(configKey)
        
        return ConfigItemDto.from(item)
    }
}

/**
 * 获取配置值查询处理器
 */
@Component
class GetConfigValueQueryHandler(
    private val configurationRepository: ConfigurationRepository
) : QueryHandler<GetConfigValueQuery, ConfigValueDto> {
    
    @Transactional(readOnly = true)
    override suspend fun handle(query: GetConfigValueQuery): ConfigValueDto {
        val environment = Environment.of(query.environment)
        val configuration = configurationRepository.findByNamespaceAndEnvironment(
            query.namespace,
            environment
        ) ?: throw NamespaceConfigurationNotFoundException(query.namespace, environment)
        
        val configKey = ConfigKey.of(query.key)
        val item = configuration.getItem(configKey)
            ?: throw ConfigItemNotFoundException(configKey)
        
        return ConfigValueDto.from(item)
    }
}

/**
 * 列出命名空间配置查询处理器
 */
@Component
class ListNamespaceConfigurationsQueryHandler(
    private val configurationRepository: ConfigurationRepository
) : QueryHandler<ListNamespaceConfigurationsQuery, List<ConfigurationDto>> {
    
    @Transactional(readOnly = true)
    override suspend fun handle(query: ListNamespaceConfigurationsQuery): List<ConfigurationDto> {
        val configurations = configurationRepository.findByNamespace(query.namespace)
        return configurations.map { ConfigurationDto.from(it) }
    }
}

/**
 * 列出环境配置查询处理器
 */
@Component
class ListEnvironmentConfigurationsQueryHandler(
    private val configurationRepository: ConfigurationRepository
) : QueryHandler<ListEnvironmentConfigurationsQuery, List<ConfigurationDto>> {
    
    @Transactional(readOnly = true)
    override suspend fun handle(query: ListEnvironmentConfigurationsQuery): List<ConfigurationDto> {
        val environment = Environment.of(query.environment)
        val configurations = configurationRepository.findByEnvironment(environment)
        return configurations.map { ConfigurationDto.from(it) }
    }
}

/**
 * 搜索配置项查询处理器
 */
@Component
class SearchConfigItemsQueryHandler(
    private val configurationRepository: ConfigurationRepository
) : QueryHandler<SearchConfigItemsQuery, ConfigItemPageDto> {
    
    @Transactional(readOnly = true)
    override suspend fun handle(query: SearchConfigItemsQuery): ConfigItemPageDto {
        val environment = Environment.of(query.environment)
        val configuration = configurationRepository.findByNamespaceAndEnvironment(
            query.namespace,
            environment
        ) ?: throw NamespaceConfigurationNotFoundException(query.namespace, environment)
        
        // 简单的内存搜索实现
        val allItems = configuration.getItems().values
        val filteredItems = if (query.keyword.isNotBlank()) {
            allItems.filter { item ->
                item.key.toString().contains(query.keyword, ignoreCase = true) ||
                item.getDescription().contains(query.keyword, ignoreCase = true)
            }
        } else {
            allItems.toList()
        }
        
        // 分页
        val startIndex = query.page * query.size
        val endIndex = minOf(startIndex + query.size, filteredItems.size)
        val pageItems = if (startIndex < filteredItems.size) {
            filteredItems.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        val totalElements = filteredItems.size.toLong()
        val totalPages = (totalElements + query.size - 1) / query.size
        
        return ConfigItemPageDto(
            content = pageItems.map { ConfigItemDto.from(it) },
            page = query.page,
            size = query.size,
            totalElements = totalElements,
            totalPages = totalPages.toInt(),
            hasNext = query.page < totalPages - 1,
            hasPrevious = query.page > 0
        )
    }
}

/**
 * 获取配置统计查询处理器
 */
@Component
class GetConfigurationStatsQueryHandler(
    private val configurationRepository: ConfigurationRepository,
    private val domainService: ConfigurationDomainService
) : QueryHandler<GetConfigurationStatsQuery, ConfigurationStatsDto> {
    
    @Transactional(readOnly = true)
    override suspend fun handle(query: GetConfigurationStatsQuery): ConfigurationStatsDto {
        val environment = Environment.of(query.environment)
        val configuration = configurationRepository.findByNamespaceAndEnvironment(
            query.namespace,
            environment
        ) ?: throw NamespaceConfigurationNotFoundException(query.namespace, environment)
        
        val summary = domainService.generateSummary(configuration)
        return ConfigurationStatsDto.from(summary)
    }
}

/**
 * 获取命名空间统计查询处理器
 */
@Component
class GetNamespaceStatsQueryHandler(
    private val configurationRepository: ConfigurationRepository
) : QueryHandler<GetNamespaceStatsQuery, NamespaceStatsDto> {
    
    @Transactional(readOnly = true)
    override suspend fun handle(query: GetNamespaceStatsQuery): NamespaceStatsDto {
        val configurations = configurationRepository.findByNamespace(query.namespace)
        
        val environments = configurations.map { it.environment.toString() }.distinct()
        val totalItems = configurations.sumOf { it.getItemCount() }
        val lastUpdated = configurations.maxOfOrNull { it.getUpdatedAt() }
        
        return NamespaceStatsDto(
            namespace = query.namespace,
            environments = environments,
            totalConfigurations = configurations.size,
            totalItems = totalItems,
            lastUpdated = lastUpdated
        )
    }
}

/**
 * 比较配置查询处理器
 */
@Component
class CompareConfigurationsQueryHandler(
    private val configurationRepository: ConfigurationRepository,
    private val domainService: ConfigurationDomainService
) : QueryHandler<CompareConfigurationsQuery, ConfigurationComparisonDto> {
    
    @Transactional(readOnly = true)
    override suspend fun handle(query: CompareConfigurationsQuery): ConfigurationComparisonDto {
        val sourceEnv = Environment.of(query.sourceEnvironment)
        val targetEnv = Environment.of(query.targetEnvironment)
        
        val sourceConfig = configurationRepository.findByNamespaceAndEnvironment(
            query.sourceNamespace,
            sourceEnv
        ) ?: throw NamespaceConfigurationNotFoundException(query.sourceNamespace, sourceEnv)
        
        val targetConfig = configurationRepository.findByNamespaceAndEnvironment(
            query.targetNamespace,
            targetEnv
        ) ?: throw NamespaceConfigurationNotFoundException(query.targetNamespace, targetEnv)
        
        val diff = domainService.compareConfigurations(sourceConfig, targetConfig)
        
        return ConfigurationComparisonDto(
            sourceConfiguration = ConfigurationDto.from(sourceConfig),
            targetConfiguration = ConfigurationDto.from(targetConfig),
            added = diff.added.map { ConfigItemDto.from(it) },
            modified = diff.modified.map { (key, oldItem, newItem) ->
                ConfigItemComparisonDto(
                    key = key.toString(),
                    oldValue = oldItem.getValue().toString(),
                    newValue = newItem.getValue().toString(),
                    oldDisplayValue = oldItem.getDisplayValue(),
                    newDisplayValue = newItem.getDisplayValue(),
                    type = oldItem.type
                )
            },
            removed = diff.removed.map { ConfigItemDto.from(it) },
            hasChanges = diff.hasChanges()
        )
    }