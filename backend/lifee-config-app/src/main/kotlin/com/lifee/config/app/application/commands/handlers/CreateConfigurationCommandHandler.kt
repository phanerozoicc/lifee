package com.lifee.config.app.application.commands.handlers

import com.lifee.common.cqrs.commands.CommandHandler
import com.lifee.config.app.application.commands.CreateConfigurationCommand
import com.lifee.config.domain.aggregates.Configuration
import com.lifee.config.domain.exceptions.ConfigurationAlreadyExistsException
import com.lifee.config.domain.repositories.ConfigurationRepository
import com.lifee.config.domain.services.ConfigValidationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 创建配置命令处理器
 */
@Component
class CreateConfigurationCommandHandler(
    private val configurationRepository: ConfigurationRepository,
    private val validationService: ConfigValidationService
) : CommandHandler<CreateConfigurationCommand> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(CreateConfigurationCommandHandler::class.java)
    }
    
    @Transactional
    override suspend fun handle(command: CreateConfigurationCommand) {
        logger.info("处理创建配置命令: namespace={}, environment={}", 
                   command.namespace, command.environment)
        
        // 验证命名空间
        validationService.validateNamespace(command.namespace)
        
        // 验证权限（假设从上下文获取用户ID）
        val userId = "current_user" // 实际应该从安全上下文获取
        validationService.validatePermission(
            userId = userId,
            namespace = command.namespace,
            environment = command.environment,
            operation = "CREATE"
        )
        
        // 检查配置是否已存在
        val existingConfiguration = configurationRepository.findByNamespaceAndEnvironment(
            command.namespace,
            command.environment
        )
        
        if (existingConfiguration != null) {
            throw ConfigurationAlreadyExistsException(
                command.namespace,
                command.environment
            )
        }
        
        // 创建新配置
        val configuration = Configuration.create(
            configurationId = command.configurationId,
            namespace = command.namespace,
            environment = command.environment
        )
        
        // 保存配置
        configurationRepository.save(configuration)
        
        logger.info("成功创建配置: configurationId={}, namespace={}, environment={}", 
                   command.configurationId, command.namespace, command.environment)
    }
}

/**
 * 配置已存在异常
 */
class ConfigurationAlreadyExistsException(
    namespace: String,
    environment: com.lifee.config.domain.valueobjects.Environment
) : RuntimeException("配置已存在: 命名空间 '$namespace'，环境 '$environment'")