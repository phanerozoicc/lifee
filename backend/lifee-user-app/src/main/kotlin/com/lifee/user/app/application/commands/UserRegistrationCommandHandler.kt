package com.lifee.user.app.application.commands

import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.common.orchestration.BusinessFlowOrchestrator
import com.lifee.common.orchestration.BusinessFlowType
import com.lifee.user.domain.User
import com.lifee.user.domain.Email
import com.lifee.user.domain.Password
import com.lifee.user.domain.UserId
import com.lifee.user.domain.UserProfile
import com.lifee.user.domain.UserRepository

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 用户注册命令
 */
data class RegisterUserCommand(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
) : com.lifee.common.cqrs.commands.Command

/**
 * 用户注册命令处理器
 * 协调完整的用户注册流程
 */
@Component
class UserRegistrationCommandHandler(
    private val userRepository: UserRepository,
    private val businessFlowOrchestrator: BusinessFlowOrchestrator
) : AsyncCommandHandler<RegisterUserCommand, UserRegistrationResult> {
    
    private val logger = LoggerFactory.getLogger(UserRegistrationCommandHandler::class.java)
    
    @Transactional
    override suspend fun handle(command: RegisterUserCommand): UserRegistrationResult {
        logger.info("Starting user registration for email: {}", command.email)
        
        try {
            // 1. 验证用户输入
            validateUserInput(command)
            
            // 2. 创建用户
            val email = Email(command.email)
            val userId = UserId.generate(1L)
            val password = Password(command.password)
            
            val profile = UserProfile.create(
                firstName = command.firstName,
                lastName = command.lastName
            )
            
            val user = User(
                id = userId,
                email = email,
                password = password,
                profile = profile
            )
            
            // 3. 保存用户
            userRepository.save(user)
            logger.info("User created successfully with ID: {}", userId)
            
            // 4. 启动用户注册业务流程
            val flowId = businessFlowOrchestrator.startFlow(
                flowType = BusinessFlowType.USER_REGISTRATION,
                initiatorId = userId.toString(),
                flowData = mapOf(
                    "userId" to userId.toString(),
                    "email" to command.email,
                    "firstName" to command.firstName,
                    "lastName" to command.lastName
                )
            )
            
            logger.info("User registration flow completed for user: {}", userId)
            
            return UserRegistrationResult.success(
                userId = userId.toString(),
                flowId = flowId,
                message = "用户注册成功"
            )
            
        } catch (e: Exception) {
            logger.error("Error during user registration: {}", e.message, e)
            return UserRegistrationResult.failure("用户注册失败: ${e.message}")
        }
    }
    
    /**
     * 验证用户输入
     */
    private fun validateUserInput(command: RegisterUserCommand) {
        require(command.email.isNotBlank()) { "邮箱不能为空" }
        require(command.password.isNotBlank()) { "密码不能为空" }
        require(command.firstName.isNotBlank()) { "名字不能为空" }
        require(command.lastName.isNotBlank()) { "姓氏不能为空" }
        
        // 验证邮箱格式
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        require(emailRegex.matches(command.email)) { "邮箱格式无效" }
        
        // 验证密码强度
        require(command.password.length >= 8) { "密码长度至少8位" }
        require(command.password.any { it.isUpperCase() }) { "密码必须包含大写字母" }
        require(command.password.any { it.isLowerCase() }) { "密码必须包含小写字母" }
        require(command.password.any { it.isDigit() }) { "密码必须包含数字" }
    }
    

}

/**
 * 用户注册结果
 */
data class UserRegistrationResult(
    val success: Boolean,
    val userId: String? = null,
    val flowId: String? = null,
    val message: String,
    val error: String? = null
) {
    companion object {
        fun success(userId: String, flowId: String, message: String): UserRegistrationResult {
            return UserRegistrationResult(
                success = true,
                userId = userId,
                flowId = flowId,
                message = message
            )
        }
        
        fun failure(error: String): UserRegistrationResult {
            return UserRegistrationResult(
                success = false,
                message = "注册失败",
                error = error
            )
        }
    }
}