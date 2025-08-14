package com.github.phanerozoicc.user.application.command

import com.github.phanerozoicc.base.event.DomainEventPublisher
import com.github.phanerozoicc.user.domain.model.UserId
import com.github.phanerozoicc.user.domain.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * 修改密码命令
 */
data class ChangePasswordCommand(
    override val commandId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val userId: UserId,
    override val ipAddress: String? = null,
    override val userAgent: String? = null,
    val oldPassword: String,
    val newPassword: String,
    val confirmPassword: String
) : UserCommand()


/**
 * 修改密码命令处理器
 */
@Service
class ChangePasswordCommandHandler(
    private val userRepository: UserRepository,
    private val domainEventPublisher: DomainEventPublisher
) : CommandHandler<ChangePasswordCommand> {

    override suspend fun handle(command: ChangePasswordCommand): CommandResult {
        try {
            // 验证命令
            val validationResult = validate(command)
            if (validationResult is CommandResult.ValidationError) {
                return validationResult
            }

            val user = userRepository.findById(command.userId)
                ?: return CommandResult.Failure("用户不存在", "USER_NOT_FOUND")

            // 修改密码
            user.changePassword(
                oldPassword = command.oldPassword,
                newPassword = command.newPassword,
                ipAddress = command.ipAddress
            )

            // 保存用户
            val savedUser = userRepository.save(user)

            // 发布领域事件
            savedUser.getDomainEvents().forEach { event ->
                domainEventPublisher.publish(event)
            }
            savedUser.clearDomainEvents()

            return CommandResult.Success("密码修改成功")

        } catch (e: IllegalArgumentException) {
            return CommandResult.Failure(e.message ?: "密码修改失败", "PASSWORD_CHANGE_FAILED")
        } catch (e: Exception) {
            return CommandResult.Failure("系统错误，请稍后重试", "SYSTEM_ERROR")
        }
    }

    override fun validate(command: ChangePasswordCommand): CommandResult {
        val errors = mutableMapOf<String, MutableList<String>>()

        if (command.oldPassword.isBlank()) {
            errors.getOrPut("oldPassword") { mutableListOf() }.add("原密码不能为空")
        }

        if (command.newPassword.isBlank()) {
            errors.getOrPut("newPassword") { mutableListOf() }.add("新密码不能为空")
        }

        if (command.newPassword != command.confirmPassword) {
            errors.getOrPut("confirmPassword") { mutableListOf() }.add("确认密码与新密码不一致")
        }

        return if (errors.isEmpty()) {
            CommandResult.Success()
        } else {
            CommandResult.ValidationError(errors)
        }
    }
}
