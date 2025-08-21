package com.github.phanerozoicc.user.domain.factory

import com.github.phanerozoicc.user.domain.event.UserRegisteredEvent
import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.model.Password
import com.github.phanerozoicc.user.domain.model.PasswordSpecification
import com.github.phanerozoicc.user.domain.model.User
import com.github.phanerozoicc.user.domain.model.UserProfile
import com.github.phanerozoicc.user.domain.model.UserStatus
import com.github.phanerozoicc.user.domain.service.UserIdGenerate
import org.springframework.stereotype.Component

@Component
class UserFactory(
    private val userIdGenerate: UserIdGenerate,
    private val passwordSpecification: PasswordSpecification
) {

    /**
     * 创建新用户（注册）
     * @param email 邮箱地址
     * @param plainPassword 明文密码
     * @param nickname 昵称
     * @param ipAddress 注册IP地址
     * @param userAgent 用户代理
     * @return 新用户实例
     */
    fun create(
        email: Email,
        plainPassword: String,
        nickname: String,
        ipAddress: String? = null,
        userAgent: String? = null
    ): User {
            // 验证密码策略
        passwordSpecification.validatePassword(plainPassword)

        val userId = userIdGenerate.generateNext()
        val password = Password.of(plainPassword)
        val profile = UserProfile.of(nickname)
        val status = UserStatus.pending("等待邮箱验证")

        val user = User(
            id = userId,
            email = email,
            password = password,
            profile = profile,
            status = status
        )

        // 发布用户注册事件
        user.recordEvent(
            UserRegisteredEvent(
                userId = userId,
                email = email,
                nickname = nickname,
                ipAddress = ipAddress,
                userAgent = userAgent
            )
        )

        return user
    }
}
