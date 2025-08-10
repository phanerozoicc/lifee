package com.lifee.user.app.handlers

import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.exceptions.BusinessRuleException
import com.lifee.user.app.commands.ActivateUserCommand
import com.lifee.user.domain.UserId
import com.lifee.user.domain.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 激活用户命令处理器
 */
@Component
class ActivateUserCommandHandler(
    private val userRepository: UserRepository,
    private val eventBus: EventBus
) : AsyncCommandHandler<ActivateUserCommand, Unit> {
    
    private val logger = LoggerFactory.getLogger(ActivateUserCommandHandler::class.java)
    
    @Transactional
    override suspend fun handle(command: ActivateUserCommand) {
        logger.info("处理用户激活命令: userId={}", command.userId)
        
        // 查找用户
        val userId = UserId.fromString(command.userId)
        val user = userRepository.findById(userId)
            ?: throw BusinessRuleException("用户不存在: ${command.userId}")
        
        // 激活用户
        user.activate()
        
        // 保存用户
        val savedUser = userRepository.save(user)
        
        // 发布领域事件
        eventBus.publishAll(savedUser.getDomainEvents())
        savedUser.clearDomainEvents()
        
        logger.info("用户激活成功: userId={}", command.userId)
    }
}