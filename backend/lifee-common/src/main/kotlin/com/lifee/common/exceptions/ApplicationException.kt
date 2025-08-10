package com.lifee.common.exceptions

/**
 * 应用层异常
 * 表示应用服务层的业务逻辑错误
 */
open class ApplicationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 验证异常
 * 表示输入数据验证失败
 */
class ValidationException(
    message: String,
    cause: Throwable? = null
) : ApplicationException(message, cause)

/**
 * 授权异常
 * 表示用户没有权限执行某个操作
 */
class AuthorizationException(
    message: String,
    cause: Throwable? = null
) : ApplicationException(message, cause)

/**
 * 资源未找到异常
 * 表示请求的资源不存在
 */
class ResourceNotFoundException(
    message: String,
    cause: Throwable? = null
) : ApplicationException(message, cause)