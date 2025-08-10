package com.lifee.knowledge.infrastructure.persistence.entities

import jakarta.persistence.*
import java.time.Instant
import java.util.*

/**
 * 文档JPA实体
 */
@Entity
@Table(name = "documents")
class DocumentEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    var id: UUID = UUID.randomUUID()
    
    @Column(name = "title", nullable = false, length = 200)
    var title: String = ""
    
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    var content: String = ""
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    var type: DocumentTypeEnum = DocumentTypeEnum.TEXT
    
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_base_id", nullable = false)
    var knowledgeBase: KnowledgeBaseEntity? = null
    
    // 添加唯一约束：同一知识库下的文档标题不能重复
    @Table(uniqueConstraints = [
        UniqueConstraint(columnNames = ["knowledge_base_id", "title"])
    ])
    class UniqueConstraints
}

/**
 * 文档类型枚举
 */
enum class DocumentTypeEnum {
    MARKDOWN,
    TEXT,
    PDF,
    WORD,
    HTML,
    JSON,
    XML
}