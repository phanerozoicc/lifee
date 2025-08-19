package com.lifee.user.app.handlers

import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.exceptions.BusinessRuleException
import com.lifee.user.app.commands.LoginCommand
import com.lifee.user.app.dto.LoginResponseDto
import com.lifee.user.domain.*
import com.lifee.user.domain.events.UserLoginFailedEvent
import com.lifee.user.domain.events.UserLoginSuccessEvent
import com.lifee.user.domain.services.JwtService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.mockk.*
import kotlinx.coroutines.runBlocking
import java.time.Instant

/**
 * LoginCommandHandler测试
 */
class LoginCommandHandlerTest : BehaviorSpec({
    
    given("用户登录命令处理器") {
        val userRepository = mockk<UserRepository>()
        val userLoginLogRepository = mockk<UserLoginLogRepository>()
        val jwtService = mockk<JwtService>()
        val eventBus = mockk<EventBus>()
        
        val handler = LoginCommandHandler(
            userRepository = userRepository,
            userLoginLogRepository = userLoginLogRepository,
            jwtService = jwtService,
            eventBus = eventBus
        )
        
        beforeEach {
            clearAllMocks()
        }
        
        `when`("使用有效凭据登录") {
            val command = LoginCommand(
                email = "test@example.com",
                password = "Password123!",
                ipAddress = "192.168.1.1",
                userAgent = "Mozilla/5.0 Test Browser"
            )
            
            val userId = UserId.generate()
            val email = Email(command.email)
            val password = Password(command.password)
            val user = mockk<User>()
            val loginLog = mockk<UserLoginLog>()
            val accessToken = "jwt-access-token"
            val refreshToken = "jwt-refresh-token"
            
            // Mock设置
            coEvery { userRepository.findByEmail(email) } returns user
            every { user.getId() } returns userId
            every { user.getEmail() } returns email
            every { user.getPassword() } returns password
            every { user.getStatus() } returns UserStatus.ACTIVE
            every { user.isActivated() } returns true
            every { password.matches(command.password) } returns true
            every { jwtService.generateAccessToken(userId.value) } returns accessToken
            every { jwtService.generateRefreshToken(userId.value) } returns refreshToken
            every { UserLoginLog.create(userId, command.ipAddress, command.userAgent, true) } returns loginLog
            coEvery { userLoginLogRepository.save(loginLog) } returns loginLog
            coEvery { eventBus.publish(any<UserLoginSuccessEvent>()) } just Runs
            
            then("应该成功登录并返回令牌") {
                val result = runBlocking {
                    handler.handle(command)
                }
                
                result shouldNotBe null
                result.accessToken shouldBe accessToken
                result.refreshToken shouldBe refreshToken
                result.tokenType shouldBe "Bearer"
                result.expiresIn shouldBe 3600 // 1小时
                
                // 验证调用
                coVerify { userRepository.findByEmail(email) }
                verify { password.matches(command.password) }
                verify { jwtService.generateAccessToken(userId.value) }
                verify { jwtService.generateRefreshToken(userId.value) }
                verify { UserLoginLog.create(userId, command.ipAddress, command.userAgent, true) }
                coVerify { userLoginLogRepository.save(loginLog) }
                coVerify { eventBus.publish(any<UserLoginSuccessEvent>()) }
            }
        }
        
        `when`("使用不存在的邮箱登录") {
            val command = LoginCommand(
                email = "nonexistent@example.com",
                password = "Password123!",
                ipAddress = "192.168.1.1",
                userAgent = "Mozilla/5.0 Test Browser"
            )
            
            val email = Email(command.email)
            coEvery { userRepository.findByEmail(email) } returns null
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    runBlocking {
                        handler.handle(command)
                    }
                }
                
                // 验证只检查了用户存在性
                coVerify { userRepository.findByEmail(email) }
                verify(exactly = 0) { jwtService.generateAccessToken(any()) }
                coVerify(exactly = 0) { eventBus.publish(any<UserLoginSuccessEvent>()) }
            }
        }
        
        `when`("使用错误密码登录") {
            val command = LoginCommand(
                email = "test@example.com",
                password = "WrongPassword123!",
                ipAddress = "192.168.1.1",
                userAgent = "Mozilla/5.0 Test Browser"
            )
            
            val userId = UserId.generate()
            val email = Email(command.email)
            val storedPassword = Password("CorrectPassword123!")
            val user = mockk<User>()
            val loginLog = mockk<UserLoginLog>()
            
            coEvery { userRepository.findByEmail(email) } returns user
            every { user.getId() } returns userId
            every { user.getEmail() } returns email
            every { user.getPassword() } returns storedPassword
            every { user.getStatus() } returns UserStatus.ACTIVE
            every { user.isActivated() } returns true
            every { storedPassword.matches(command.password) } returns false
            every { UserLoginLog.create(userId, command.ipAddress, command.userAgent, false) } returns loginLog
            coEvery { userLoginLogRepository.save(loginLog) } returns loginLog
            coEvery { eventBus.publish(any<UserLoginFailedEvent>()) } just Runs
            
            then("应该抛出业务规则异常并记录失败日志") {
                shouldThrow<BusinessRuleException> {
                    runBlocking {
                        handler.handle(command)
                    }
                }
                
                // 验证调用
                coVerify { userRepository.findByEmail(email) }
                verify { storedPassword.matches(command.password) }
                verify { UserLoginLog.create(userId, command.ipAddress, command.userAgent, false) }
                coVerify { userLoginLogRepository.save(loginLog) }
                coVerify { eventBus.publish(any<UserLoginFailedEvent>()) }
                verify(exactly = 0) { jwtService.generateAccessToken(any()) }
            }
        }
        
        `when`("尝试登录未激活的用户") {
            val command = LoginCommand(
                email = "inactive@example.com",
                password = "Password123!",
                ipAddress = "192.168.1.1",
                userAgent = "Mozilla/5.0 Test Browser"
            )
            
            val userId = UserId.generate()
            val email = Email(command.email)
            val password = Password(command.password)
            val user = mockk<User>()
            val loginLog = mockk<UserLoginLog>()
            
            coEvery { userRepository.findByEmail(email) } returns user
            every { user.getId() } returns userId
            every { user.getEmail() } returns email
            every { user.getPassword() } returns password
            every { user.getStatus() } returns UserStatus.PENDING_ACTIVATION
            every { user.isActivated() } returns false
            every { password.matches(command.password) } returns true
            every { UserLoginLog.create(userId, command.ipAddress, command.userAgent, false) } returns loginLog
            coEvery { userLoginLogRepository.save(loginLog) } returns loginLog
            coEvery { eventBus.publish(any<UserLoginFailedEvent>()) } just Runs
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    runBlocking {
                        handler.handle(command)
                    }
                }
                
                // 验证记录了失败日志
                verify { UserLoginLog.create(userId, command.ipAddress, command.userAgent, false) }
                coVerify { userLoginLogRepository.save(loginLog) }
                coVerify { eventBus.publish(any<UserLoginFailedEvent>()) }
                verify(exactly = 0) { jwtService.generateAccessToken(any()) }
            }
        }
        
        `when`("尝试登录被停用的用户") {
            val command = LoginCommand(
                email = "suspended@example.com",
                password = "Password123!",
                ipAddress = "192.168.1.1",
                userAgent = "Mozilla/5.0 Test Browser"
            )
            
            val userId = UserId.generate()
            val email = Email(command.email)
            val password = Password(command.password)
            val user = mockk<User>()
            val loginLog = mockk<UserLoginLog>()
            
            coEvery { userRepository.findByEmail(email) } returns user
            every { user.getId() } returns userId
            every { user.getEmail() } returns email
            every { user.getPassword() } returns password
            every { user.getStatus() } returns UserStatus.SUSPENDED
            every { user.isActivated() } returns true
            every { password.matches(command.password) } returns true
            every { UserLoginLog.create(userId, command.ipAddress, command.userAgent, false) } returns loginLog
            coEvery { userLoginLogRepository.save(loginLog) } returns loginLog
            coEvery { eventBus.publish(any<UserLoginFailedEvent>()) } just Runs
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    runBlocking {
                        handler.handle(command)
                    }
                }
                
                // 验证记录了失败日志
                verify { UserLoginLog.create(userId, command.ipAddress, command.userAgent, false) }
                coVerify { userLoginLogRepository.save(loginLog) }
                coVerify { eventBus.publish(any<UserLoginFailedEvent>()) }
                verify(exactly = 0) { jwtService.generateAccessToken(any()) }
            }
        }
        
        `when`("尝试登录已删除的用户") {
            val command = LoginCommand(
                email = "deleted@example.com",
                password = "Password123!",
                ipAddress = "192.168.1.1",
                userAgent = "Mozilla/5.0 Test Browser"
            )
            
            val userId = UserId.generate()
            val email = Email(command.email)
            val password = Password(command.password)
            val user = mockk<User>()
            val loginLog = mockk<UserLoginLog>()
            
            coEvery { userRepository.findByEmail(email) } returns user
            every { user.getId() } returns userId
            every { user.getEmail() } returns email
            every { user.getPassword() } returns password
            every { user.getStatus() } returns UserStatus.DELETED
            every { user.isActivated() } returns true
            every { password.matches(command.password) } returns true
            every { UserLoginLog.create(userId, command.ipAddress, command.userAgent, false) } returns loginLog
            coEvery { userLoginLogRepository.save(loginLog) } returns loginLog
            coEvery { eventBus.publish(any<UserLoginFailedEvent>()) } just Runs
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    runBlocking {
                        handler.handle(command)
                    }
                }
                
                // 验证记录了失败日志
                verify { UserLoginLog.create(userId, command.ipAddress, command.userAgent, false) }
                coVerify { userLoginLogRepository.save(loginLog) }
                coVerify { eventBus.publish(any<UserLoginFailedEvent>()) }
                verify(exactly = 0) { jwtService.generateAccessToken(any()) }
            }
        }
        
        `when`("JWT令牌生成失败") {
            val command = LoginCommand(
                email = "test@example.com",
                password = "Password123!",
                ipAddress = "192.168.1.1",
                userAgent = "Mozilla/5.0 Test Browser"
            )
            
            val userId = UserId.generate()
            val email = Email(command.email)
            val password = Password(command.password)
            val user = mockk<User>()
            
            coEvery { userRepository.findByEmail(email) } returns user
            every { user.getId() } returns userId
            every { user.getEmail() } returns email
            every { user.getPassword() } returns password
            every { user.getStatus() } returns UserStatus.ACTIVE
            every { user.isActivated() } returns true
            every { password.matches(command.password) } returns true
            every { jwtService.generateAccessToken(userId.value) } throws RuntimeException("JWT生成失败")
            
            then("应该抛出运行时异常") {
                shouldThrow<RuntimeException> {
                    runBlocking {
                        handler.handle(command)
                    }
                }
            }
        }
        
        `when`("使用无效的邮箱格式") {
            val command = LoginCommand(
                email = "invalid-email",
                password = "Password123!",
                ipAddress = "192.168.1.1",
                userAgent = "Mozilla/5.0 Test Browser"
            )
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    runBlocking {
                        handler.handle(command)
                    }
                }
            }
        }
        
        `when`("使用空密码") {
            val command = LoginCommand(
                email = "test@example.com",
                password = "",
                ipAddress = "192.168.1.1",
                userAgent = "Mozilla/5.0 Test Browser"
            )
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    runBlocking {
                        handler.handle(command)
                    }
                }
            }
        }
    }
})