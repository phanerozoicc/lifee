package com.lifee.knowledge.domain.valueobjects

/**
 * 知识库名称值对象
 */
data class KnowledgeBaseName(
    val value: String
) {
    init {
        require(value.isNotBlank()) { "Knowledge base name cannot be blank" }
        require(value.length <= 100) { "Knowledge base name cannot exceed 100 characters" }
        require(value.length >= 2) { "Knowledge base name must be at least 2 characters" }
    }
    
    override fun toString(): String = value
}