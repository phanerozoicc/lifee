package com.lifee.common.exceptions

/**
 * 领域异常基类
 * 所有领域相关的异常都应该继承此类
 */
abstract class DomainException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    
    /**
     * 获取错误代码
     * 子类可以重写此方法提供特定的错误代码
     */
    open fun getErrorCode(): String = this::class.simpleName ?: "DOMAIN_ERROR"
}