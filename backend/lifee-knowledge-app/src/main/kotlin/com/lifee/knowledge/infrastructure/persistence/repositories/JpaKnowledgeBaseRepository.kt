package com.lifee.knowledge.infrastructure.persistence.repositories

import com.lifee.knowledge.infrastructure.persistence.entities.KnowledgeBaseEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

/**
 * 知识库JPA Repository
 */
@Repository
interface JpaKnowledgeBaseRepository : JpaRepository<KnowledgeBaseEntity, UUID> {
    
    /**
     * 根据所有者ID查找知识库
     */
    fun findByOwnerId(ownerId: UUID): List<KnowledgeBaseEntity>
    
    /**
     * 根据所有者ID分页查找知识库
     */
    fun findByOwnerId(ownerId: UUID, pageable: Pageable): List<KnowledgeBaseEntity>
    
    /**
     * 根据所有者ID和名称查找知识库
     */
    fun findByOwnerIdAndName(ownerId: UUID, name: String): KnowledgeBaseEntity?
    
    /**
     * 检查知识库是否存在
     */
    fun existsByOwnerIdAndName(ownerId: UUID, name: String): Boolean
    
    /**
     * 统计用户的知识库数量
     */
    fun countByOwnerId(ownerId: UUID): Long
    
    /**
     * 查找知识库并预加载文档
     */
    @Query("SELECT kb FROM KnowledgeBaseEntity kb LEFT JOIN FETCH kb.documents WHERE kb.id = :id")
    fun findByIdWithDocuments(@Param("id") id: UUID): KnowledgeBaseEntity?
}