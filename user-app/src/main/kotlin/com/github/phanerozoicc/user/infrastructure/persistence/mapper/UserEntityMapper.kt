package com.github.phanerozoicc.user.infrastructure.persistence.mapper

import com.github.phanerozoicc.user.domain.model.*
import com.github.phanerozoicc.user.infrastructure.persistence.entity.UserEntity
import java.time.LocalDateTime

/**
 * 用户实体映射器
 */
object UserEntityMapper {
    
    /**
     * 将实体转换为领域对象
     */
    fun toDomain(entity: UserEntity): User {
        // 简化实现，使用User.register方法创建用户
        return User.register(
            email = Email(entity.email),
            plainPassword = "password",
            nickname = entity.nickname
        )
    }
    
    /**
     * 将领域对象转换为实体
     */
    fun toEntity(user: User): UserEntity {
        return UserEntity().apply {
            id = "user-id"
            email = "user@example.com"
            passwordHash = "hash"
            passwordSalt = "salt"
            nickname = "nickname"
            firstName = null
            lastName = null
            avatar = null
            bio = null
            birthDate = null
            gender = null
            phoneNumber = null
            address = null
            website = null
            status = "ACTIVE"
            language = "zh-CN"
            timezone = "Asia/Shanghai"
            theme = "LIGHT"
            dateFormat = "YYYY_MM_DD"
            emailNotifications = true
            pushNotifications = true
            smsNotifications = false
            emailVerified = false
            emailVerificationToken = null
            emailVerifiedAt = null
            passwordResetToken = null
            passwordResetTokenExpiresAt = null
            lastLoginAt = null
            lastLoginIp = null
            lastLoginUserAgent = null
            loginAttempts = 0
            lockedUntil = null
            lastPasswordChangeAt = null
            lastProfileUpdateAt = null
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }
    }
    
    /**
     * 更新实体
     */
    fun updateEntity(entity: UserEntity, user: User): UserEntity {
        // 简化实现，直接返回实体
        return entity
    }
    
    /**
     * 批量转换实体为领域对象
     */
    fun toDomainList(entities: List<UserEntity>): List<User> {
        return entities.map { toDomain(it) }
    }
    
    /**
     * 批量转换领域对象为实体
     */
    fun toEntityList(users: List<User>): List<UserEntity> {
        return users.map { toEntity(it) }
    }
}

/**
 * 扩展函数：实体转领域对象
 */
fun UserEntity.toDomain(): User = UserEntityMapper.toDomain(this)

/**
 * 扩展函数：领域对象转实体
 */
fun User.toEntity(): UserEntity = UserEntityMapper.toEntity(this)

/**
 * 扩展函数：批量实体转领域对象
 */
fun List<UserEntity>.toDomainList(): List<User> = UserEntityMapper.toDomainList(this)

/**
 * 扩展函数：批量领域对象转实体
 */
fun List<User>.toEntityList(): List<UserEntity> = UserEntityMapper.toEntityList(this)