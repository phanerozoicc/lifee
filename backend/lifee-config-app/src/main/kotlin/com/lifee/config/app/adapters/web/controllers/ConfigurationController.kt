package com.lifee.config.app.adapters.web.controllers

import com.lifee.config.app.application.commands.BatchUpdateConfigItemsCommand
import com.lifee.config.app.application.dtos.*
import com.lifee.config.app.application.services.ConfigurationApplicationService
import com.lifee.config.domain.valueobjects.ConfigType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 配置控制器
 */
@RestController
@RequestMapping("/api/v1/configurations")
class ConfigurationController(
    private val configurationService: ConfigurationApplicationService
) {
    
    /**
     * 创建配置
     */
    @PostMapping
    suspend fun createConfiguration(
        @Valid @RequestBody request: CreateConfigurationRequest
    ): ResponseEntity<Void> {
        configurationService.createConfiguration(
            namespace = request.namespace,
            environment = request.environment
        )
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
    
    /**
     * 获取配置详情
     */
    @GetMapping("/{configurationId}")
    suspend fun getConfiguration(
        @PathVariable configurationId: String
    ): ResponseEntity<ConfigurationDetailDto> {
        val configuration = configurationService.getConfiguration(configurationId)
        return ResponseEntity.ok(configuration)
    }
    
    /**
     * 根据命名空间和环境获取配置
     */
    @GetMapping
    suspend fun getConfigurationByNamespace(
        @RequestParam namespace: String,
        @RequestParam environment: String
    ): ResponseEntity<ConfigurationDetailDto> {
        val configuration = configurationService.getConfigurationByNamespace(namespace, environment)
        return ResponseEntity.ok(configuration)
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping
    suspend fun deleteConfiguration(
        @RequestParam namespace: String,
        @RequestParam environment: String
    ): ResponseEntity<Void> {
        configurationService.deleteConfiguration(namespace, environment)
        return ResponseEntity.noContent().build()
    }
    
    /**
     * 清空配置
     */
    @DeleteMapping("/items")
    suspend fun clearConfiguration(
        @RequestParam namespace: String,
        @RequestParam environment: String
    ): ResponseEntity<Void> {
        configurationService.clearConfiguration(namespace, environment)
        return ResponseEntity.noContent().build()
    }
    
    /**
     * 添加配置项
     */
    @PostMapping("/items")
    suspend fun addConfigItem(
        @Valid @RequestBody request: AddConfigItemRequest
    ): ResponseEntity<Void> {
        configurationService.addConfigItem(
            namespace = request.namespace,
            environment = request.environment,
            key = request.key,
            value = request.value,
            type = request.type,
            description = request.description,
            isEncrypted = request.isEncrypted
        )
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
    
    /**
     * 获取配置项
     */
    @GetMapping("/items")
    suspend fun getConfigItem(
        @RequestParam namespace: String,
        @RequestParam environment: String,
        @RequestParam key: String
    ): ResponseEntity<ConfigItemDto> {
        val item = configurationService.getConfigItem(namespace, environment, key)
        return ResponseEntity.ok(item)
    }
    
    /**
     * 更新配置项
     */
    @PutMapping("/items")
    suspend fun updateConfigItem(
        @Valid @RequestBody request: UpdateConfigItemRequest
    ): ResponseEntity<Void> {
        configurationService.updateConfigItem(
            namespace = request.namespace,
            environment = request.environment,
            key = request.key,
            value = request.value,
            description = request.description
        )
        return ResponseEntity.noContent().build()
    }
    
    /**
     * 删除配置项
     */
    @DeleteMapping("/items/{key}")
    suspend fun removeConfigItem(
        @PathVariable key: String,
        @RequestParam namespace: String,
        @RequestParam environment: String
    ): ResponseEntity<Void> {
        configurationService.removeConfigItem(namespace, environment, key)
        return ResponseEntity.noContent().build()
    }
    
    /**
     * 批量更新配置项
     */
    @PutMapping("/items/batch")
    suspend fun batchUpdateConfigItems(
        @Valid @RequestBody request: BatchUpdateConfigItemsRequest
    ): ResponseEntity<Void> {
        configurationService.batchUpdateConfigItems(
            namespace = request.namespace,
            environment = request.environment,
            items = request.items
        )
        return ResponseEntity.noContent().build()
    }
    
    /**
     * 搜索配置项
     */
    @GetMapping("/items/search")
    suspend fun searchConfigItems(
        @RequestParam namespace: String,
        @RequestParam environment: String,
        @RequestParam(defaultValue = "") keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ConfigItemPageDto> {
        val result = configurationService.searchConfigItems(namespace, environment, keyword, page, size)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 获取配置值
     */
    @GetMapping("/values")
    suspend fun getConfigValue(
        @RequestParam namespace: String,
        @RequestParam environment: String,
        @RequestParam key: String
    ): ResponseEntity<ConfigValueDto> {
        val value = configurationService.getConfigValue(namespace, environment, key)
        return ResponseEntity.ok(value)
    }
    
    /**
     * 复制配置到环境
     */
    @PostMapping("/copy")
    suspend fun copyConfigurationToEnvironment(
        @Valid @RequestBody request: CopyConfigurationRequest
    ): ResponseEntity<Void> {
        configurationService.copyConfigurationToEnvironment(
            sourceNamespace = request.sourceNamespace,
            sourceEnvironment = request.sourceEnvironment,
            targetNamespace = request.targetNamespace,
            targetEnvironment = request.targetEnvironment,
            overwrite = request.overwrite
        )
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
    
    /**
     * 获取配置统计
     */
    @GetMapping("/stats")
    suspend fun getConfigurationStats(
        @RequestParam namespace: String,
        @RequestParam environment: String
    ): ResponseEntity<ConfigurationStatsDto> {
        val stats = configurationService.getConfigurationStats(namespace, environment)
        return ResponseEntity.ok(stats)
    }
    
    /**
     * 比较配置
     */
    @GetMapping("/compare")
    suspend fun compareConfigurations(
        @RequestParam sourceNamespace: String,
        @RequestParam sourceEnvironment: String,
        @RequestParam targetNamespace: String,
        @RequestParam targetEnvironment: String
    ): ResponseEntity<ConfigurationComparisonDto> {
        val comparison = configurationService.compareConfigurations(
            sourceNamespace, sourceEnvironment,
            targetNamespace, targetEnvironment
        )
        return ResponseEntity.ok(comparison)
    }
    
    /**
     * 列出命名空间配置
     */
    @GetMapping("/namespaces/{namespace}")
    suspend fun listNamespaceConfigurations(
        @PathVariable namespace: String
    ): ResponseEntity<List<ConfigurationDto>> {
        val configurations = configurationService.listNamespaceConfigurations(namespace)
        return ResponseEntity.ok(configurations)
    }
    
    /**
     * 列出环境配置
     */
    @GetMapping("/environments/{environment}")
    suspend fun listEnvironmentConfigurations(
        @PathVariable environment: String
    ): ResponseEntity<List<ConfigurationDto>> {
        val configurations = configurationService.listEnvironmentConfigurations(environment)
        return ResponseEntity.ok(configurations)
    }
    
    /**
     * 获取命名空间统计
     */
    @GetMapping("/namespaces/{namespace}/stats")
    suspend fun getNamespaceStats(
        @PathVariable namespace: String
    ): ResponseEntity<NamespaceStatsDto> {
        val stats = configurationService.getNamespaceStats(namespace)
        return ResponseEntity.ok(stats)
    }
}

/**
 * 创建配置请求
 */
data class CreateConfigurationRequest(
    @field:NotBlank(message = "命名空间不能为空")
    val namespace: String,
    
    @field:NotBlank(message = "环境不能为空")
    val environment: String
)

/**
 * 添加配置项请求
 */
data class AddConfigItemRequest(
    @field:NotBlank(message = "命名空间不能为空")
    val namespace: String,
    
    @field:NotBlank(message = "环境不能为空")
    val environment: String,
    
    @field:NotBlank(message = "配置键不能为空")
    val key: String,
    
    @field:NotBlank(message = "配置值不能为空")
    val value: String,
    
    @field:NotNull(message = "配置类型不能为空")
    val type: ConfigType,
    
    val description: String = "",
    val isEncrypted: Boolean = false
)

/**
 * 更新配置项请求
 */
data class UpdateConfigItemRequest(
    @field:NotBlank(message = "命名空间不能为空")
    val namespace: String,
    
    @field:NotBlank(message = "环境不能为空")
    val environment: String,
    
    @field:NotBlank(message = "配置键不能为空")
    val key: String,
    
    @field:NotBlank(message = "配置值不能为空")
    val value: String,
    
    val description: String? = null
)

/**
 * 批量更新配置项请求
 */
data class BatchUpdateConfigItemsRequest(
    @field:NotBlank(message = "命名空间不能为空")
    val namespace: String,
    
    @field:NotBlank(message = "环境不能为空")
    val environment: String,
    
    @field:Valid
    val items: List<BatchUpdateConfigItemsCommand.ConfigItemUpdate>
)

/**
 * 复制配置请求
 */
data class CopyConfigurationRequest(
    @field:NotBlank(message = "源命名空间不能为空")
    val sourceNamespace: String,
    
    @field:NotBlank(message = "源环境不能为空")
    val sourceEnvironment: String,
    
    @field:NotBlank(message = "目标命名空间不能为空")
    val targetNamespace: String,
    
    @field:NotBlank(message = "目标环境不能为空")
    val targetEnvironment: String,
    
    val overwrite: Boolean = false
)