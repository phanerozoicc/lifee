package com.github.phanerozoicc.user.application.command

import com.github.phanerozoicc.base.event.DomainEventPublisher
import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.model.UserId
import com.github.phanerozoicc.user.domain.repository.UserRepository
import org.springframework.stereotype.Service


/**
 * 用户登录命令
 */
data class LoginUserCommand(
    override val userId: UserId? = null,
    override val ipAddress: String? = null,
    override val userAgent: String? = null,
    val email: String,
    val password: String,
    val rememberMe: Boolean = false,
    val sessionId: String? = null
) : UserCommand()


/**
 * 用户登录命令处理器
 */
@Service
class LoginUserCommandHandler(
    private val userRepository: UserRepository,
    private val domainEventPublisher: DomainEventPublisher
) : CommandHandler<LoginUserCommand> {

    override suspend fun handle(command: LoginUserCommand): CommandResult {
        try {
            // 验证命令
            val validationResult = validate(command)
            if (validationResult is CommandResult.ValidationError) {
                return validationResult
            }

            val email = Email.of(command.email)
            val user = userRepository.findByEmail(email)
                ?: return CommandResult.Failure("用户名或密码错误", "LOGIN_FAILED")

            // 执行登录
            user.login(
                plainPassword = command.password,
                ipAddress = command.ipAddress,
                userAgent = command.userAgent,
                sessionId = command.sessionId
            )

            // 保存用户状态
            val savedUser = userRepository.save(user)

            // 发布领域事件
            savedUser.getDomainEvents().forEach { event ->
                domainEventPublisher.publish(event)
            }
            savedUser.clearDomainEvents()

            return CommandResult.Success(
                message = "登录成功",
                data = mapOf(
                    "userId" to savedUser.id,
                    "sessionId" to command.sessionId
                )
            )

        } catch (e: IllegalArgumentException) {
            return CommandResult.Failure(e.message ?: "登录失败", "LOGIN_FAILED")
        } catch (e: IllegalStateException) {
            return CommandResult.Failure(e.message ?: "账户状态异常", "ACCOUNT_STATUS_ERROR")
        } catch (e: Exception) {
            return CommandResult.Failure("系统错误，请稍后重试", "SYSTEM_ERROR")
        }
    }

    override fun validate(command: LoginUserCommand): CommandResult {
        val errors = mutableMapOf<String, MutableList<String>>()

        if (command.email.isBlank()) {
            errors.getOrPut("email") { mutableListOf() }.add("邮箱不能为空")
        }

        if (command.password.isBlank()) {
            errors.getOrPut("password") { mutableListOf() }.add("密码不能为空")
        }

        return if (errors.isEmpty()) {
            CommandResult.Success()
        } else {
            CommandResult.ValidationError(errors)
        }
    }
}
