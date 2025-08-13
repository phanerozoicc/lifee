package com.github.phanerozoicc.user.application.command

import com.github.phanerozoicc.base.command.CommandHandler
import com.github.phanerozoicc.base.domain.DomainEventPublisher
import com.github.phanerozoicc.user.domain.event.UserRegistered
import com.github.phanerozoicc.user.domain.factory.UserFactory
import com.github.phanerozoicc.user.domain.model.*
import com.github.phanerozoicc.user.domain.repository.UserRepository
import com.github.phanerozoicc.user.domain.model.ActivationToken
import com.github.phanerozoicc.user.domain.repository.ActivationTokenRepository
import com.github.phanerozoicc.user.domain.service.UserDomainService
import org.springframework.stereotype.Service

/**
 * 用户注册命令
 */
data class RegisterUserCommand(
    override val userId: UserId? = null,
    override val ipAddress: String? = null,
    override val userAgent: String? = null,
    val email: String,
    val password: String,
    val nickname: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val acceptTerms: Boolean = true,
    val marketingConsent: Boolean = false
) : UserCommand()


/**
 * 用户注册命令处理器
 */
@Service
class RegisterUserCommandHandler(
    private val userFactory: UserFactory,
    private val userRepository: UserRepository,
    private val activationTokenRepository: ActivationTokenRepository,
    private val userDomainService: UserDomainService,
    private val domainEventPublisher: DomainEventPublisher
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
        val savedUser = userRepository.save(user)

        // 生成激活令牌
        val activationToken = ActivationToken.generate(user.id)
        activationTokenRepository.save(activationToken)

        // 发布领域事件
        savedUser.getDomainEvents().forEach { event ->
            domainEventPublisher.publish(
                if(event is UserRegistered) {
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
