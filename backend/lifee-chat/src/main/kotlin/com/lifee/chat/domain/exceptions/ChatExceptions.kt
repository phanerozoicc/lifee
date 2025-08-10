package com.lifee.chat.domain.exceptions

/**
 * 对话未找到异常
 */
class ConversationNotFoundException(
    message: String = "对话不存在",
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 消息未找到异常
 */
class MessageNotFoundException(
    message: String = "消息不存在",
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 无权访问对话异常
 */
class UnauthorizedConversationAccessException(
    message: String = "无权访问此对话",
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 对话容量超限异常
 */
class ConversationCapacityExceededException(
    message: String = "对话消息数量已达上限",
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 消息内容无效异常
 */
class InvalidMessageContentException(
    message: String = "消息内容无效",
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 不支持的消息类型异常
 */
class UnsupportedMessageTypeException(
    message: String = "不支持的消息类型",
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 对话标题无效异常
 */
class InvalidConversationTitleException(
    message: String = "对话标题无效",
    cause: Throwable? = null
) : RuntimeException(message, cause)