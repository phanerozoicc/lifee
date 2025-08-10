package com.lifee.common.exceptions

/**
 * 基础设施层异常
 * 表示基础设施层的技术错误，如数据库连接、网络通信等
 */
open class InfrastructureException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 数据库异常
 * 表示数据库操作相关的错误
 */
class DatabaseException(
    message: String,
    cause: Throwable? = null
) : InfrastructureException(message, cause)

/**
 * 网络异常
 * 表示网络通信相关的错误
 */
class NetworkException(
    message: String,
    cause: Throwable? = null
) : InfrastructureException(message, cause)

/**
 * 外部服务异常
 * 表示调用外部服务时发生的错误
 */
class ExternalServiceException(
    message: String,
    cause: Throwable? = null
) : InfrastructureException(message, cause)

/**
 * 配置异常
 * 表示系统配置相关的错误
 */
class ConfigurationException(
    message: String,
    cause: Throwable? = null
) : InfrastructureException(message, cause)