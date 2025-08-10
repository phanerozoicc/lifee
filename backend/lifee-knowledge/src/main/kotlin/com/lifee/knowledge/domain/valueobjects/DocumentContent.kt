package com.lifee.knowledge.domain.valueobjects

/**
 * 文档内容值对象
 */
data class DocumentContent(
    val value: String
) {
    init {
        require(value.isNotBlank()) { "Document content cannot be blank" }
        require(value.length <= 1_000_000) { "Document content cannot exceed 1,000,000 characters" }
    }
    
    override fun toString(): String = value
    
    /**
     * 获取内容摘要（前200个字符）
     */
    fun getSummary(): String {
        return if (value.length <= 200) {
            value
        } else {
            value.substring(0, 200) + "..."
        }
    }
    
    /**
     * 获取内容长度
     */
    fun getLength(): Int = value.length
}