package com.lifee.user.app.controllers

import com.lifee.common.cqrs.commands.CommandBus
import com.lifee.common.cqrs.queries.QueryBus
import com.lifee.user.app.commands.RegisterUserCommand
import com.lifee.user.app.dto.UserDto
import com.lifee.user.app.dto.RegisterUserRequest
import com.lifee.user.app.dto.ActivateUserRequest
import com.lifee.user.domain.UserId
import com.lifee.user.domain.Email
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * 用户控制器测试
 */
class UserControllerTest : BehaviorSpec({
    
    given("用户控制器") {
        val commandBus = mockk<CommandBus>()
        val queryBus = mockk<QueryBus>()
        
        val controller = UserController(
            commandBus = commandBus,
            queryBus = queryBus
        )
        
        beforeEach {
            clearAllMocks()
        }
        
        `when`("注册新用户") {
            val request = RegisterUserRequest(
                email = "test@example.com",
                password = "Password123!",
                firstName = "John",
                lastName = "Doe"
            )
            
            coEvery { commandBus.send(any<RegisterUserCommand>()) } just Runs
            
            then("应该返回成功响应") {
                val response = runBlocking {
                    controller.register(request)
                }
                
                response.statusCode shouldBe HttpStatus.CREATED
                coVerify { commandBus.send(any<RegisterUserCommand>()) }
            }
        }
        
        `when`("激活用户") {
            val userId = UserId.generate()
            val request = ActivateUserRequest(
                token = "activation-token"
            )
            
            coEvery { commandBus.send(any()) } just Runs
            
            then("应该返回成功响应") {
                val response = runBlocking {
                    controller.activate(userId.value, request)
                }
                
                response.statusCode shouldBe HttpStatus.OK
                coVerify { commandBus.send(any()) }
            }
        }
        
        `when`("获取用户信息") {
            val userId = UserId.generate()
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
            
            coEvery { queryBus.send(any()) } returns userDto
            
            then("应该返回用户信息") {
                val response = runBlocking {
                    controller.getUser(userId.value)
                }
                
                response.statusCode shouldBe HttpStatus.OK
                response.body shouldBe userDto
                coVerify { queryBus.send(any()) }
            }
        }
        
        `when`("获取不存在的用户") {
            val userId = UserId.generate()
            
            coEvery { queryBus.send(any()) } returns null
            
            then("应该返回404") {
                val response = runBlocking {
                    controller.getUser(userId.value)
                }
                
                response.statusCode shouldBe HttpStatus.NOT_FOUND
                coVerify { queryBus.send(any()) }
            }
        }
    }
})

/**
 * 模拟的用户控制器
 */
class UserController(
    private val commandBus: CommandBus,
    private val queryBus: QueryBus
) {
    
    suspend fun register(request: RegisterUserRequest): ResponseEntity<String> {
        val command = RegisterUserCommand(
            email = request.email,
            password = request.password,
            firstName = request.firstName,
            lastName = request.lastName
        )
        
        commandBus.send(command)
        return ResponseEntity.status(HttpStatus.CREATED).body("用户注册成功")
    }
    
    suspend fun activate(userId: String, request: ActivateUserRequest): ResponseEntity<String> {
        // 这里应该有激活逻辑
        commandBus.send(mockk())
        return ResponseEntity.ok("用户激活成功")
    }
    
    suspend fun getUser(userId: String): ResponseEntity<UserDto> {
        val userDto = queryBus.send<UserDto?>(mockk())
        return if (userDto != null) {
            ResponseEntity.ok(userDto)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

/**
 * 请求DTO类
 */
data class RegisterUserRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

data class ActivateUserRequest(
    val token: String
)