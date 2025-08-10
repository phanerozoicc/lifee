package com.lifee.knowledge.domain.valueobjects

/**
 * 文档标题值对象
 */
data class DocumentTitle(
    val value: String
) {
    init {
        require(value.isNotBlank()) { "Document title cannot be blank" }
        require(value.length <= 200) { "Document title cannot exceed 200 characters" }
        require(value.length >= 1) { "Document title must be at least 1 character" }
    }
    
    override fun toString(): String = value
}