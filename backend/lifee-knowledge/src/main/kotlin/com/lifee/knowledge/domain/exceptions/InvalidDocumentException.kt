package com.lifee.knowledge.domain.exceptions

/**
 * 无效文档异常
 * 当文档内容不符合业务规则时抛出
 */
class InvalidDocumentException(message: String) : RuntimeException(message)