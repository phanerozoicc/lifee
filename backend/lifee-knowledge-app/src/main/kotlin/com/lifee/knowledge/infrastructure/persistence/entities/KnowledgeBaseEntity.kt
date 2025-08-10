package com.lifee.knowledge.infrastructure.persistence.entities

import jakarta.persistence.*
import java.time.Instant
import java.util.*

/**
 * 知识库JPA实体
 */
@Entity
@Table(name = "knowledge_bases")
class KnowledgeBaseEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    var id: UUID = UUID.randomUUID()
    
    @Column(name = "name", nullable = false, length = 100)
    var name: String = ""
    
    @Column(name = "description", length = 500)
    var description: String = ""
    
    @Column(name = "owner_id", nullable = false, columnDefinition = "UUID")
    var ownerId: UUID = UUID.randomUUID()
    
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
    
    @OneToMany(
        mappedBy = "knowledgeBase",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var documents: MutableSet<DocumentEntity> = mutableSetOf()
    
    // 添加唯一约束：同一用户下的知识库名称不能重复
    @Table(uniqueConstraints = [
        UniqueConstraint(columnNames = ["owner_id", "name"])
    ])
    class UniqueConstraints
}