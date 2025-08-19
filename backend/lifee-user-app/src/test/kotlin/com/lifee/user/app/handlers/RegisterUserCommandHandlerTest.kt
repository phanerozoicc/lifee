package com.lifee.user.app.handlers

import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.exceptions.BusinessRuleException
import com.lifee.user.app.commands.RegisterUserCommand
import com.lifee.user.domain.*
import com.lifee.user.domain.events.UserRegisteredEvent
import com.lifee.user.domain.services.UserFactory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.springframework.transaction.support.TransactionTemplate

/**
 * RegisterUserCommandHandler测试
 */
class RegisterUserCommandHandlerTest : BehaviorSpec({
    
    given("用户注册命令处理器") {
        val userRepository = mockk<UserRepository>()
        val activationTokenRepository = mockk<ActivationTokenRepository>()
        val eventBus = mockk<EventBus>()
        val transactionTemplate = mockk<TransactionTemplate>()
        val userFactory = mockk<UserFactory>()
        
        val handler = RegisterUserCommandHandler(
            userRepository = userRepository,
            activationTokenRepository = activationTokenRepository,
            eventBus = eventBus,
            transactionTemplate = transactionTemplate,
            userFactory = userFactory
        )
        
        beforeEach {
            clearAllMocks()
        }
        
        `when`("处理有效的用户注册命令") {
            val command = RegisterUserCommand(
                email = "test@example.com",
                password = "Password123!",
                firstName = "John",
                lastName = "Doe"
            )
            
            val userId = UserId.generate()
            val email = Email(command.email)
            val password = Password(command.password)
            val user = mockk<User>()
            val activationToken = mockk<ActivationToken>()
            
            // Mock设置
            coEvery { userRepository.existsByEmail(email) } returns false
            every { userFactory.createUser(email, password, command.firstName, command.lastName) } returns user
            every { user.getId() } returns userId
            every { user.getUncommittedEvents() } returns listOf(
                UserRegisteredEvent(
                    userId = userId,
                    email = email,
                    firstName = command.firstName,
                    lastName = command.lastName,
                    registeredAt = java.time.Instant.now()
                )
            )
            coEvery { userRepository.save(user) } returns user
            every { ActivationToken.generate(userId) } returns activationToken
            coEvery { activationTokenRepository.save(activationToken) } returns activationToken
            coEvery { eventBus.publish(any()) } just Runs
            every { transactionTemplate.execute<Unit>(any()) } answers {
                val callback = firstArg<org.springframework.transaction.support.TransactionCallback<Unit>>()
                callback.doInTransaction(mockk())
            }
            
            then("应该成功注册用户") {
                runBlocking {
                    handler.handle(command)
                }
                
                // 验证调用
                coVerify { userRepository.existsByEmail(email) }
                verify { userFactory.createUser(email, password, command.firstName, command.lastName) }
                coVerify { userRepository.save(user) }
                verify { ActivationToken.generate(userId) }
                coVerify { activationTokenRepository.save(activationToken) }
                coVerify { eventBus.publish(any<UserRegisteredEvent>()) }
            }
        }
        
        `when`("尝试注册已存在的邮箱") {
            val command = RegisterUserCommand(
                email = "existing@example.com",
                password = "Password123!",
                firstName = "John",
                lastName = "Doe"
            )
            
            val email = Email(command.email)
            coEvery { userRepository.existsByEmail(email) } returns true
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    runBlocking {
                        handler.handle(command)
                    }
                }
                
                // 验证只检查了邮箱存在性，没有进行后续操作
                coVerify { userRepository.existsByEmail(email) }
                verify(exactly = 0) { userFactory.createUser(any(), any(), any(), any()) }
                coVerify(exactly = 0) { userRepository.save(any()) }
            }
        }
        
        `when`("处理无效的邮箱格式") {
            val command = RegisterUserCommand(
                email = "invalid-email",
                password = "Password123!",
                firstName = "John",
                lastName = "Doe"
            )
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    runBlocking {
                        handler.handle(command)
                    }
                }
            }
        }
        
        `when`("处理无效的密码") {
            val command = RegisterUserCommand(
                email = "test@example.com",
                password = "weak", // 弱密码
                firstName = "John",
                lastName = "Doe"
            )
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    runBlocking {
                        handler.handle(command)
                    }
                }
            }
        }
        
        `when`("处理空的名字") {
            val command = RegisterUserCommand(
                email = "test@example.com",
                password = "Password123!",
                firstName = "", // 空名字
                lastName = "Doe"
            )
            
            val email = Email(command.email)
            val password = Password(command.password)
            
            coEvery { userRepository.existsByEmail(email) } returns false
            every { userFactory.createUser(email, password, command.firstName, command.lastName) } throws BusinessRuleException("名字不能为空")
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    runBlocking {
                        handler.handle(command)
                    }
                }
            }
        }
        
        `when`("处理空的姓氏") {
            val command = RegisterUserCommand(
                email = "test@example.com",
                password = "Password123!",
                firstName = "John",
                lastName = "" // 空姓氏
            )
            
            val email = Email(command.email)
            val password = Password(command.password)
            
            coEvery { userRepository.existsByEmail(email) } returns false
            every { userFactory.createUser(email, password, command.firstName, command.lastName) } throws BusinessRuleException("姓氏不能为空")
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    runBlocking {
                        handler.handle(command)
                    }
                }
            }
        }
        
        `when`("数据库保存失败") {
            val command = RegisterUserCommand(
                email = "test@example.com",
                password = "Password123!",
                firstName = "John",
                lastName = "Doe"
            )
            
            val userId = UserId.generate()
            val email = Email(command.email)
            val password = Password(command.password)
            val user = mockk<User>()
            
            coEvery { userRepository.existsByEmail(email) } returns false
            every { userFactory.createUser(email, password, command.firstName, command.lastName) } returns user
            every { user.getId() } returns userId
            coEvery { userRepository.save(user) } throws RuntimeException("数据库连接失败")
            every { transactionTemplate.execute<Unit>(any()) } answers {
                val callback = firstArg<org.springframework.transaction.support.TransactionCallback<Unit>>()
                callback.doInTransaction(mockk())
            }
            
            then("应该抛出运行时异常") {
                shouldThrow<RuntimeException> {
                    runBlocking {
                        handler.handle(command)
                    }
                }
            }
        }
        
        `when`("激活令牌生成失败") {
            val command = RegisterUserCommand(
                email = "test@example.com",
                password = "Password123!",
                firstName = "John",
                lastName = "Doe"
            )
            
            val userId = UserId.generate()
            val email = Email(command.email)
            val password = Password(command.password)
            val user = mockk<User>()
            
            coEvery { userRepository.existsByEmail(email) } returns false
            every { userFactory.createUser(email, password, command.firstName, command.lastName) } returns user
            every { user.getId() } returns userId
            coEvery { userRepository.save(user) } returns user
            every { ActivationToken.generate(userId) } throws RuntimeException("令牌生成失败")
            every { transactionTemplate.execute<Unit>(any()) } answers {
                val callback = firstArg<org.springframework.transaction.support.TransactionCallback<Unit>>()
                callback.doInTransaction(mockk())
            }
            
            then("应该抛出运行时异常") {
                shouldThrow<RuntimeException> {
                    runBlocking {
                        handler.handle(command)
                    }
                }
            }
        }
        
        `when`("事件发布失败") {
            val command = RegisterUserCommand(
                email = "test@example.com",
                password = "Password123!",
                firstName = "John",
                lastName = "Doe"
            )
            
            val userId = UserId.generate()
            val email = Email(command.email)
            val password = Password(command.password)
            val user = mockk<User>()
            val activationToken = mockk<ActivationToken>()
            
            coEvery { userRepository.existsByEmail(email) } returns false
            every { userFactory.createUser(email, password, command.firstName, command.lastName) } returns user
            every { user.getId() } returns userId
            every { user.getUncommittedEvents() } returns listOf(
                UserRegisteredEvent(
                    userId = userId,
                    email = email,
                    firstName = command.firstName,
                    lastName = command.lastName,
                    registeredAt = java.time.Instant.now()
                )
            )
            coEvery { userRepository.save(user) } returns user
            every { ActivationToken.generate(userId) } returns activationToken
            coEvery { activationTokenRepository.save(activationToken) } returns activationToken
            coEvery { eventBus.publish(any()) } throws RuntimeException("事件发布失败")
            every { transactionTemplate.execute<Unit>(any()) } answers {
                val callback = firstArg<org.springframework.transaction.support.TransactionCallback<Unit>>()
                callback.doInTransaction(mockk())
            }
            
            then("应该抛出运行时异常") {
                shouldThrow<RuntimeException> {
                    runBlocking {
                        handler.handle(command)
                    }
                }
            }
        }
    }
})