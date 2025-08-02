package com.github.phanerozoicc.user.application

import com.github.phanerozoicc.user.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 用户应用服务
 */
@Service
@Transactional
class UserApplicationService(
    private val userRepository: UserRepository,
    private val userDomainService: UserDomainService
) {
    
    /**
     * 注册新用户
     */
    fun registerUser(command: RegisterUserCommand): User {
        val username = Username(command.username)
        val email = Email(command.email)
        val profile = UserProfile(
            displayName = command.displayName,
            avatar = command.avatar,
            bio = command.bio
        )
        
        val user = userDomainService.createUser(username, email, command.password, profile)
        val savedUser = userRepository.save(user)
        
        return UserDto.from(savedUser)
    }
    
    /**
     * 用户登录验证
     */
    @Transactional(readOnly = true)
    fun authenticateUser(command: AuthenticateUserCommand): UserDto? {
        val user = userRepository.findByUsername(Username(command.username))
            ?: return null
            
        if (!user.isActive()) {
            throw UserNotActiveException("User is not active")
        }
        
        return if (userDomainService.verifyPassword(user, command.password)) {
            UserDto.from(user)
        } else {
            null
        }
    }
    
    /**
     * 获取用户信息
     */
    @Transactional(readOnly = true)
    fun getUserById(userId: String): User {
        return userRepository.findById(UserId.from(userId))
            ?: throw UserNotFoundException("User not found: $userId")
    }
    
    /**
     * 根据用户名获取用户信息
     */
    @Transactional(readOnly = true)
    fun getUserByUsername(username: String): User {
        return userRepository.findByUsername(Username(username))
            ?: throw UserNotFoundException("User not found: $username")
    }
    
    /**
     * 更新用户资料
     */
    fun updateUserProfile(command: UpdateUserProfileCommand): User {
        val user = userRepository.findById(UserId.from(command.userId))
            ?: throw UserNotFoundException("User not found: ${command.userId}")
            
        val newProfile = UserProfile(
            displayName = command.displayName,
            avatar = command.avatar,
            bio = command.bio
        )
        
        user.updateProfile(newProfile)
        return userRepository.save(user)
    }
    
    /**
     * 更改密码
     */
    fun changePassword(command: ChangePasswordCommand) {
        val user = userRepository.findById(UserId.from(command.userId))
            ?: throw UserNotFoundException("User not found: ${command.userId}")
            
        if (!userDomainService.verifyPassword(user, command.currentPassword)) {
            throw InvalidPasswordException("Current password is incorrect")
        }
        
        userDomainService.changePassword(user, command.newPassword)
        userRepository.save(user)
    }
    
    /**
     * 激活用户
     */
    fun activateUser(userId: String) {
        val user = userRepository.findById(UserId.from(userId))
            ?: throw UserNotFoundException("User not found: $userId")
            
        user.activate()
        userRepository.save(user)
    }
    
    /**
     * 停用用户
     */
    fun deactivateUser(userId: String) {
        val user = userRepository.findById(UserId.from(userId))
            ?: throw UserNotFoundException("User not found: $userId")
            
        user.deactivate()
        userRepository.save(user)
    }
    
    /**
     * 检查用户名可用性
     */
    @Transactional(readOnly = true)
    fun checkUsernameAvailability(username: String): Boolean {
        return userDomainService.isUsernameAvailable(Username(username))
    }
    
    /**
     * 检查邮箱可用性
     */
    @Transactional(readOnly = true)
    fun checkEmailAvailability(email: String): Boolean {
        return userDomainService.isEmailAvailable(Email(email))
    }
    
    /**
     * 检查用户名可用性
     */
    @Transactional(readOnly = true)
    fun isUsernameAvailable(username: String): Boolean {
        return !userRepository.existsByUsername(username)
    }
    
    /**
     * 检查邮箱是否可用
     */
    @Transactional(readOnly = true)
    fun isEmailAvailable(email: String): Boolean {
        return !userRepository.existsByEmail(email)
    }
    
    /**
     * 分页获取用户列表
     */
    @Transactional(readOnly = true)
    fun getUsers(pageable: org.springframework.data.domain.Pageable, keyword: String?): org.springframework.data.domain.Page<User> {
        return if (keyword.isNullOrBlank()) {
            userRepository.findAll(pageable)
        } else {
            userRepository.findByUsernameContainingOrEmailContaining(keyword, keyword, pageable)
        }
    }
    
    /**
     * 获取用户统计信息
     */
    @Transactional(readOnly = true)
    fun getUserStats(): Map<String, Any> {
        val totalUsers = userRepository.count()
        val activeUsers = userRepository.countByIsActiveTrue()
        val inactiveUsers = totalUsers - activeUsers
        
        return mapOf(
            "totalUsers" to totalUsers,
            "activeUsers" to activeUsers,
            "inactiveUsers" to inactiveUsers
        )
    }
    
    /**
     * 删除用户
     */
    fun deleteUser(userId: String) {
        val user = userRepository.findById(UserId.from(userId))
            ?: throw UserNotFoundException("User not found: $userId")
            
        userRepository.delete(user)
    }
    
    /**
     * 修改密码（重载方法，支持用户名参数）
     */
    fun changePassword(username: String, command: ChangePasswordCommand) {
        val user = userRepository.findByUsername(Username(username))
            ?: throw UserNotFoundException("User not found: $username")
            
        if (!userDomainService.verifyPassword(user, command.currentPassword)) {
            throw InvalidPasswordException("Current password is incorrect")
        }
        
        userDomainService.changePassword(user, command.newPassword)
        userRepository.save(user)
    }
    
    /**
     * 更新用户资料（重载方法，支持用户名参数）
     */
    fun updateUserProfile(username: String, command: UpdateUserProfileCommand): User {
        val user = userRepository.findByUsername(Username(username))
            ?: throw UserNotFoundException("User not found: $username")
            
        val newProfile = UserProfile(
            displayName = command.displayName,
            avatar = command.avatar,
            bio = command.bio
        )
        
        user.updateProfile(newProfile)
        val savedUser = userRepository.save(user)
        
        return UserDto.from(savedUser)
    }
    
    /**
     * 用户登录验证（返回认证结果）
     */
    @Transactional(readOnly = true)
    fun authenticateUser(command: AuthenticateUserCommand): Map<String, Any> {
        val user = userRepository.findByUsername(Username(command.username))
            ?: throw UserNotFoundException("Invalid username or password")
            
        if (!user.isActive()) {
            throw UserNotActiveException("User is not active")
        }
        
        if (!userDomainService.verifyPassword(user, command.password)) {
            throw InvalidPasswordException("Invalid username or password")
        }
        
        // 这里应该生成JWT token，暂时返回用户信息
        return mapOf(
            "user" to UserDto.fromDomain(user),
            "token" to "jwt-token-placeholder"
        )
    }
}

/**
 * 用户未激活异常
 */
class UserNotActiveException(message: String) : RuntimeException(message)