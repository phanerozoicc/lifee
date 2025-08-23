package com.lifee.chat.domain.repositories

import com.lifee.chat.domain.aggregates.Conversation
import com.lifee.chat.domain.valueobjects.ConversationId
import com.lifee.common.domain.valueobjects.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * 对话仓储接口
 */
interface ConversationRepository {
    
    /**
     * 保存对话
     */
    suspend fun save(conversation: Conversation): Conversation
    
    /**
     * 根据ID查找对话
     */
    suspend fun findById(id: ConversationId): Conversation?
    
    /**
     * 根据用户ID查找对话列表
     */
    suspend fun findByUserId(userId: UserId): List<Conversation>
    
    /**
     * 根据用户ID分页查找对话列表
     */
    suspend fun findByUserId(userId: UserId, pageable: Pageable): Page<Conversation>
    
    /**
     * 根据用户ID和对话ID查找对话
     */
    suspend fun findByIdAndUserId(id: ConversationId, userId: UserId): Conversation?
    
    /**
     * 删除对话
     */
    suspend fun delete(conversation: Conversation)
    
    /**
     * 根据ID删除对话
     */
    suspend fun deleteById(id: ConversationId)
    
    /**
     * 检查对话是否存在
     */
    suspend fun existsById(id: ConversationId): Boolean
    
    /**
     * 检查用户是否拥有指定对话
     */
    suspend fun existsByIdAndUserId(id: ConversationId, userId: UserId): Boolean
    
    /**
     * 统计用户的对话数量
     */
    suspend fun countByUserId(userId: UserId): Long
}