package com.lifee.user.app.handlers

import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.exceptions.BusinessRuleException
import com.lifee.user.app.commands.ChangePasswordCommand
import com.lifee.user.domain.Password
import com.lifee.user.domain.UserId
import com.lifee.user.domain.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 更改密码命令处理器
 */
@Component
class ChangePasswordCommandHandler(
    private val userRepository: UserRepository,
    private val eventBus: EventBus
) : AsyncCommandHandler<ChangePasswordCommand, Unit> {
    
    private val logger = LoggerFactory.getLogger(ChangePasswordCommandHandler::class.java)
    
    @Transactional
    override suspend fun handle(command: ChangePasswordCommand) {
        logger.info("处理更改密码命令: userId={}", command.userId)
        
        // 查找用户
        val userId = UserId.fromString(command.userId)
        val user = userRepository.findById(userId)
            ?: throw BusinessRuleException("用户不存在: ${command.userId}")
        
        // 验证当前密码
        val currentPasswordValid = user.verifyPassword(command.currentPassword)
        BusinessRuleException.throwIf(
            !currentPasswordValid,
            "当前密码不正确"
        )
        
        // 创建新密码
        val newPassword = Password.fromPlainText(command.newPassword)
        
        // 更改密码
        user.changePassword(newPassword)
        
        // 保存用户
        val savedUser = userRepository.save(user)
        
        // 发布领域事件
        eventBus.publishAll(savedUser.getDomainEvents())
        savedUser.clearDomainEvents()
        
        logger.info("密码更改成功: userId={}", command.userId)
    }
}