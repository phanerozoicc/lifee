package com.lifee.config.domain.exceptions

import com.lifee.common.domain.DomainException
import com.lifee.config.domain.valueobjects.ConfigKey
import com.lifee.config.domain.valueobjects.ConfigId
import com.lifee.config.domain.valueobjects.Environment

/**
 * 配置未找到异常
 */
class ConfigurationNotFoundException(
    configurationId: ConfigId
) : DomainException("配置未找到: $configurationId")

/**
 * 命名空间配置未找到异常
 */
class NamespaceConfigurationNotFoundException(
    namespace: String,
    environment: Environment
) : DomainException("命名空间 '$namespace' 在环境 '$environment' 中的配置未找到")

/**
 * 配置项已存在异常
 */
class ConfigItemAlreadyExistsException(
    key: ConfigKey,
    environment: Environment
) : DomainException("配置项 '$key' 在环境 '$environment' 中已存在")

/**
 * 配置项未找到异常
 */
class ConfigItemNotFoundException(
    key: ConfigKey,
    environment: Environment
) : DomainException("配置项 '$key' 在环境 '$environment' 中未找到")

/**
 * 配置值类型不匹配异常
 */
class ConfigValueTypeMismatchException(
    key: ConfigKey,
    expectedType: String,
    actualValue: String
) : DomainException("配置项 '$key' 的值 '$actualValue' 与期望类型 '$expectedType' 不匹配")

/**
 * 配置命名空间不匹配异常
 */
class ConfigNamespaceMismatchException(
    key: ConfigKey,
    expectedNamespace: String,
    actualNamespace: String?
) : DomainException("配置项 '$key' 的命名空间 '$actualNamespace' 与期望命名空间 '$expectedNamespace' 不匹配")

/**
 * 配置环境不匹配异常
 */
class ConfigEnvironmentMismatchException(
    key: ConfigKey,
    expectedEnvironment: Environment,
    actualEnvironment: Environment
) : DomainException("配置项 '$key' 的环境 '$actualEnvironment' 与期望环境 '$expectedEnvironment' 不匹配")

/**
 * 配置验证失败异常
 */
class ConfigValidationException(
    message: String,
    val errors: List<String> = emptyList()
) : DomainException(message) {
    
    override fun toString(): String {
        return if (errors.isNotEmpty()) {
            "$message\n验证错误:\n${errors.joinToString("\n")}"
        } else {
            message ?: "配置验证失败"
        }
    }
}

/**
 * 配置加密异常
 */
class ConfigEncryptionException(
    key: ConfigKey,
    cause: Throwable? = null
) : DomainException("配置项 '$key' 加密失败", cause)

/**
 * 配置解密异常
 */
class ConfigDecryptionException(
    key: ConfigKey,
    cause: Throwable? = null
) : DomainException("配置项 '$key' 解密失败", cause)

/**
 * 配置权限异常
 */
class ConfigPermissionException(
    operation: String,
    namespace: String,
    environment: Environment
) : DomainException("没有权限在环境 '$environment' 的命名空间 '$namespace' 中执行操作: $operation")

/**
 * 配置锁定异常
 */
class ConfigLockedException(
    namespace: String,
    environment: Environment
) : DomainException("配置已被锁定，无法修改: 命名空间 '$namespace'，环境 '$environment'")

/**
 * 配置版本冲突异常
 */
class ConfigVersionConflictException(
    namespace: String,
    environment: Environment,
    expectedVersion: String,
    actualVersion: String
) : DomainException("配置版本冲突: 命名空间 '$namespace'，环境 '$environment'，期望版本 '$expectedVersion'，实际版本 '$actualVersion'")

/**
 * 配置导入异常
 */
class ConfigImportException(
    message: String,
    cause: Throwable? = null
) : DomainException("配置导入失败: $message", cause)

/**
 * 配置导出异常
 */
class ConfigExportException(
    message: String,
    cause: Throwable? = null
) : DomainException("配置导出失败: $message", cause)