package com.lifee.knowledge.domain.valueobjects

/**
 * 知识库描述值对象
 */
data class KnowledgeBaseDescription(
    val value: String
) {
    init {
        require(value.length <= 500) { "Knowledge base description cannot exceed 500 characters" }
    }
    
    override fun toString(): String = value
    
    companion object {
        fun empty(): KnowledgeBaseDescription = KnowledgeBaseDescription("")
    }
}