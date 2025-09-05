package com.github.phanerozoicc.user.application.command

import com.fasterxml.jackson.annotation.JsonFormat
import com.github.phanerozoicc.base.command.Command
import com.github.phanerozoicc.base.command.CommandHandler
import com.github.phanerozoicc.base.event.EventBus
import com.github.phanerozoicc.user.application.service.JwtService
import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.model.User
import com.github.phanerozoicc.user.domain.model.UserId
import com.github.phanerozoicc.user.domain.repository.UserRepository
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime


/**
 * 用户登录命令
 */
data class LoginUserCommand(
    val email: String,
    val password: String,
    val rememberMe: Boolean = false,
    val sessionId: String? = null,
    val userId: UserId? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null,
) : Command()


/**
 * 用户登录命令处理器
 */
@Service
class LoginUserCommandHandler(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val eventBus: EventBus,
    private val transitionTemplate: TransactionTemplate,
) : CommandHandler<LoginUserCommand, LoginResponse> {

    companion object: KLogging()

    override fun handle(command: LoginUserCommand): LoginResponse {
        try {
            // 验证命令
            validate(command)

            val email = Email.of(command.email)
            val user = userRepository.findByEmail(email)
                ?: throw IllegalStateException("用户不存在")

            // 执行登录
            user.login(
                plainPassword = command.password,
                ipAddress = command.ipAddress,
                userAgent = command.userAgent,
                sessionId = command.sessionId
            )

            // 保存用户状态

            val savedUser = transitionTemplate.execute {
                val savedUser = runBlocking {
                    userRepository.save(user)
                }
                eventBus.publishAll(savedUser.getDomainEvents())
                savedUser.clearDomainEvents()
                savedUser
            }
            // 生成jwt令牌
            // 生成访问令牌
            val accessToken = jwtService.generateAccessToken(
                user,
                rememberMe = command.rememberMe,
                sessionId = command.sessionId
            )
            // 生成刷新令牌
            val refreshToken = jwtService.generateRefreshToken(
                user,
                rememberMe = command.rememberMe,
                sessionId = command.sessionId,
            )


            // 记录登录日志



            return LoginResponse(
                user = UserProfileDTO.fromDomain(savedUser!!),
                accessToken = accessToken,
                refreshToken = refreshToken
            )


        } catch (e: Exception) {
            logger.error("用户登录失败: ${e.message}", e)
            throw e
        }
    }

    fun validate(command: LoginUserCommand) {
        if (command.email.isBlank()) {
            throw IllegalArgumentException("邮箱不能为空")
        }
        if (command.password.isBlank()) {
            throw IllegalArgumentException("密码不能为空")
        }
    }
}



/**
 * 登录响应
 */
data class LoginResponse(
    val user: UserProfileDTO,
    val accessToken: String,
    val refreshToken: String
)

data class UserProfileDTO(
    val id: String,
    val email: String,
    val nickname: String,
    val avatarUrl: String?,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val lastLoginAt: LocalDateTime?
) {
    companion object {
        fun fromDomain(user: User): UserProfileDTO {
            return UserProfileDTO(
                id = user.id.value,
                email = user.getEmail().value,
                nickname = user.getProfile().nickname,
                avatarUrl = user.getProfile().avatar,
                lastLoginAt = user.getLastLoginAt()
            )
        }
    }
}
