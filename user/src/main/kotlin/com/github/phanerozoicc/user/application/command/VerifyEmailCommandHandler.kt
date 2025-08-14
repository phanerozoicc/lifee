package com.github.phanerozoicc.user.application.command

import com.github.phanerozoicc.base.event.DomainEventPublisher
import com.github.phanerozoicc.user.domain.model.UserId
import com.github.phanerozoicc.user.domain.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime


/**
 * 验证邮箱命令
 */
data class VerifyEmailCommand(
    override val commandId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val userId: UserId,
    override val ipAddress: String? = null,
    override val userAgent: String? = null,
    val verificationToken: String
) : UserCommand()

/**
 * 验证邮箱命令处理器
 */
@Service
class VerifyEmailCommandHandler(
    private val userRepository: UserRepository,
    private val domainEventPublisher: DomainEventPublisher
) : CommandHandler<VerifyEmailCommand> {

    override suspend fun handle(command: VerifyEmailCommand): CommandResult {
        try {
            val user = userRepository.findById(command.userId)
                ?: return CommandResult.Failure("用户不存在", "USER_NOT_FOUND")

            // 验证邮箱
            user.verifyEmail(command.verificationToken)

            // 保存用户
            val savedUser = userRepository.save(user)

            // 发布领域事件
            savedUser.getDomainEvents().forEach { event ->
                domainEventPublisher.publish(event)
            }
            savedUser.clearDomainEvents()

            return CommandResult.Success("邮箱验证成功")

        } catch (e: IllegalArgumentException) {
            return CommandResult.Failure(e.message ?: "邮箱验证失败", "EMAIL_VERIFICATION_FAILED")
        } catch (e: Exception) {
            return CommandResult.Failure("系统错误，请稍后重试", "SYSTEM_ERROR")
        }
    }

    override fun validate(command: VerifyEmailCommand): CommandResult {
        val errors = mutableMapOf<String, MutableList<String>>()

        if (command.verificationToken.isBlank()) {
            errors.getOrPut("verificationToken") { mutableListOf() }.add("验证令牌不能为空")
        }

        return if (errors.isEmpty()) {
            CommandResult.Success()
        } else {
            CommandResult.ValidationError(errors)
        }
    }
}
