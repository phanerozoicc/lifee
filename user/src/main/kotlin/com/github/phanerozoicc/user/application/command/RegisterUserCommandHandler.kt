package com.github.phanerozoicc.user.application.command

import com.github.phanerozoicc.base.command.Command
import com.github.phanerozoicc.base.command.CommandHandler
import com.github.phanerozoicc.base.event.EventBus
import com.github.phanerozoicc.user.domain.event.UserRegisteredEvent
import com.github.phanerozoicc.user.domain.factory.UserFactory
import com.github.phanerozoicc.user.domain.model.*
import com.github.phanerozoicc.user.domain.repository.ActivationTokenRepository
import com.github.phanerozoicc.user.domain.repository.UserRepository
import com.github.phanerozoicc.user.domain.service.UserDomainService
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

/**
 * 用户注册命令
 */
data class RegisterUserCommand(
    val email: String,
    val password: String,
    val nickname: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val acceptTerms: Boolean = true,
    val marketingConsent: Boolean = false,
    val ipAddress: String? = null,
    val userAgent: String? = null,
) : Command()


/**
 * 用户注册命令处理器
 */
@Service
class RegisterUserCommandHandler(
    private val userFactory: UserFactory,
    private val userRepository: UserRepository,
    private val activationTokenRepository: ActivationTokenRepository,
    private val userDomainService: UserDomainService,
    private val eventBus: EventBus
) : CommandHandler<RegisterUserCommand, Unit> {


    private val emailSpecification = EmailSpecification()
    private val passwordSpecification = PasswordSpecification()
    private val userSpecification = UserSpecification()

    override fun handle(command: RegisterUserCommand) {
        // 验证命令
        validate(command)

        val email = Email.of(command.email)
        // 验证唯一性
        userDomainService.validateUserUniqueness(email, command.nickname)


        // 创建用户
        val user = userFactory.create(
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
        runBlocking {
            val savedUser = userRepository.save(user)

            // 生成激活令牌
            val activationToken = ActivationToken.generate(user.id)
            activationTokenRepository.save(activationToken)

            // 之后可以通过saga管理器模式处理后续的业务流程
            // 这里为了简化直接依赖事件机制触发后续步骤并行处理
            // 发布领域事件
            savedUser.getDomainEvents().forEach { event ->
                eventBus.publish(
                    if(event is UserRegisteredEvent) {
                        event.copy(
                            activationToken = activationToken.value
                        )
                    } else {
                        event
                    }
                )
            }
            savedUser.clearDomainEvents()
        }

    }

    private fun validate(command: RegisterUserCommand) {
        // 验证邮箱
        if (command.email.isBlank()) {
            throw IllegalArgumentException("邮箱不能为空")
        }
        val email = Email.of(command.email)
        emailSpecification.validateEmail(email)

        // 验证密码
        if (command.password.isBlank()) {
            throw IllegalArgumentException("密码不能为空")
        }
        passwordSpecification.validatePassword(command.password)

        // 验证昵称
        if (command.nickname.isBlank()) {
            throw IllegalArgumentException("昵称不能为空")
        }

        // 验证服务条款
        if (!command.acceptTerms) {
            throw IllegalArgumentException("必须同意服务条款")
        }
    }

}
