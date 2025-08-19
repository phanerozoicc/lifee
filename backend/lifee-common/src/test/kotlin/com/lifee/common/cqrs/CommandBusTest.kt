package com.lifee.common.cqrs

import com.lifee.common.cqrs.commands.Command
import com.lifee.common.cqrs.commands.CommandBus
import com.lifee.common.cqrs.commands.CommandHandler
import com.lifee.common.cqrs.commands.DefaultCommandBus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.BehaviorSpec.Given
import io.kotest.core.spec.style.BehaviorSpec.When
import io.kotest.core.spec.style.BehaviorSpec.Then
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.runBlocking
import org.springframework.context.ApplicationContext
import io.mockk.mockk
import io.mockk.every
import io.mockk.coEvery
import io.mockk.coVerify

/**
 * 命令总线测试
 */
class CommandBusTest : BehaviorSpec() {
    
    init {
        Given("一个命令总线") {
            val mockApplicationContext = mockk<ApplicationContext>()
            val commandBus = DefaultCommandBus(mockApplicationContext)
            
            When("发送一个有处理器的命令") {
                val command = TestCommand("测试数据")
                val handler = mockk<CommandHandler<TestCommand>>()
                
                every { mockApplicationContext.getBeansOfType(CommandHandler::class.java) } returns mapOf(
                    "testCommandHandler" to handler
                )
                coEvery { handler.handle(command) } returns Unit
                
                Then("应该成功处理命令") {
                    runBlocking {
                        commandBus.send(command)
                    }
                    
                    coVerify(exactly = 1) { handler.handle(command) }
                }
            }
            
            When("发送一个没有处理器的命令") {
                val command = UnhandledCommand("无处理器")
                
                every { mockApplicationContext.getBeansOfType(CommandHandler::class.java) } returns emptyMap()
                
                Then("应该抛出异常") {
                    shouldThrow<IllegalArgumentException> {
                        runBlocking {
                            commandBus.send(command)
                        }
                    }
                }
            }
            
            When("发送一个有多个处理器的命令") {
                val command = TestCommand("测试数据")
                val handler1 = mockk<CommandHandler<TestCommand>>()
                val handler2 = mockk<CommandHandler<TestCommand>>()
                
                every { mockApplicationContext.getBeansOfType(CommandHandler::class.java) } returns mapOf(
                    "handler1" to handler1,
                    "handler2" to handler2
                )
                
                Then("应该抛出异常") {
                    shouldThrow<IllegalArgumentException> {
                        runBlocking {
                            commandBus.send(command)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 测试命令
 */
data class TestCommand(val data: String) : Command

/**
 * 无处理器的测试命令
 */
data class UnhandledCommand(val data: String) : Command