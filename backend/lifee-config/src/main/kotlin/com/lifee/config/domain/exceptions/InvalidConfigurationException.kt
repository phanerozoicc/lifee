package com.lifee.config.domain.exceptions

/**
 * 无效配置异常
 * 用于表示配置验证失败的情况
 */
class InvalidConfigurationException(
    message: String,
    val errorType: String,
    val details: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    
    companion object {
        // 配置键相关错误
        const val KEY_TOO_SHORT = "keyTooShort"
        const val RESERVED_KEYWORD = "reservedKeyword"
        
        // 配置值相关错误
        const val TYPE_MISMATCH = "typeMismatch"
        const val INTEGER_OUT_OF_RANGE = "integerOutOfRange"
        const val INVALID_INTEGER_FORMAT = "invalidIntegerFormat"
        const val INVALID_LONG_FORMAT = "invalidLongFormat"
        const val INVALID_DOUBLE_VALUE = "invalidDoubleValue"
        const val INVALID_DOUBLE_FORMAT = "invalidDoubleFormat"
        const val EMPTY_JSON_VALUE = "emptyJsonValue"
        const val INVALID_JSON_FORMAT = "invalidJsonFormat"
        const val JSON_TOO_LONG = "jsonTooLong"
        const val LIST_TOO_LONG = "listTooLong"
        const val LIST_ITEM_TOO_LONG = "listItemTooLong"
        const val PASSWORD_TOO_SHORT = "passwordTooShort"
        const val PASSWORD_TOO_LONG = "passwordTooLong"
        
        // 描述相关错误
        const val DESCRIPTION_TOO_LONG = "descriptionTooLong"
        
        // 命名空间相关错误
        const val NAMESPACE_TOO_SHORT = "namespaceTooShort"
        const val NAMESPACE_TOO_LONG = "namespaceTooLong"
        const val INVALID_NAMESPACE_FORMAT = "invalidNamespaceFormat"
        const val RESERVED_NAMESPACE = "reservedNamespace"
        
        // 数量限制相关错误
        const val CONFIG_ITEM_COUNT_EXCEEDED = "configItemCountExceeded"
        const val EMPTY_BATCH_UPDATE = "emptyBatchUpdate"
        const val BATCH_UPDATE_SIZE_EXCEEDED = "batchUpdateSizeExceeded"
        
        // 权限相关错误
        const val INSUFFICIENT_PERMISSION = "insufficientPermission"
        const val PRODUCTION_ACCESS_DENIED = "productionAccessDenied"
        const val DELETE_PERMISSION_DENIED = "deletePermissionDenied"
        const val BATCH_UPDATE_PERMISSION_DENIED = "batchUpdatePermissionDenied"
    }
    
    /**
     * 获取错误详情
     */
    fun getErrorDetails(): Map<String, Any> {
        return mapOf(
            "errorType" to errorType,
            "message" to (message ?: ""),
            "details" to details,
            "timestamp" to System.currentTimeMillis()
        )
    }
    
    override fun toString(): String {
        return "InvalidConfigurationException(errorType='$errorType', message='$message', details='$details')"
    }
}