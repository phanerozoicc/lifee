package com.lifee.recommendation.app.configuration

import com.lifee.common.cqrs.commands.CommandBus
import com.lifee.common.cqrs.queries.QueryBus
import com.lifee.recommendation.app.application.commands.handlers.*
import com.lifee.recommendation.app.application.queries.handlers.*
import com.lifee.recommendation.domain.repositories.RecommendationRepository
import com.lifee.recommendation.domain.services.RecommendationDomainService
import com.lifee.recommendation.domain.services.RecommendationValidationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 推荐模块配置
 */
@Configuration
class RecommendationConfiguration {
    
    /**
     * 推荐领域服务
     */
    @Bean
    fun recommendationDomainService(): RecommendationDomainService {
        return RecommendationDomainService()
    }
    
    /**
     * 创建推荐命令处理器
     */
    @Bean
    fun createRecommendationCommandHandler(
        repository: RecommendationRepository
    ): CreateRecommendationCommandHandler {
        return CreateRecommendationCommandHandler(repository)
    }
    
    /**
     * 推荐验证服务
     */
    @Bean
    fun recommendationValidationService(): RecommendationValidationService {
        return RecommendationValidationService()
    }
    
    /**
     * 添加推荐项命令处理器
     */
    @Bean
    fun addRecommendationItemCommandHandler(
        repository: RecommendationRepository,
        domainService: RecommendationDomainService,
        validationService: RecommendationValidationService
    ): AddRecommendationItemCommandHandler {
        return AddRecommendationItemCommandHandler(repository, domainService, validationService)
    }
    
    /**
     * 更新推荐分数命令处理器
     */
    @Bean
    fun updateRecommendationScoreCommandHandler(
        repository: RecommendationRepository,
        validationService: RecommendationValidationService
    ): UpdateRecommendationScoreCommandHandler {
        return UpdateRecommendationScoreCommandHandler(repository, validationService)
    }
    
    /**
     * 生成推荐命令处理器
     */
    @Bean
    fun generateRecommendationCommandHandler(
        repository: RecommendationRepository,
        domainService: RecommendationDomainService,
        validationService: RecommendationValidationService
    ): GenerateRecommendationCommandHandler {
        return GenerateRecommendationCommandHandler(repository, domainService, validationService)
    }
    
    /**
     * 获取推荐查询处理器
     */
    @Bean
    fun getRecommendationQueryHandler(
        repository: RecommendationRepository
    ): GetRecommendationQueryHandler {
        return GetRecommendationQueryHandler(repository)
    }
    
    /**
     * 获取用户推荐查询处理器
     */
    @Bean
    fun getUserRecommendationQueryHandler(
        repository: RecommendationRepository
    ): GetUserRecommendationQueryHandler {
        return GetUserRecommendationQueryHandler(repository)
    }
    
    /**
     * 注册命令处理器
     */
    @Bean
    fun registerCommandHandlers(
        commandBus: CommandBus,
        createHandler: CreateRecommendationCommandHandler,
        addItemHandler: AddRecommendationItemCommandHandler,
        updateScoreHandler: UpdateRecommendationScoreCommandHandler,
        generateHandler: GenerateRecommendationCommandHandler
    ): CommandBus {
        commandBus.registerHandler(createHandler)
        commandBus.registerHandler(addItemHandler)
        commandBus.registerHandler(updateScoreHandler)
        commandBus.registerHandler(generateHandler)
        return commandBus
    }
    
    /**
     * 注册查询处理器
     */
    @Bean
    fun registerQueryHandlers(
        queryBus: QueryBus,
        getRecommendationHandler: GetRecommendationQueryHandler,
        getUserRecommendationHandler: GetUserRecommendationQueryHandler
    ): QueryBus {
        queryBus.registerHandler(getRecommendationHandler)
        queryBus.registerHandler(getUserRecommendationHandler)
        return queryBus
    }
}