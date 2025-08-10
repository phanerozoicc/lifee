package com.lifee.config.app.application.commands.handlers

import com.lifee.common.cqrs.commands.CommandHandler
import com.lifee.config.app.application.commands.*
import com.lifee.config.domain.exceptions.*
import com.lifee.config.domain.repositories.ConfigurationRepository
import com.lifee.config.domain.services.ConfigurationDomainService
import com.lifee.config.domain.services.ConfigValidationService
import com.lifee.config.domain.valueobjects.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 添加配置项命令处理器
 */
@Component
class AddConfigItemCommandHandler(
    private val configurationRepository: ConfigurationRepository,
    private val domainService: ConfigurationDomainService,
    private val validationService: ConfigValidationService
) : CommandHandler<AddConfigItemCommand> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(AddConfigItemCommandHandler::class.java)
    }
    
    @Transactional
    override suspend fun handle(command: AddConfigItemCommand) {
        logger.info("处理添加配置项命令: configurationId={}, key={}, type={}", 
                   command.configurationId, command.key, command.type)
        
        val configuration = configurationRepository.findById(command.configurationId)
            ?: throw ConfigurationNotFoundException(command.configurationId)
        
        val configKey = command.key
        val configValue = command.value
        
        // 使用验证服务进行全面验证
        validationService.validateConfigItem(
            key = configKey,
            value = configValue,
            type = command.type,
            description = command.description,
            isEncrypted = command.isEncrypted
        )
        
        // 验证配置项数量限制
        validationService.validateConfigItemCount(
            currentCount = configuration.getItems().size,
            namespace = configuration.namespace
        )
        
        // 验证是否可以添加配置项
        domainService.validateCanAddItem(configuration, configKey)
        
        // 验证值类型
        domainService.validateValueType(configValue, command.type)
        
        // 添加配置项
        configuration.addItem(
            key = configKey,
            value = configValue,
            type = command.type,
            description = command.description,
            isEncrypted = command.isEncrypted
        )
        
        configurationRepository.save(configuration)
        
        logger.info("成功添加配置项: configurationId={}, key={}", 
                   command.configurationId, command.key.value)
    }
}

/**
 * 更新配置项命令处理器
 */
@Component
class UpdateConfigItemCommandHandler(
    private val configurationRepository: ConfigurationRepository,
    private val domainService: ConfigurationDomainService,
    private val validationService: ConfigValidationService
) : CommandHandler<UpdateConfigItemCommand> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(UpdateConfigItemCommandHandler::class.java)
    }
    
    @Transactional
    override suspend fun handle(command: UpdateConfigItemCommand) {
        logger.info("处理更新配置项命令: configurationId={}, key={}", 
                   command.configurationId, command.key)
        
        val configuration = configurationRepository.findById(command.configurationId)
            ?: throw ConfigurationNotFoundException(command.configurationId)
        
        val configKey = command.key
        val configValue = command.value
        
        // 获取现有配置项
        val existingItem = configuration.getItem(configKey)
            ?: throw ConfigItemNotFoundException(configKey, configuration.environment)
        
        // 使用验证服务进行全面验证
        validationService.validateConfigItem(
            key = configKey,
            value = configValue,
            type = existingItem.type,
            description = existingItem.getDescription(),
            isEncrypted = existingItem.isEncrypted
        )
        
        // 验证值类型
        domainService.validateValueType(configValue, existingItem.type)
        
        // 更新配置项
        configuration.updateItem(
            key = configKey,
            value = configValue
        )
        
        configurationRepository.save(configuration)
        
        logger.info("成功更新配置项: configurationId={}, key={}", 
                   command.configurationId, command.key.value)
    }
}

/**
 * 删除配置项命令处理器
 */
@Component
class RemoveConfigItemCommandHandler(
    private val configurationRepository: ConfigurationRepository,
    private val validationService: ConfigValidationService
) : CommandHandler<RemoveConfigItemCommand> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(RemoveConfigItemCommandHandler::class.java)
    }
    
    @Transactional
    override suspend fun handle(command: RemoveConfigItemCommand) {
        logger.info("处理删除配置项命令: configurationId={}, key={}", 
                   command.configurationId, command.key.value)
        
        val configuration = configurationRepository.findByNamespaceAndEnvironment(
            command.namespace,
            command.environment
        ) ?: throw NamespaceConfigurationNotFoundException(command.namespace, command.environment)
        
        // 验证权限（假设从上下文获取用户ID）
        val userId = "current_user" // 实际应该从安全上下文获取
        validationService.validatePermission(
            userId = userId,
            namespace = configuration.namespace,
            environment = configuration.environment,
            operation = "DELETE"
        )
        
        val configKey = ConfigKey.of(command.key)
        
        // 删除配置项
        configuration.removeItem(configKey)
        
        configurationRepository.save(configuration)
        
        logger.info("成功删除配置项: configurationId={}, key={}", 
                   command.configurationId, command.key.value)
    }
}

/**
 * 批量更新配置项命令处理器
 */
@Component
class BatchUpdateConfigItemsCommandHandler(
    private val configurationRepository: ConfigurationRepository,
    private val domainService: ConfigurationDomainService,
    private val validationService: ConfigValidationService
) : CommandHandler<BatchUpdateConfigItemsCommand> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(BatchUpdateConfigItemsCommandHandler::class.java)
    }
    
    @Transactional
    override suspend fun handle(command: BatchUpdateConfigItemsCommand) {
        logger.info("处理批量更新配置项命令: configurationId={}, 更新数量={}", 
                   command.configurationId, command.updates.size)
        
        val configuration = configurationRepository.findById(command.configurationId)
            ?: throw ConfigurationNotFoundException(command.configurationId)
        
        // 验证批量更新
        validationService.validateBatchUpdate(command.updates)
        
        // 验证权限（假设从上下文获取用户ID）
        val userId = "current_user" // 实际应该从安全上下文获取
        validationService.validatePermission(
            userId = userId,
            namespace = configuration.namespace,
            environment = configuration.environment,
            operation = "BATCH_UPDATE"
        )
        
        // 批量更新配置项
        val updates = command.updates.mapValues { (key, value) ->
            // 获取现有配置项以验证类型
            val existingItem = configuration.getItem(key)
                ?: throw ConfigItemNotFoundException(key, configuration.environment)
            
            // 使用验证服务进行验证
            validationService.validateConfigItem(
                key = key,
                value = value,
                type = existingItem.type,
                description = existingItem.getDescription(),
                isEncrypted = existingItem.isEncrypted
            )
            
            // 验证值类型
            domainService.validateValueType(value, existingItem.type)
            
            value
        }
        
        configuration.batchUpdate(updates)
        
        configurationRepository.save(configuration)
        
        logger.info("成功批量更新配置项: configurationId={}, 更新数量={}", 
                   command.configurationId, updates.size)
    }
}

/**
 * 清空配置命令处理器
 */
@Component
class ClearConfigurationCommandHandler(
    private val configurationRepository: ConfigurationRepository,
    private val validationService: ConfigValidationService
) : CommandHandler<ClearConfigurationCommand> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(ClearConfigurationCommandHandler::class.java)
    }
    
    @Transactional
    override suspend fun handle(command: ClearConfigurationCommand) {
        logger.info("处理清空配置命令: configurationId={}", command.configurationId)
        
        val configuration = configurationRepository.findByNamespaceAndEnvironment(
            command.namespace,
            command.environment
        ) ?: throw NamespaceConfigurationNotFoundException(command.namespace, command.environment)
        
        // 验证权限（假设从上下文获取用户ID）
        val userId = "current_user" // 实际应该从安全上下文获取
        validationService.validatePermission(
            userId = userId,
            namespace = configuration.namespace,
            environment = configuration.environment,
            operation = "DELETE"
        )
        
        // 清空配置
        configuration.clear()
        
        configurationRepository.save(configuration)
        
        logger.info("成功清空配置: configurationId={}", command.configurationId)
    }
}

/**
 * 删除配置命令处理器
 */
@Component
class DeleteConfigurationCommandHandler(
    private val configurationRepository: ConfigurationRepository,
    private val validationService: ConfigValidationService
) : CommandHandler<DeleteConfigurationCommand> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(DeleteConfigurationCommandHandler::class.java)
    }
    
    @Transactional
    override suspend fun handle(command: DeleteConfigurationCommand) {
        logger.info("处理删除配置命令: configurationId={}", command.configurationId)
        
        val configuration = configurationRepository.findByNamespaceAndEnvironment(
            command.namespace,
            command.environment
        ) ?: throw NamespaceConfigurationNotFoundException(command.namespace, command.environment)
        
        // 验证权限（假设从上下文获取用户ID）
        val userId = "current_user" // 实际应该从安全上下文获取
        validationService.validatePermission(
            userId = userId,
            namespace = configuration.namespace,
            environment = configuration.environment,
            operation = "DELETE"
        )
        
        configurationRepository.delete(configuration)
        
        logger.info("成功删除配置: configurationId={}", command.configurationId)
    }
}

/**
 * 复制配置到环境命令处理器
 */
@Component
class CopyConfigurationToEnvironmentCommandHandler(
    private val configurationRepository: ConfigurationRepository,
    private val validationService: ConfigValidationService
) : CommandHandler<CopyConfigurationToEnvironmentCommand> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(CopyConfigurationToEnvironmentCommandHandler::class.java)
    }
    
    @Transactional
    override suspend fun handle(command: CopyConfigurationToEnvironmentCommand) {
        logger.info("处理复制配置命令: sourceNamespace={}, sourceEnvironment={}, targetNamespace={}, targetEnvironment={}", 
                   command.sourceNamespace, command.sourceEnvironment, command.targetNamespace, command.targetEnvironment)
        
        val sourceConfiguration = configurationRepository.findByNamespaceAndEnvironment(
            command.sourceNamespace,
            command.sourceEnvironment
        ) ?: throw NamespaceConfigurationNotFoundException(command.sourceNamespace, command.sourceEnvironment)
        
        // 验证目标命名空间
        validationService.validateNamespace(command.targetNamespace)
        
        // 验证权限（假设从上下文获取用户ID）
        val userId = "current_user" // 实际应该从安全上下文获取
        validationService.validatePermission(
            userId = userId,
            namespace = command.sourceNamespace,
            environment = command.sourceEnvironment,
            operation = "READ"
        )
        validationService.validatePermission(
            userId = userId,
            namespace = command.targetNamespace,
            environment = command.targetEnvironment,
            operation = "CREATE"
        )
        
        val targetEnvironment = Environment.of(command.targetEnvironment)
        
        // 检查目标配置是否已存在
        val existingTarget = configurationRepository.findByNamespaceAndEnvironment(
            command.targetNamespace,
            targetEnvironment
        )
        
        if (existingTarget != null && !command.overwrite) {
            throw ConfigurationAlreadyExistsException(
                command.targetNamespace,
                targetEnvironment
            )
        }
        
        // 复制配置
        val targetConfiguration = sourceConfiguration.copyToEnvironment(
            targetNamespace = command.targetNamespace,
            targetEnvironment = targetEnvironment
        )
        
        // 如果目标配置已存在且允许覆盖，则删除现有配置
        if (existingTarget != null && command.overwrite) {
            configurationRepository.delete(existingTarget)
        }
        
        configurationRepository.save(targetConfiguration)
        
        logger.info("成功复制配置: sourceNamespace={}, sourceEnvironment={}, targetNamespace={}, targetEnvironment={}", 
                   command.sourceNamespace, command.sourceEnvironment, command.targetNamespace, command.targetEnvironment)
    }
}