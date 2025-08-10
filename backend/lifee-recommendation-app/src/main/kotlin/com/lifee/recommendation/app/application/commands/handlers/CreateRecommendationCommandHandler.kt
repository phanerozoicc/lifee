package com.lifee.recommendation.app.application.commands.handlers

import com.lifee.common.cqrs.commands.CommandHandler
import com.lifee.recommendation.app.application.commands.CreateRecommendationCommand
import com.lifee.recommendation.domain.aggregates.Recommendation
import com.lifee.recommendation.domain.repositories.RecommendationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 创建推荐命令处理器
 */
@Component
class CreateRecommendationCommandHandler(
    private val recommendationRepository: RecommendationRepository
) : CommandHandler<CreateRecommendationCommand> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(CreateRecommendationCommandHandler::class.java)
    }
    
    @Transactional
    override suspend fun handle(command: CreateRecommendationCommand) {
        logger.info("处理创建推荐命令: userId={}, recommendationId={}", 
                   command.userId, command.recommendationId)
        
        try {
            // 检查用户是否已有推荐
            if (recommendationRepository.existsByUserId(command.userId)) {
                logger.warn("用户已存在推荐: userId={}", command.userId)
                throw IllegalArgumentException("用户已存在推荐: ${command.userId}")
            }
            
            // 创建新推荐
            val recommendation = Recommendation.create(
                recommendationId = command.recommendationId,
                userId = command.userId
            )
            
            // 保存推荐
            recommendationRepository.save(recommendation)
            
            logger.info("成功创建推荐: userId={}, recommendationId={}", 
                       command.userId, command.recommendationId)
        } catch (e: Exception) {
            logger.error("创建推荐失败: userId={}, error={}", command.userId, e.message, e)
            throw e
        }
    }
}