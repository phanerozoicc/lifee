package com.lifee.config.app.adapters.web.controllers

import com.lifee.config.app.application.commands.BatchUpdateConfigItemsCommand
import com.lifee.config.app.application.dtos.*
import com.lifee.config.app.application.services.ConfigurationApplicationService
import com.lifee.config.domain.valueobjects.ConfigType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 配置控制器
 * 提供系统配置的管理功能
 */
@RestController
@RequestMapping("/api/v1/configurations")
@Tag(name = "配置管理", description = "系统配置的创建、查询、更新、删除功能")
class ConfigurationController(
    private val configurationService: ConfigurationApplicationService
) {
    
    /**
     * 创建配置
     * 
     * @param request 创建配置请求，包含命名空间和环境
     * @return 创建成功无返回内容
     */
    @Operation(summary = "创建配置", description = "为指定命名空间和环境创建新的配置")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "配置创建成功"),
            ApiResponse(responseCode = "400", description = "请求参数错误"),
            ApiResponse(responseCode = "409", description = "配置已存在")
        ]
    )
    @PostMapping
    suspend fun createConfiguration(
        @Parameter(description = "创建配置请求", required = true)
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
     * 
     * 支持多维度搜索指定命名空间和环境下的配置项，包含完整的分页、排序和高级过滤功能。
     * 
     * @param namespace 命名空间，必填
     * @param environment 环境，必填
     * @param keyword 搜索关键词，支持按配置键名和描述进行模糊搜索
     * @param configType 配置类型过滤，支持：STRING、NUMBER、BOOLEAN、JSON、ARRAY
     * @param isEncrypted 是否加密过滤，true表示只查询加密配置，false表示只查询非加密配置
     * @param page 页码，从0开始，默认为0，最小值为0
     * @param size 每页数量，默认为20，取值范围1-100
     * @param sortBy 排序字段，支持：key（配置键）、type（配置类型）、createdAt（创建时间）、updatedAt（更新时间）
     * @param sortOrder 排序方向，支持：asc（升序）、desc（降序），默认为asc
     * @param includeValues 是否包含配置值，默认为true，设为false时只返回配置元数据
     * @param tags 标签过滤，支持按配置标签进行过滤，多个标签用逗号分隔
     * @return 分页的配置项搜索结果
     */
    @Operation(
        summary = "搜索配置项", 
        description = "多维度搜索指定命名空间和环境下的配置项，支持关键词搜索、类型过滤、排序和分页"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "搜索成功，返回分页的配置项列表"),
            ApiResponse(responseCode = "400", description = "搜索参数错误、分页参数无效或排序参数错误"),
            ApiResponse(responseCode = "401", description = "用户未认证"),
            ApiResponse(responseCode = "403", description = "无权限访问指定命名空间或环境")
        ]
    )
    @GetMapping("/items/search")
    suspend fun searchConfigItems(
        @Parameter(description = "命名空间", required = true, example = "app")
        @RequestParam @NotBlank namespace: String,
        
        @Parameter(description = "环境", required = true, example = "production")
        @RequestParam @NotBlank environment: String,
        
        @Parameter(
            description = "搜索关键词，支持按配置键名和描述进行模糊搜索",
            example = "database"
        )
        @RequestParam(defaultValue = "") keyword: String,
        
        @Parameter(
            description = "配置类型过滤",
            example = "STRING",
            schema = io.swagger.v3.oas.annotations.media.Schema(
                allowableValues = ["STRING", "NUMBER", "BOOLEAN", "JSON", "ARRAY"]
            )
        )
        @RequestParam(required = false) configType: String?,
        
        @Parameter(
            description = "是否加密过滤，true表示只查询加密配置，false表示只查询非加密配置",
            example = "false"
        )
        @RequestParam(required = false) isEncrypted: Boolean?,
        
        @Parameter(
            description = "页码，从0开始",
            example = "0",
            schema = io.swagger.v3.oas.annotations.media.Schema(minimum = "0")
        )
        @RequestParam(defaultValue = "0") page: Int,
        
        @Parameter(
            description = "每页返回的记录数量，取值范围1-100",
            example = "20",
            schema = io.swagger.v3.oas.annotations.media.Schema(minimum = "1", maximum = "100")
        )
        @RequestParam(defaultValue = "20") size: Int,
        
        @Parameter(
            description = "排序字段",
            example = "key",
            schema = io.swagger.v3.oas.annotations.media.Schema(
                allowableValues = ["key", "type", "createdAt", "updatedAt"]
            )
        )
        @RequestParam(defaultValue = "key") sortBy: String,
        
        @Parameter(
            description = "排序方向",
            example = "asc",
            schema = io.swagger.v3.oas.annotations.media.Schema(
                allowableValues = ["asc", "desc"]
            )
        )
        @RequestParam(defaultValue = "asc") sortOrder: String,
        
        @Parameter(
            description = "是否包含配置值，默认为true，设为false时只返回配置元数据",
            example = "true"
        )
        @RequestParam(defaultValue = "true") includeValues: Boolean,
        
        @Parameter(
            description = "标签过滤，支持按配置标签进行过滤，多个标签用逗号分隔",
            example = "database,cache"
        )
        @RequestParam(required = false) tags: String?
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