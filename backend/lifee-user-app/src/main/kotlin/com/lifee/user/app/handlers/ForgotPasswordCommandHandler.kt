package com.lifee.user.app.handlers

import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.common.exceptions.BusinessRuleException
import com.lifee.user.app.commands.ForgotPasswordCommand
import com.lifee.user.domain.*
import com.lifee.user.domain.services.EmailService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

/**
 * 忘记密码命令处理器
 */
@Component
class ForgotPasswordCommandHandler(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val emailService: EmailService,
    private val transactionTemplate: TransactionTemplate
) : AsyncCommandHandler<ForgotPasswordCommand, Unit> {
    
    private val logger = LoggerFactory.getLogger(ForgotPasswordCommandHandler::class.java)
    
    override suspend fun handle(command: ForgotPasswordCommand) {
        logger.info("处理忘记密码命令: email={}", command.email)
        
        val email = Email.of(command.email)
        
        // 查找用户
        val user = userRepository.findByEmail(email)
            ?: throw BusinessRuleException("邮箱 ${command.email} 未注册")
        
        // 检查用户状态
        BusinessRuleException.throwIf(
            user.getStatus() == UserStatus.DELETED,
            "用户账户已被删除"
        )
        
        BusinessRuleException.throwIf(
            user.getStatus() == UserStatus.SUSPENDED,
            "用户账户已被停用"
        )
        
        // 删除用户现有的密码重置令牌
        passwordResetTokenRepository.deleteAllByUserId(user.getId())
        
        // 生成新的密码重置令牌
        val resetToken = PasswordResetToken.generate(user.getId())
        passwordResetTokenRepository.save(resetToken)
        
        // 发送密码重置邮件
        emailService.sendPasswordResetEmail(
            userId = user.getId(),
            email = user.getEmail(),
            firstName = user.getProfile().firstName,
            lastName = user.getProfile().lastName,
            resetToken = resetToken.value
        )
        
        logger.info("密码重置令牌已生成并发送邮件: userId={}, email={}", user.getId().value, command.email)
    }
}