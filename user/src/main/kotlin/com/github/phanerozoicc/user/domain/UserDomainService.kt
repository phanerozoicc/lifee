package com.github.phanerozoicc.user.domain

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * 用户域服务
 */
@Service
class UserDomainService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    
    /**
     * 创建新用户
     */
    fun createUser(
        username: Username,
        email: Email,
        rawPassword: String,
        profile: UserProfile
    ): User {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw UserAlreadyExistsException("Username '${username.value}' already exists")
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(email)) {
            throw UserAlreadyExistsException("Email '${email.value}' already exists")
        }
        
        // 加密密码
        val passwordHash = PasswordHash(passwordEncoder.encode(rawPassword))
        
        // 创建用户
        return User.create(username, email, passwordHash, profile)
    }
    
    /**
     * 验证用户密码
     */
    fun verifyPassword(user: User, rawPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash().value)
    }
    
    /**
     * 更改用户密码
     */
    fun changePassword(user: User, newRawPassword: String) {
        val newPasswordHash = PasswordHash(passwordEncoder.encode(newRawPassword))
        user.changePassword(newPasswordHash)
    }
    
    /**
     * 检查用户名可用性
     */
    fun isUsernameAvailable(username: Username): Boolean {
        return !userRepository.existsByUsername(username)
    }
    
    /**
     * 检查邮箱可用性
     */
    fun isEmailAvailable(email: Email): Boolean {
        return !userRepository.existsByEmail(email)
    }
}

/**
 * 用户已存在异常
 */
class UserAlreadyExistsException(message: String) : RuntimeException(message)

/**
 * 用户未找到异常
 */
class UserNotFoundException(message: String) : RuntimeException(message)

/**
 * 无效密码异常
 */
class InvalidPasswordException(message: String) : RuntimeException(message)