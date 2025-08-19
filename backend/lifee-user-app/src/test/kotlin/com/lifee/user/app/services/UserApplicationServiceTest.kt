package com.lifee.user.app.services

import com.lifee.common.cqrs.commands.CommandBus
import com.lifee.common.cqrs.queries.QueryBus
import com.lifee.user.app.commands.RegisterUserCommand
import com.lifee.user.app.commands.ActivateUserCommand
import com.lifee.user.app.queries.GetUserByIdQuery
import com.lifee.user.app.queries.GetUserByEmailQuery
import com.lifee.user.app.dto.UserDto
import com.lifee.user.domain.UserId
import com.lifee.user.domain.Email
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import kotlinx.coroutines.runBlocking

/**
 * 用户应用服务测试
 */
class UserApplicationServiceTest : BehaviorSpec({
    
    given("用户应用服务") {
        val commandBus = mockk<CommandBus>()
        val queryBus = mockk<QueryBus>()
        
        val service = UserApplicationService(
            commandBus = commandBus,
            queryBus = queryBus
        )
        
        beforeEach {
            clearAllMocks()
        }
        
        `when`("注册新用户") {
            val command = RegisterUserCommand(
                email = "test@example.com",
                password = "Password123!",
                firstName = "John",
                lastName = "Doe"
            )
            
            coEvery { commandBus.send(command) } just Runs
            
            then("应该发送注册命令") {
                runBlocking {
                    service.registerUser(command)
                }
                
                coVerify { commandBus.send(command) }
            }
        }
        
        `when`("激活用户") {
            val userId = UserId.generate()
            val token = "activation-token"
            val command = ActivateUserCommand(userId, token)
            
            coEvery { commandBus.send(command) } just Runs
            
            then("应该发送激活命令") {
                runBlocking {
                    service.activateUser(userId, token)
                }
                
                coVerify { commandBus.send(command) }
            }
        }
        
        `when`("通过ID查询用户") {
            val userId = UserId.generate()
            val query = GetUserByIdQuery(userId)
            val userDto = UserDto(
                id = userId.value,
                email = "test@example.com",
                firstName = "John",
                lastName = "Doe",
                status = "ACTIVE",
                emailVerified = true,
                createdAt = java.time.Instant.now(),
                lastLoginAt = null
            )
            
            coEvery { queryBus.send(query) } returns userDto
            
            then("应该返回用户信息") {
                val result = runBlocking {
                    service.getUserById(userId)
                }
                
                result shouldBe userDto
                coVerify { queryBus.send(query) }
            }
        }
        
        `when`("通过邮箱查询用户") {
            val email = Email("test@example.com")
            val query = GetUserByEmailQuery(email)
            val userDto = UserDto(
                id = "user-123",
                email = email.value,
                firstName = "John",
                lastName = "Doe",
                status = "ACTIVE",
                emailVerified = true,
                createdAt = java.time.Instant.now(),
                lastLoginAt = null
            )
            
            coEvery { queryBus.send(query) } returns userDto
            
            then("应该返回用户信息") {
                val result = runBlocking {
                    service.getUserByEmail(email)
                }
                
                result shouldBe userDto
                coVerify { queryBus.send(query) }
            }
        }
    }
})

/**
 * 模拟的用户应用服务
 */
class UserApplicationService(
    private val commandBus: CommandBus,
    private val queryBus: QueryBus
) {
    
    suspend fun registerUser(command: RegisterUserCommand) {
        commandBus.send(command)
    }
    
    suspend fun activateUser(userId: UserId, token: String) {
        val command = ActivateUserCommand(userId, token)
        commandBus.send(command)
    }
    
    suspend fun getUserById(userId: UserId): UserDto? {
        val query = GetUserByIdQuery(userId)
        return queryBus.send(query)
    }
    
    suspend fun getUserByEmail(email: Email): UserDto? {
        val query = GetUserByEmailQuery(email)
        return queryBus.send(query)
    }
}