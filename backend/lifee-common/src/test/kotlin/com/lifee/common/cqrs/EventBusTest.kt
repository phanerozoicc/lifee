package com.lifee.common.cqrs

import com.lifee.common.cqrs.events.DefaultEventBus
import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.cqrs.events.EventHandler
import com.lifee.common.domain.DomainEvent
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.springframework.context.ApplicationContext
import io.mockk.mockk
import io.mockk.every
import io.mockk.coEvery
import io.mockk.coVerify
import java.time.Instant
import java.util.*

/**
 * 事件总线测试
 */
class EventBusTest : BehaviorSpec({
    
    given("一个事件总线") {
        val mockApplicationContext = mockk<ApplicationContext>()
        val eventBus = DefaultEventBus(mockApplicationContext)
        
        `when`("发布一个有处理器的事件") {
            val event = TestEvent("test-aggregate-id", "测试事件")
            val handler = mockk<EventHandler<TestEvent>>()
            
            every { mockApplicationContext.getBeansOfType(EventHandler::class.java) } returns mapOf(
                "testEventHandler" to handler
            )
            coEvery { handler.handle(event) } returns Unit
            
            then("应该成功处理事件") {
                runBlocking {
                    eventBus.publish(event)
                }
                
                coVerify(exactly = 1) { handler.handle(event) }
            }
        }
        
        `when`("发布一个没有处理器的事件") {
            val event = UnhandledEvent("test-aggregate-id", "无处理器事件")
            
            every { mockApplicationContext.getBeansOfType(EventHandler::class.java) } returns emptyMap()
            
            then("应该静默忽略") {
                runBlocking {
                    eventBus.publish(event)
                }
                // 不应该抛出异常
            }
        }
        
        `when`("发布一个有多个处理器的事件") {
            val event = TestEvent("test-aggregate-id", "测试事件")
            val handler1 = mockk<EventHandler<TestEvent>>()
            val handler2 = mockk<EventHandler<TestEvent>>()
            
            every { mockApplicationContext.getBeansOfType(EventHandler::class.java) } returns mapOf(
                "handler1" to handler1,
                "handler2" to handler2
            )
            coEvery { handler1.handle(event) } returns Unit
            coEvery { handler2.handle(event) } returns Unit
            
            then("应该调用所有处理器") {
                runBlocking {
                    eventBus.publish(event)
                }
                
                coVerify(exactly = 1) { handler1.handle(event) }
                coVerify(exactly = 1) { handler2.handle(event) }
            }
        }
        
        `when`("批量发布事件") {
            val events = listOf(
                TestEvent("aggregate-1", "事件1"),
                TestEvent("aggregate-2", "事件2")
            )
            val handler = mockk<EventHandler<TestEvent>>()
            
            every { mockApplicationContext.getBeansOfType(EventHandler::class.java) } returns mapOf(
                "testEventHandler" to handler
            )
            coEvery { handler.handle(any()) } returns Unit
            
            then("应该处理所有事件") {
                runBlocking {
                    eventBus.publishAll(events)
                }
                
                coVerify(exactly = 2) { handler.handle(any()) }
            }
        }
    }
})

/**
 * 测试事件
 */
data class TestEvent(
    override val aggregateId: String,
    val data: String,
    override val eventId: String = UUID.randomUUID().toString(),
    override val eventType: String = "TestEvent",
    override val version: Long = 1,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent

/**
 * 无处理器的测试事件
 */
data class UnhandledEvent(
    override val aggregateId: String,
    val data: String,
    override val eventId: String = UUID.randomUUID().toString(),
    override val eventType: String = "UnhandledEvent",
    override val version: Long = 1,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent