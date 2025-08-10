package com.lifee.recommendation.app.application.commands.handlers

import com.lifee.common.cqrs.commands.CommandHandler
import com.lifee.recommendation.app.application.commands.AddRecommendationItemCommand
import com.lifee.recommendation.domain.entities.RecommendationItem
import com.lifee.recommendation.domain.exceptions.RecommendationNotFoundException
import com.lifee.recommendation.domain.repositories.RecommendationRepository
import com.lifee.recommendation.domain.services.RecommendationDomainService
import com.lifee.recommendation.domain.services.RecommendationValidationService
import com.lifee.recommendation.domain.valueobjects.RecommendationScore
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 添加推荐项命令处理器
 */
@Component
class AddRecommendationItemCommandHandler(
    private val recommendationRepository: RecommendationRepository,
    private val recommendationDomainService: RecommendationDomainService,
    private val recommendationValidationService: RecommendationValidationService
) : CommandHandler<AddRecommendationItemCommand> {
    
    private val logger = LoggerFactory.getLogger(AddRecommendationItemCommandHandler::class.java)
    
    @Transactional
    override suspend fun handle(command: AddRecommendationItemCommand) {
        logger.debug("开始处理添加推荐项命令: recommendationId={}, contentId={}, score={}, type={}", 
            command.recommendationId, command.contentId, command.score, command.type)
        
        // 查找推荐
        val recommendation = recommendationRepository.findById(command.recommendationId)
            ?: throw RecommendationNotFoundException(command.recommendationId)
        
        // 创建推荐项
        val recommendationItem = RecommendationItem(
            contentId = command.contentId,
            score = RecommendationScore.of(command.score),
            type = command.type,
            reason = command.reason
        )
        
        // 使用验证服务进行业务规则验证
        recommendationValidationService.validateRecommendationItem(recommendation, recommendationItem)
        
        // 验证是否可以添加（保留原有的领域服务验证）
        if (!recommendationDomainService.canAddItem(recommendation, recommendationItem)) {
            throw IllegalArgumentException("无法添加推荐项: ${command.contentId}")
        }
        
        // 添加推荐项
        recommendation.addItem(recommendationItem)
        
        // 保存推荐
        recommendationRepository.save(recommendation)
        
        logger.info("成功添加推荐项: recommendationId={}, contentId={}, score={}, itemCount={}", 
            command.recommendationId, command.contentId, command.score, recommendation.getItemCount())
    }
}