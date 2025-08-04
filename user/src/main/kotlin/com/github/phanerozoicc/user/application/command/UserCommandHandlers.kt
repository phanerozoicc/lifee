package com.github.phanerozoicc.user.application.command

import com.github.phanerozoicc.domain.DomainEventPublisher
import com.github.phanerozoicc.user.domain.cqrs.*
import com.github.phanerozoicc.user.domain.model.*
import com.github.phanerozoicc.user.domain.repository.UserRepository
import com.github.phanerozoicc.user.domain.service.UserDomainService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

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

/**
 * 用户登录命令处理器
 */
@Service
class LoginUserCommandHandler(
    private val userRepository: UserRepository,
    private val domainEventPublisher: DomainEventPublisher
) : CommandHandler<LoginUserCommand> {
    
    @Transactional
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
                    "userId" to savedUser.getId().getValue(),
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

/**
 * 更新用户资料命令处理器
 */
@Service
class UpdateUserProfileCommandHandler(
    private val userRepository: UserRepository,
    private val userDomainService: UserDomainService,
    private val domainEventPublisher: DomainEventPublisher
) : CommandHandler<UpdateUserProfileCommand> {
    
    @Transactional
    override suspend fun handle(command: UpdateUserProfileCommand): CommandResult {
        try {
            // 验证命令
            val validationResult = validate(command)
            if (validationResult is CommandResult.ValidationError) {
                return validationResult
            }
            
            val user = userRepository.findById(command.userId)
                ?: return CommandResult.Failure("用户不存在", "USER_NOT_FOUND")
            
            // 验证昵称唯一性
            if (command.nickname != null) {
                userDomainService.validateProfileUpdateUniqueness(command.userId, command.nickname)
            }
            
            // 构建新的用户资料
            val currentProfile = user.getProfile()
            val newProfile = UserProfile.of(
                nickname = command.nickname ?: currentProfile.getNickname(),
                firstName = command.firstName ?: currentProfile.getFirstName(),
                lastName = command.lastName ?: currentProfile.getLastName(),
                avatar = command.avatar ?: currentProfile.getAvatar(),
                bio = command.bio ?: currentProfile.getBio(),
                birthDate = command.birthDate?.let { LocalDate.parse(it) } ?: currentProfile.getBirthDate(),
                gender = command.gender?.let { UserProfile.Gender.valueOf(it) } ?: currentProfile.getGender(),
                phoneNumber = command.phoneNumber ?: currentProfile.getPhoneNumber(),
                address = command.address ?: currentProfile.getAddress(),
                website = command.website ?: currentProfile.getWebsite()
            )
            
            // 更新用户资料
            user.updateProfile(newProfile, command.updatedBy)
            
            // 保存用户
            val savedUser = userRepository.save(user)
            
            // 发布领域事件
            savedUser.getDomainEvents().forEach { event ->
                domainEventPublisher.publish(event)
            }
            savedUser.clearDomainEvents()
            
            return CommandResult.Success("用户资料更新成功")
            
        } catch (e: IllegalArgumentException) {
            return CommandResult.Failure(e.message ?: "资料更新失败", "PROFILE_UPDATE_FAILED")
        } catch (e: Exception) {
            return CommandResult.Failure("系统错误，请稍后重试", "SYSTEM_ERROR")
        }
    }
    
    override fun validate(command: UpdateUserProfileCommand): CommandResult {
        val errors = mutableMapOf<String, MutableList<String>>()
        
        // 验证生日格式
        if (command.birthDate != null) {
            try {
                LocalDate.parse(command.birthDate)
            } catch (e: Exception) {
                errors.getOrPut("birthDate") { mutableListOf() }.add("生日格式不正确")
            }
        }
        
        // 验证性别
        if (command.gender != null) {
            try {
                UserProfile.Gender.valueOf(command.gender)
            } catch (e: Exception) {
                errors.getOrPut("gender") { mutableListOf() }.add("性别值不正确")
            }
        }
        
        return if (errors.isEmpty()) {
            CommandResult.Success()
        } else {
            CommandResult.ValidationError(errors)
        }
    }
}

/**
 * 修改密码命令处理器
 */
@Service
class ChangePasswordCommandHandler(
    private val userRepository: UserRepository,
    private val domainEventPublisher: DomainEventPublisher
) : CommandHandler<ChangePasswordCommand> {
    
    @Transactional
    override suspend fun handle(command: ChangePasswordCommand): CommandResult {
        try {
            // 验证命令
            val validationResult = validate(command)
            if (validationResult is CommandResult.ValidationError) {
                return validationResult
            }
            
            val user = userRepository.findById(command.userId)
                ?: return CommandResult.Failure("用户不存在", "USER_NOT_FOUND")
            
            // 修改密码
            user.changePassword(
                oldPassword = command.oldPassword,
                newPassword = command.newPassword,
                ipAddress = command.ipAddress
            )
            
            // 保存用户
            val savedUser = userRepository.save(user)
            
            // 发布领域事件
            savedUser.getDomainEvents().forEach { event ->
                domainEventPublisher.publish(event)
            }
            savedUser.clearDomainEvents()
            
            return CommandResult.Success("密码修改成功")
            
        } catch (e: IllegalArgumentException) {
            return CommandResult.Failure(e.message ?: "密码修改失败", "PASSWORD_CHANGE_FAILED")
        } catch (e: Exception) {
            return CommandResult.Failure("系统错误，请稍后重试", "SYSTEM_ERROR")
        }
    }
    
    override fun validate(command: ChangePasswordCommand): CommandResult {
        val errors = mutableMapOf<String, MutableList<String>>()
        
        if (command.oldPassword.isBlank()) {
            errors.getOrPut("oldPassword") { mutableListOf() }.add("原密码不能为空")
        }
        
        if (command.newPassword.isBlank()) {
            errors.getOrPut("newPassword") { mutableListOf() }.add("新密码不能为空")
        }
        
        if (command.newPassword != command.confirmPassword) {
            errors.getOrPut("confirmPassword") { mutableListOf() }.add("确认密码与新密码不一致")
        }
        
        return if (errors.isEmpty()) {
            CommandResult.Success()
        } else {
            CommandResult.ValidationError(errors)
        }
    }
}

/**
 * 验证邮箱命令处理器
 */
@Service
class VerifyEmailCommandHandler(
    private val userRepository: UserRepository,
    private val domainEventPublisher: DomainEventPublisher
) : CommandHandler<VerifyEmailCommand> {
    
    @Transactional
    override suspend fun handle(command: VerifyEmailCommand): CommandResult {
        try {
            val user = userRepository.findById(command.userId)
                ?: return CommandResult.Failure("用户不存在", "USER_NOT_FOUND")
            
            // 验证邮箱
            user.verifyEmail(command.verificationToken)
            
            // 保存用户
            val savedUser = userRepository.save(user)
            
            // 发布领域事件
            savedUser.getDomainEvents().forEach { event ->
                domainEventPublisher.publish(event)
            }
            savedUser.clearDomainEvents()
            
            return CommandResult.Success("邮箱验证成功")
            
        } catch (e: IllegalArgumentException) {
            return CommandResult.Failure(e.message ?: "邮箱验证失败", "EMAIL_VERIFICATION_FAILED")
        } catch (e: Exception) {
            return CommandResult.Failure("系统错误，请稍后重试", "SYSTEM_ERROR")
        }
    }
    
    override fun validate(command: VerifyEmailCommand): CommandResult {
        val errors = mutableMapOf<String, MutableList<String>>()
        
        if (command.verificationToken.isBlank()) {
            errors.getOrPut("verificationToken") { mutableListOf() }.add("验证令牌不能为空")
        }
        
        return if (errors.isEmpty()) {
            CommandResult.Success()
        } else {
            CommandResult.ValidationError(errors)
        }
    }
}