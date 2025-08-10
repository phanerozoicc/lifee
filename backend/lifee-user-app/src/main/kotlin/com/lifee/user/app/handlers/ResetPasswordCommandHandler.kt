package com.lifee.user.app.handlers

import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.exceptions.BusinessRuleException
import com.lifee.user.app.commands.ResetPasswordCommand
import com.lifee.user.domain.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 重置密码命令处理器
 */
@Component
class ResetPasswordCommandHandler(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val eventBus: EventBus
) : AsyncCommandHandler<ResetPasswordCommand, Unit> {
    
    private val logger = LoggerFactory.getLogger(ResetPasswordCommandHandler::class.java)
    
    @Transactional
    override suspend fun handle(command: ResetPasswordCommand) {
        logger.info("处理重置密码命令: token={}", command.token)
        
        // 查找密码重置令牌
        val resetToken = passwordResetTokenRepository.findByToken(command.token)
            ?: throw BusinessRuleException("无效的密码重置令牌")
        
        // 验证令牌是否有效
        BusinessRuleException.throwIf(
            !resetToken.isValid(),
            "密码重置令牌已过期或已使用"
        )
        
        // 查找用户
        val user = userRepository.findById(resetToken.userId)
            ?: throw BusinessRuleException("用户不存在")
        
        // 检查用户状态
        BusinessRuleException.throwIf(
            user.getStatus() == UserStatus.DELETED,
            "用户账户已被删除"
        )
        
        BusinessRuleException.throwIf(
            user.getStatus() == UserStatus.SUSPENDED,
            "用户账户已被停用"
        )
        
        // 创建新密码
        val newPassword = Password.fromPlainText(command.newPassword)
        
        // 更改用户密码
        user.changePassword(newPassword)
        
        // 保存用户
        userRepository.save(user)
        
        // 标记令牌为已使用
        val usedToken = resetToken.markAsUsed()
        passwordResetTokenRepository.save(usedToken)
        
        // 删除用户的所有其他密码重置令牌
        passwordResetTokenRepository.deleteAllByUserId(user.getId())
        
        // 发布领域事件
        eventBus.publishAll(user.getDomainEvents())
        user.clearDomainEvents()
        
        logger.info("密码重置成功: userId={}", user.getId().value)
    }
}