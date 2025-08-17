package com.lifee.knowledge.domain.repositories

import com.lifee.knowledge.domain.aggregates.KnowledgeBase
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.common.domain.valueobjects.UserId

/**
 * 知识库仓储接口
 */
interface KnowledgeBaseRepository {
    
    /**
     * 保存知识库
     */
    suspend fun save(knowledgeBase: KnowledgeBase): KnowledgeBase
    
    /**
     * 根据ID查找知识库
     */
    suspend fun findById(id: KnowledgeBaseId): KnowledgeBase?
    
    /**
     * 根据用户ID查找所有知识库
     */
    suspend fun findByOwnerId(ownerId: UserId): List<KnowledgeBase>
    
    /**
     * 根据用户ID和名称查找知识库
     */
    suspend fun findByOwnerIdAndName(ownerId: UserId, name: String): KnowledgeBase?
    
    /**
     * 删除知识库
     */
    suspend fun delete(id: KnowledgeBaseId)
    
    /**
     * 检查知识库是否存在
     */
    suspend fun existsById(id: KnowledgeBaseId): Boolean
    
    /**
     * 检查用户是否已有同名知识库
     */
    suspend fun existsByOwnerIdAndName(ownerId: UserId, name: String): Boolean
    
    /**
     * 获取用户的知识库数量
     */
    suspend fun countByOwnerId(ownerId: UserId): Long
    
    /**
     * 分页查询用户的知识库
     */
    suspend fun findByOwnerIdWithPagination(
        ownerId: UserId, 
        offset: Int, 
        limit: Int
    ): List<KnowledgeBase>
}