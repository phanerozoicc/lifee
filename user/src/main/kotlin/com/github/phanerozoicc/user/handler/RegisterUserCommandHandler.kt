package com.github.phanerozoicc.user.handler

import com.github.phanerozoicc.base.domain.DomainEventPublisher
import com.github.phanerozoicc.user.domain.cqrs.CommandHandler
import com.github.phanerozoicc.user.domain.cqrs.CommandResult
import com.github.phanerozoicc.user.domain.cqrs.RegisterUserCommand
import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.model.User
import com.github.phanerozoicc.user.domain.repository.UserRepository
import com.github.phanerozoicc.user.domain.service.UserDomainService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 用户注册命令处理器
 */
@Service
class RegisterUserCommandHandler(
    private val userRepository: UserRepository,
    private val userDomainService: UserDomainService,
    private val domainEventPublisher: DomainEventPublisher
) : CommandHandler<RegisterUserCommand> {

    @Transactional
    override suspend fun handle(command: RegisterUserCommand): CommandResult {
        try {
            // 验证命令
            val validationResult = validate(command)
            if (validationResult is CommandResult.ValidationError) {
                return validationResult
            }

            val email = Email.of(command.email)

            // 验证唯一性
            userDomainService.validateUserUniqueness(email, command.nickname)

            // 创建用户
            val user = User.register(
                email = email,
                plainPassword = command.password,
                nickname = command.nickname,
                ipAddress = command.ipAddress,
                userAgent = command.userAgent
            )

            // 如果提供了姓名信息，更新用户资料
            if (command.firstName != null || command.lastName != null) {
                val updatedProfile = user.getProfile().copy(
                    firstName = command.firstName,
                    lastName = command.lastName
                )
                user.updateProfile(updatedProfile)
            }

            // 保存用户
            val savedUser = userRepository.save(user)

            // 发布领域事件
            savedUser.getDomainEvents().forEach { event ->
                domainEventPublisher.publish(event)
            }
            savedUser.clearDomainEvents()

            return CommandResult.Success(
                message = "用户注册成功",
                data = mapOf("userId" to savedUser.getId().getValue())
            )

        } catch (e: IllegalArgumentException) {
            return CommandResult.Failure(e.message ?: "注册失败", "REGISTRATION_FAILED")
        } catch (e: Exception) {
            return CommandResult.Failure("系统错误，请稍后重试", "SYSTEM_ERROR")
        }
    }

    override fun validate(command: RegisterUserCommand): CommandResult {
        val errors = mutableMapOf<String, MutableList<String>>()

        // 验证邮箱
        if (command.email.isBlank()) {
            errors.getOrPut("email") { mutableListOf() }.add("邮箱不能为空")
        } else {
            try {
                Email.of(command.email)
            } catch (e: IllegalArgumentException) {
                errors.getOrPut("email") { mutableListOf() }.add(e.message ?: "邮箱格式不正确")
            }
        }

        // 验证密码
        if (command.password.isBlank()) {
            errors.getOrPut("password") { mutableListOf() }.add("密码不能为空")
        }

        // 验证昵称
        if (command.nickname.isBlank()) {
            errors.getOrPut("nickname") { mutableListOf() }.add("昵称不能为空")
        }

        // 验证服务条款
        if (!command.acceptTerms) {
            errors.getOrPut("acceptTerms") { mutableListOf() }.add("必须同意服务条款")
        }

        return if (errors.isEmpty()) {
            CommandResult.Success()
        } else {
            CommandResult.ValidationError(errors)
        }
    }
}
