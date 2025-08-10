package com.lifee.user.app.services

import com.lifee.common.cqrs.commands.CommandBus
import com.lifee.config.app.application.commands.CreateConfigurationCommand
import com.lifee.config.domain.valueobjects.ConfigId
import com.lifee.config.domain.valueobjects.Environment
import com.lifee.knowledge.application.commands.CreateKnowledgeBaseCommand
import com.lifee.recommendation.app.application.commands.CreateRecommendationCommand
import com.lifee.recommendation.domain.valueobjects.RecommendationId
import com.lifee.user.domain.UserId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

/**
 * 用户初始化服务
 * 负责在用户注册后初始化其他模块的相关数据
 */
@Service
class UserInitializationService(
    private val commandBus: CommandBus,
    private val transactionTemplate: TransactionTemplate
) {
    
    private val logger = LoggerFactory.getLogger(UserInitializationService::class.java)
    
    /**
     * 初始化新用户的所有相关数据
     * 这个方法涉及多个模块的数据操作，使用编程式事务保证一致性
     */
    suspend fun initializeNewUser(userId: UserId, email: String, firstName: String, lastName: String) {
        logger.info("开始初始化新用户数据: userId={}, email={}", userId.value, email)
        
        transactionTemplate.execute { _ ->
            try {
            // 1. 初始化用户配置
            initializeUserConfiguration(userId)
            
            // 2. 初始化用户推荐系统
            initializeUserRecommendation(userId)
            
            // 3. 初始化用户知识库
            initializeUserKnowledgeBase(userId, firstName, lastName)
            
                logger.info("用户数据初始化完成: userId={}", userId.value)
            } catch (e: Exception) {
                logger.error("用户数据初始化失败: userId={}", userId.value, e)
                throw e
            }
        }
    }
    

    /**
     * 初始化用户配置
     */
    private suspend fun initializeUserConfiguration(userId: UserId) {
        logger.debug("初始化用户配置: userId={}", userId.value)
        
        try {
            // 创建用户默认配置
            val configId = ConfigId.generate()
            val createConfigCommand = CreateConfigurationCommand(
                configurationId = configId,
                namespace = "user-${userId.value}",
                environment = Environment.PRODUCTION
            )
            
            commandBus.send<CreateConfigurationCommand, Unit>(createConfigCommand)
            logger.debug("用户配置初始化完成: userId={}, configId={}", userId.value, configId.value)
        } catch (e: Exception) {
            logger.error("用户配置初始化失败: userId={}", userId.value, e)
            throw e
        }
    }
    
    /**
     * 初始化用户推荐系统
     */
    private suspend fun initializeUserRecommendation(userId: UserId) {
        logger.debug("初始化用户推荐系统: userId={}", userId.value)
        
        try {
            // 创建用户推荐配置
            val recommendationId = RecommendationId.generate()
            val createRecommendationCommand = CreateRecommendationCommand(
                recommendationId = recommendationId,
                userId = com.lifee.recommendation.domain.valueobjects.UserId.from(userId.value)
            )
            
            commandBus.send<CreateRecommendationCommand, Unit>(createRecommendationCommand)
            logger.debug("用户推荐系统初始化完成: userId={}, recommendationId={}", userId.value, recommendationId.value)
        } catch (e: Exception) {
            logger.error("用户推荐系统初始化失败: userId={}", userId.value, e)
            throw e
        }
    }
    
    /**
     * 初始化用户知识库
     */
    private suspend fun initializeUserKnowledgeBase(userId: UserId, firstName: String = "用户", lastName: String = "") {
        logger.debug("初始化用户知识库: userId={}", userId.value)
        
        try {
            // 创建用户个人知识库
            val knowledgeBaseId = UUID.randomUUID().toString()
            val displayName = if (lastName.isNotBlank()) "$firstName $lastName" else firstName
            val createKnowledgeBaseCommand = CreateKnowledgeBaseCommand(
                knowledgeBaseId = knowledgeBaseId,
                name = "${displayName}的个人知识库",
                description = "个人知识库，用于存储和管理个人知识内容",
                ownerId = userId.value
            )
            
            commandBus.send<CreateKnowledgeBaseCommand, Unit>(createKnowledgeBaseCommand)
            logger.debug("用户知识库初始化完成: userId={}, knowledgeBaseId={}", userId.value, knowledgeBaseId)
        } catch (e: Exception) {
            logger.error("用户知识库初始化失败: userId={}", userId.value, e)
            throw e
        }
    }
}