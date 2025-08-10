package com.lifee.config.domain.services

import com.lifee.config.domain.valueobjects.*
import com.lifee.config.domain.entities.ConfigItem
import com.lifee.config.domain.exceptions.InvalidConfigurationException
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

/**
 * 配置验证服务
 * 提供配置值格式验证、权限检查等业务规则
 */
class ConfigValidationService {
    
    companion object {
        private val logger = LoggerFactory.getLogger(ConfigValidationService::class.java)
        
        // 配置限制常量
        private const val MAX_CONFIG_ITEMS_PER_NAMESPACE = 1000
        private const val MAX_BATCH_UPDATE_SIZE = 100
        private const val MAX_NAMESPACE_LENGTH = 100
        private const val MIN_NAMESPACE_LENGTH = 3
        private const val MAX_DESCRIPTION_LENGTH = 500
        private const val MIN_DESCRIPTION_LENGTH = 0
        
        // 敏感词列表
        private val SENSITIVE_KEYWORDS = setOf(
            "password", "secret", "key", "token", "credential",
            "auth", "private", "confidential", "sensitive"
        )
        
        // 命名空间格式验证
        private val NAMESPACE_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9._-]*$")
        
        // JSON格式验证
        private val JSON_OBJECT_PATTERN = Pattern.compile("^\\s*\\{.*\\}\\s*$", Pattern.DOTALL)
        private val JSON_ARRAY_PATTERN = Pattern.compile("^\\s*\\[.*\\]\\s*$", Pattern.DOTALL)
        
        // URL格式验证
        private val URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
            Pattern.CASE_INSENSITIVE
        )
        
        // 邮箱格式验证
        private val EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
        )
    }
    
    /**
     * 验证配置项
     */
    fun validateConfigItem(
        key: ConfigKey,
        value: ConfigValue,
        type: ConfigType,
        description: String = "",
        isEncrypted: Boolean = false
    ) {
        logger.debug("验证配置项: key={}, type={}, isEncrypted={}", key.value, type, isEncrypted)
        
        // 验证配置键
        validateConfigKey(key)
        
        // 验证配置值
        validateConfigValue(value, type)
        
        // 验证描述
        validateDescription(description)
        
        // 验证敏感信息处理
        validateSensitiveData(key, value, type, isEncrypted)
        
        // 验证特殊格式
        validateSpecialFormats(value, type)
        
        logger.debug("配置项验证通过: key={}", key.value)
    }
    
    /**
     * 验证配置键
     */
    private fun validateConfigKey(key: ConfigKey) {
        val keyValue = key.value
        
        // 检查是否包含敏感词
        val lowerKey = keyValue.lowercase()
        val containsSensitive = SENSITIVE_KEYWORDS.any { lowerKey.contains(it) }
        
        if (containsSensitive) {
            logger.warn("配置键包含敏感词: {}", keyValue)
        }
        
        // 检查键名是否过于简单
        if (keyValue.length < 3) {
            throw InvalidConfigurationException(
                "配置键过于简单",
                "keyTooShort",
                "配置键长度至少需要3个字符"
            )
        }
        
        // 检查是否使用了保留关键字
        val reservedKeywords = setOf("null", "undefined", "true", "false")
        if (keyValue.lowercase() in reservedKeywords) {
            throw InvalidConfigurationException(
                "配置键使用了保留关键字",
                "reservedKeyword",
                "不能使用保留关键字作为配置键: $keyValue"
            )
        }
    }
    
    /**
     * 验证配置值
     */
    private fun validateConfigValue(value: ConfigValue, type: ConfigType) {
        // 基本类型验证
        if (!type.validateValue(value)) {
            throw InvalidConfigurationException(
                "配置值类型不匹配",
                "typeMismatch",
                "配置值 '${value.value}' 与类型 $type 不匹配"
            )
        }
        
        // 特定类型的额外验证
        when (type) {
            ConfigType.INTEGER -> validateIntegerValue(value)
            ConfigType.LONG -> validateLongValue(value)
            ConfigType.DOUBLE -> validateDoubleValue(value)
            ConfigType.JSON -> validateJsonValue(value)
            ConfigType.LIST -> validateListValue(value)
            ConfigType.PASSWORD -> validatePasswordValue(value)
            else -> { /* STRING和BOOLEAN类型无需额外验证 */ }
        }
    }
    
    /**
     * 验证整数值
     */
    private fun validateIntegerValue(value: ConfigValue) {
        try {
            val intValue = value.toInt()
            if (intValue < Int.MIN_VALUE || intValue > Int.MAX_VALUE) {
                throw InvalidConfigurationException(
                    "整数值超出范围",
                    "integerOutOfRange",
                    "整数值必须在 ${Int.MIN_VALUE} 到 ${Int.MAX_VALUE} 之间"
                )
            }
        } catch (e: NumberFormatException) {
            throw InvalidConfigurationException(
                "无效的整数格式",
                "invalidIntegerFormat",
                "配置值 '${value.value}' 不是有效的整数格式"
            )
        }
    }
    
    /**
     * 验证长整数值
     */
    private fun validateLongValue(value: ConfigValue) {
        try {
            value.toLong()
        } catch (e: NumberFormatException) {
            throw InvalidConfigurationException(
                "无效的长整数格式",
                "invalidLongFormat",
                "配置值 '${value.value}' 不是有效的长整数格式"
            )
        }
    }
    
    /**
     * 验证双精度浮点数值
     */
    private fun validateDoubleValue(value: ConfigValue) {
        try {
            val doubleValue = value.toDouble()
            if (doubleValue.isNaN() || doubleValue.isInfinite()) {
                throw InvalidConfigurationException(
                    "无效的浮点数值",
                    "invalidDoubleValue",
                    "浮点数值不能为NaN或无穷大"
                )
            }
        } catch (e: NumberFormatException) {
            throw InvalidConfigurationException(
                "无效的浮点数格式",
                "invalidDoubleFormat",
                "配置值 '${value.value}' 不是有效的浮点数格式"
            )
        }
    }
    
    /**
     * 验证JSON值
     */
    private fun validateJsonValue(value: ConfigValue) {
        val jsonValue = value.value.trim()
        
        if (jsonValue.isEmpty()) {
            throw InvalidConfigurationException(
                "JSON值不能为空",
                "emptyJsonValue",
                "JSON类型的配置值不能为空"
            )
        }
        
        val isValidJson = JSON_OBJECT_PATTERN.matcher(jsonValue).matches() ||
                         JSON_ARRAY_PATTERN.matcher(jsonValue).matches()
        
        if (!isValidJson) {
            throw InvalidConfigurationException(
                "无效的JSON格式",
                "invalidJsonFormat",
                "配置值必须是有效的JSON对象或数组格式"
            )
        }
        
        // 检查JSON长度
        if (jsonValue.length > 10000) {
            throw InvalidConfigurationException(
                "JSON值过长",
                "jsonTooLong",
                "JSON配置值长度不能超过10000个字符"
            )
        }
    }
    
    /**
     * 验证列表值
     */
    private fun validateListValue(value: ConfigValue) {
        val list = value.toList()
        
        if (list.size > 100) {
            throw InvalidConfigurationException(
                "列表项过多",
                "listTooLong",
                "列表配置值不能超过100个项目"
            )
        }
        
        // 检查是否有重复项
        if (list.size != list.toSet().size) {
            logger.warn("列表配置值包含重复项: {}", value.value)
        }
        
        // 检查每个项目的长度
        list.forEach { item ->
            if (item.length > 200) {
                throw InvalidConfigurationException(
                    "列表项过长",
                    "listItemTooLong",
                    "列表中的每个项目长度不能超过200个字符"
                )
            }
        }
    }
    
    /**
     * 验证密码值
     */
    private fun validatePasswordValue(value: ConfigValue) {
        val password = value.value
        
        if (password.length < 8) {
            throw InvalidConfigurationException(
                "密码过短",
                "passwordTooShort",
                "密码长度至少需要8个字符"
            )
        }
        
        if (password.length > 128) {
            throw InvalidConfigurationException(
                "密码过长",
                "passwordTooLong",
                "密码长度不能超过128个字符"
            )
        }
        
        // 检查密码复杂度
        val hasLower = password.any { it.isLowerCase() }
        val hasUpper = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        
        val complexityScore = listOf(hasLower, hasUpper, hasDigit, hasSpecial).count { it }
        
        if (complexityScore < 3) {
            logger.warn("密码复杂度较低，建议包含大小写字母、数字和特殊字符")
        }
    }
    
    /**
     * 验证描述
     */
    private fun validateDescription(description: String) {
        if (description.length > MAX_DESCRIPTION_LENGTH) {
            throw InvalidConfigurationException(
                "描述过长",
                "descriptionTooLong",
                "描述长度不能超过${MAX_DESCRIPTION_LENGTH}个字符"
            )
        }
    }
    
    /**
     * 验证敏感数据处理
     */
    private fun validateSensitiveData(
        key: ConfigKey,
        value: ConfigValue,
        type: ConfigType,
        isEncrypted: Boolean
    ) {
        val keyValue = key.value.lowercase()
        val containsSensitive = SENSITIVE_KEYWORDS.any { keyValue.contains(it) }
        
        if (containsSensitive && type != ConfigType.PASSWORD && !isEncrypted) {
            logger.warn("敏感配置项未加密: key={}, type={}", key.value, type)
        }
        
        if (type == ConfigType.PASSWORD && !isEncrypted) {
            logger.warn("密码类型配置项未加密: key={}", key.value)
        }
    }
    
    /**
     * 验证特殊格式
     */
    private fun validateSpecialFormats(value: ConfigValue, type: ConfigType) {
        val valueStr = value.value
        
        // 检查是否看起来像URL
        if (valueStr.startsWith("http") && !URL_PATTERN.matcher(valueStr).matches()) {
            logger.warn("配置值看起来像URL但格式不正确: {}", valueStr)
        }
        
        // 检查是否看起来像邮箱
        if (valueStr.contains("@") && !EMAIL_PATTERN.matcher(valueStr).matches()) {
            logger.warn("配置值看起来像邮箱但格式不正确: {}", valueStr)
        }
    }
    
    /**
     * 验证命名空间
     */
    fun validateNamespace(namespace: String) {
        if (namespace.length < MIN_NAMESPACE_LENGTH) {
            throw InvalidConfigurationException(
                "命名空间过短",
                "namespaceTooShort",
                "命名空间长度至少需要${MIN_NAMESPACE_LENGTH}个字符"
            )
        }
        
        if (namespace.length > MAX_NAMESPACE_LENGTH) {
            throw InvalidConfigurationException(
                "命名空间过长",
                "namespaceTooLong",
                "命名空间长度不能超过${MAX_NAMESPACE_LENGTH}个字符"
            )
        }
        
        if (!NAMESPACE_PATTERN.matcher(namespace).matches()) {
            throw InvalidConfigurationException(
                "命名空间格式不正确",
                "invalidNamespaceFormat",
                "命名空间只能包含字母、数字、点号、下划线和连字符，且必须以字母开头"
            )
        }
        
        // 检查保留命名空间
        val reservedNamespaces = setOf("system", "internal", "admin", "root")
        if (namespace.lowercase() in reservedNamespaces) {
            throw InvalidConfigurationException(
                "使用了保留命名空间",
                "reservedNamespace",
                "不能使用保留命名空间: $namespace"
            )
        }
    }
    
    /**
     * 验证配置项数量限制
     */
    fun validateConfigItemCount(currentCount: Int, namespace: String) {
        if (currentCount >= MAX_CONFIG_ITEMS_PER_NAMESPACE) {
            throw InvalidConfigurationException(
                "配置项数量超限",
                "configItemCountExceeded",
                "命名空间 '$namespace' 的配置项数量不能超过 $MAX_CONFIG_ITEMS_PER_NAMESPACE 个"
            )
        }
    }
    
    /**
     * 验证批量更新
     */
    fun validateBatchUpdate(updates: Map<ConfigKey, ConfigValue>) {
        if (updates.isEmpty()) {
            throw InvalidConfigurationException(
                "批量更新为空",
                "emptyBatchUpdate",
                "批量更新不能为空"
            )
        }
        
        if (updates.size > MAX_BATCH_UPDATE_SIZE) {
            throw InvalidConfigurationException(
                "批量更新数量超限",
                "batchUpdateSizeExceeded",
                "批量更新数量不能超过 $MAX_BATCH_UPDATE_SIZE 个"
            )
        }
    }
    
    /**
     * 验证权限
     */
    fun validatePermission(
        userId: String,
        namespace: String,
        environment: Environment,
        operation: String
    ) {
        logger.debug("验证权限: userId={}, namespace={}, environment={}, operation={}", 
                    userId, namespace, environment, operation)
        
        // 检查用户是否有权限访问该命名空间
        if (namespace.startsWith("system.") && !isSystemAdmin(userId)) {
            throw InvalidConfigurationException(
                "权限不足",
                "insufficientPermission",
                "用户 $userId 没有权限访问系统命名空间 $namespace"
            )
        }
        
        // 检查生产环境权限
        if (environment == Environment.PRODUCTION && !hasProductionAccess(userId)) {
            throw InvalidConfigurationException(
                "生产环境权限不足",
                "productionAccessDenied",
                "用户 $userId 没有权限访问生产环境配置"
            )
        }
        
        // 检查操作权限
        when (operation) {
            "DELETE" -> {
                if (!hasDeletePermission(userId, namespace)) {
                    throw InvalidConfigurationException(
                        "删除权限不足",
                        "deletePermissionDenied",
                        "用户 $userId 没有权限删除命名空间 $namespace 的配置"
                    )
                }
            }
            "BATCH_UPDATE" -> {
                if (!hasBatchUpdatePermission(userId, namespace)) {
                    throw InvalidConfigurationException(
                        "批量更新权限不足",
                        "batchUpdatePermissionDenied",
                        "用户 $userId 没有权限批量更新命名空间 $namespace 的配置"
                    )
                }
            }
        }
    }
    
    /**
     * 检查是否为系统管理员
     */
    private fun isSystemAdmin(userId: String): Boolean {
        // 这里应该从用户服务或权限服务获取用户角色
        // 暂时简单实现
        return userId.startsWith("admin_")
    }
    
    /**
     * 检查是否有生产环境访问权限
     */
    private fun hasProductionAccess(userId: String): Boolean {
        // 这里应该从权限服务获取用户权限
        // 暂时简单实现
        return userId.startsWith("admin_") || userId.startsWith("prod_")
    }
    
    /**
     * 检查是否有删除权限
     */
    private fun hasDeletePermission(userId: String, namespace: String): Boolean {
        // 这里应该从权限服务获取用户权限
        // 暂时简单实现
        return userId.startsWith("admin_") || namespace.startsWith("user_$userId")
    }
    
    /**
     * 检查是否有批量更新权限
     */
    private fun hasBatchUpdatePermission(userId: String, namespace: String): Boolean {
        // 这里应该从权限服务获取用户权限
        // 暂时简单实现
        return userId.startsWith("admin_") || namespace.startsWith("user_$userId")
    }
}