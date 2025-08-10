package com.lifee.recommendation.app.application.commands.handlers

import com.lifee.common.cqrs.commands.CommandHandler
import com.lifee.recommendation.app.application.commands.UpdateRecommendationScoreCommand
import com.lifee.recommendation.domain.exceptions.RecommendationNotFoundException
import com.lifee.recommendation.domain.repositories.RecommendationRepository
import com.lifee.recommendation.domain.services.RecommendationValidationService
import com.lifee.recommendation.domain.valueobjects.RecommendationScore
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 更新推荐分数命令处理器
 */
@Component
class UpdateRecommendationScoreCommandHandler(
    private val recommendationRepository: RecommendationRepository,
    private val recommendationValidationService: RecommendationValidationService
) : CommandHandler<UpdateRecommendationScoreCommand> {
    
    private val logger = LoggerFactory.getLogger(UpdateRecommendationScoreCommandHandler::class.java)
    
    @Transactional
    override suspend fun handle(command: UpdateRecommendationScoreCommand) {
        logger.debug("开始处理更新推荐分数命令: recommendationId={}, contentId={}, newScore={}", 
            command.recommendationId, command.contentId, command.newScore)
        
        // 查找推荐
        val recommendation = recommendationRepository.findById(command.recommendationId)
            ?: throw RecommendationNotFoundException(command.recommendationId)
        
        // 获取当前分数
        val currentItem = recommendation.getItems().find { it.getContentId() == command.contentId }
            ?: throw IllegalArgumentException("推荐项不存在: ${command.contentId}")
        
        val oldScore = currentItem.getScore().value
        
        // 验证分数更新
        recommendationValidationService.validateScoreUpdate(
            oldScore = oldScore,
            newScore = command.newScore,
            reason = null // 可以扩展命令以包含更新原因
        )
        
        // 更新分数
        val newScore = RecommendationScore.of(command.newScore)
        recommendation.updateItemScore(command.contentId, newScore)
        
        // 保存推荐
        recommendationRepository.save(recommendation)
        
        logger.info("成功更新推荐分数: recommendationId={}, contentId={}, oldScore={}, newScore={}", 
            command.recommendationId, command.contentId, oldScore, command.newScore)
    }
}