package com.lifee.user.app.handlers

import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.exceptions.BusinessRuleException
import com.lifee.user.app.commands.RegisterUserCommand
import com.lifee.user.domain.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

/**
 * 用户注册命令处理器
 */
@Component
class RegisterUserCommandHandler(
    private val userRepository: UserRepository,
    private val activationTokenRepository: ActivationTokenRepository,
    private val eventBus: EventBus,
    private val transactionTemplate: TransactionTemplate
) : AsyncCommandHandler<RegisterUserCommand, Unit> {
    
    private val logger = LoggerFactory.getLogger(RegisterUserCommandHandler::class.java)
    
    override suspend fun handle(command: RegisterUserCommand) {
        logger.info("处理用户注册命令: {}", command.email)
        
        transactionTemplate.execute { _ ->
            // 验证邮箱是否已存在
        val email = Email.of(command.email)
        val emailExists = userRepository.existsByEmail(email)
        
        BusinessRuleException.throwIf(
            emailExists,
            "邮箱 ${command.email} 已被注册"
        )
        
        // 创建密码
        val password = Password.fromPlainText(command.password)
        
        // 创建用户
        val user = User.create(
            email = email,
            password = password,
            firstName = command.firstName,
            lastName = command.lastName
        )
        
        // 保存用户
        val savedUser = userRepository.save(user)
        
        // 生成激活令牌
        val activationToken = ActivationToken.generate(savedUser.getId())
        activationTokenRepository.save(activationToken)
        
        // 发布领域事件（包含激活令牌信息）
        val domainEvents = savedUser.getDomainEvents().map { event ->
            if (event is UserRegisteredEvent) {
                UserRegisteredEvent(
                    userId = event.userId,
                    email = event.email,
                    firstName = event.firstName,
                    lastName = event.lastName,
                    registeredAt = event.registeredAt,
                    activationToken = activationToken.value
                )
            } else {
                event
            }
        }
        eventBus.publishAll(domainEvents)
        savedUser.clearDomainEvents()
        
            logger.info("用户注册成功: userId={}, email={}", savedUser.getId(), command.email)
        }
    }
}