package com.lifee.knowledge.domain.entities

import com.lifee.knowledge.domain.valueobjects.*
import java.time.Instant

/**
 * 文档实体
 */
data class Document(
    private val id: DocumentId,
    private var title: DocumentTitle,
    private var content: DocumentContent,
    private val type: DocumentType,
    private val createdAt: Instant,
    private var updatedAt: Instant
) {
    
    companion object {
        fun create(
            id: DocumentId,
            title: DocumentTitle,
            content: DocumentContent,
            type: DocumentType
        ): Document {
            val now = Instant.now()
            return Document(
                id = id,
                title = title,
                content = content,
                type = type,
                createdAt = now,
                updatedAt = now
            )
        }
    }
    
    // Getters
    fun getId(): DocumentId = id
    fun getTitle(): DocumentTitle = title
    fun getContent(): DocumentContent = content
    fun getType(): DocumentType = type
    fun getCreatedAt(): Instant = createdAt
    fun getUpdatedAt(): Instant = updatedAt
    
    /**
     * 更新文档内容
     */
    fun updateContent(newTitle: DocumentTitle, newContent: DocumentContent) {
        this.title = newTitle
        this.content = newContent
        this.updatedAt = Instant.now()
    }
    
    /**
     * 获取文档摘要信息
     */
    fun getSummary(): String = content.getSummary()
    
    /**
     * 获取文档大小（字符数）
     */
    fun getSize(): Int = content.getLength()
    
    /**
     * 检查文档是否为空
     */
    fun isEmpty(): Boolean = content.value.isBlank()
}