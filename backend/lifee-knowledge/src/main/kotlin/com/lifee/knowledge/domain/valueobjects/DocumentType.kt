package com.lifee.knowledge.domain.valueobjects

/**
 * 文档类型值对象
 */
enum class DocumentType(val value: String, val description: String) {
    MARKDOWN("markdown", "Markdown文档"),
    TEXT("text", "纯文本文档"),
    PDF("pdf", "PDF文档"),
    WORD("word", "Word文档"),
    HTML("html", "HTML文档"),
    JSON("json", "JSON文档"),
    XML("xml", "XML文档");
    
    companion object {
        fun fromString(value: String): DocumentType {
            return values().find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown document type: $value")
        }
        
        fun getSupportedTypes(): List<String> {
            return values().map { it.value }
        }
    }
    
    override fun toString(): String = value
}