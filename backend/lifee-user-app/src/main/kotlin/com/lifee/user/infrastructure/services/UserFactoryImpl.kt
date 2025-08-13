package com.lifee.user.infrastructure.services

import com.lifee.user.domain.*
import com.lifee.user.domain.services.UserFactory
import com.lifee.user.domain.services.UserIdGenerator
import org.springframework.stereotype.Service

/**
 * 用户工厂实现类
 * 封装用户创建逻辑，包括ID生成
 */
@Service
class UserFactoryImpl(
    private val userIdGenerator: UserIdGenerator
) : UserFactory {
    
    /**
     * 创建新用户
     * 自动生成用户ID，无需外部传入
     */
    override fun createUser(
        email: Email,
        password: Password,
        firstName: String,
        lastName: String
    ): User {
        val userId = userIdGenerator.generateNext()
        return User.create(
            id = userId,
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName
        )
    }
    
    /**
     * 创建用户（指定ID）
     * 用于特殊场景，如数据迁移等
     */
    override fun createUserWithId(
        id: UserId,
        email: Email,
        password: Password,
        firstName: String,
        lastName: String
    ): User {
        return User.create(
            id = id,
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName
        )
    }
}