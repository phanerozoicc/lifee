package com.lifee.user.app.handlers

import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.exceptions.BusinessRuleException
import com.lifee.user.app.commands.LoginCommand
import com.lifee.user.app.dto.LoginResponseDto
import com.lifee.user.app.dto.UserDto
import com.lifee.user.domain.*
// import com.lifee.user.domain.events.UserLoginSuccessEvent
// import com.lifee.user.domain.events.UserLoginFailedEvent
import com.lifee.user.domain.services.JwtService
import java.time.Instant
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

/**
 * 用户登录命令处理器
 */
@Component
class LoginCommandHandler(
    private val userRepository: UserRepository,
    private val userLoginLogRepository: UserLoginLogRepository,
    private val jwtService: JwtService,
    private val eventBus: EventBus,
    private val transactionTemplate: TransactionTemplate
) : AsyncCommandHandler<LoginCommand, LoginResponseDto> {
    
    private val logger = LoggerFactory.getLogger(LoginCommandHandler::class.java)
    
    override suspend fun handle(command: LoginCommand): LoginResponseDto {
        logger.info("处理用户登录请求: email={}, ip={}", command.email, command.ipAddress)
        
        return try {
            // 1. 根据邮箱查找用户
            val email = Email.of(command.email)
            val user = userRepository.findByEmail(email)
                ?: throw BusinessRuleException("用户不存在或密码错误")
            
            // 2. 验证密码
            if (!user.verifyPassword(command.password)) {
                logger.warn("用户登录失败 - 密码错误: email={}, ip={}", command.email, command.ipAddress)
                throw BusinessRuleException("用户不存在或密码错误")
            }
            
            // 3. 记录登录时间
            user.recordLogin()
            
            // 4. 保存用户（更新最后登录时间）
            userRepository.save(user)
            
            // 5. 生成JWT令牌
            val accessToken = jwtService.generateAccessToken(user)
            val refreshToken = jwtService.generateRefreshToken(user)
            val expiresIn = jwtService.getAccessTokenExpirationSeconds()
            
            // 6. 构建响应
            val userDto = UserDto.fromDomain(user)
            val response = LoginResponseDto(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = expiresIn,
                user = userDto
            )
            
            // 7. 记录登录成功日志
            val successLog = UserLoginLog.createSuccessLog(
                userId = user.getId(),
                email = command.email,
                ipAddress = command.ipAddress,
                userAgent = command.userAgent
            )
            userLoginLogRepository.save(successLog)
            
            logger.info("用户登录成功: userId={}, email={}", user.getId().value, command.email)
            
            response
                 
        } catch (e: BusinessRuleException) {
            // 记录登录失败日志
            val failureLog = UserLoginLog.createFailureLog(
                email = command.email,
                ipAddress = command.ipAddress,
                userAgent = command.userAgent,
                loginResult = LoginResult.FAILED_INVALID_CREDENTIALS,
                failureReason = e.message ?: "登录失败",
                userId = null
            )
            userLoginLogRepository.save(failureLog)
            
            logger.warn("用户登录失败: email={}, error={}", command.email, e.message)
            
            throw e
        } catch (e: Exception) {
            // 记录登录异常日志
            val failureLog = UserLoginLog.createFailureLog(
                email = command.email,
                ipAddress = command.ipAddress,
                userAgent = command.userAgent,
                loginResult = LoginResult.FAILED_INVALID_CREDENTIALS,
                failureReason = "系统异常: ${e.message}",
                userId = null
            )
            userLoginLogRepository.save(failureLog)
            
            logger.error("用户登录处理异常: email={}", command.email, e)
            
            throw BusinessRuleException("登录处理失败")
        }
    }
}