package com.lifee.common.eventsourcing

import com.lifee.common.domain.DomainEvent
import com.lifee.common.domain.EventSourcedAggregateRoot
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldBeEmpty
import java.time.Instant
import java.util.*

/**
 * 事件溯源聚合根测试
 */
class EventSourcedAggregateRootTest : BehaviorSpec({
    
    given("一个事件溯源聚合根") {
        val aggregateId = TestAggregateId(UUID.randomUUID().toString())
        val aggregate = TestAggregate(aggregateId)
        
        `when`("创建新的聚合根") {
            then("应该有正确的初始状态") {
                aggregate.getId() shouldBe aggregateId
                aggregate.getVersion() shouldBe 0
                aggregate.hasUncommittedEvents() shouldBe false
                aggregate.getUncommittedEvents() shouldBeEmpty()
            }
        }
        
        `when`("添加领域事件") {
            val event = TestDomainEvent(aggregateId.value, "测试事件")
            aggregate.addDomainEvent(event)
            
            then("应该正确记录未提交事件") {
                aggregate.hasUncommittedEvents() shouldBe true
                aggregate.getUncommittedEvents() shouldHaveSize 1
                aggregate.getUncommittedEvents().first() shouldBe event
            }
        }
        
        `when`("标记事件为已提交") {
            val event = TestDomainEvent(aggregateId.value, "测试事件")
            aggregate.addDomainEvent(event)
            aggregate.markEventsAsCommitted()
            
            then("应该清空未提交事件列表") {
                aggregate.hasUncommittedEvents() shouldBe false
                aggregate.getUncommittedEvents() shouldBeEmpty()
                aggregate.getVersion() shouldBe 1
            }
        }
        
        `when`("应用历史事件") {
            val event = TestDomainEvent(aggregateId.value, "历史事件")
            aggregate.applyEvent(event)
            
            then("应该增加版本但不记录为未提交事件") {
                aggregate.getVersion() shouldBe 1
                aggregate.hasUncommittedEvents() shouldBe false
                aggregate.getUncommittedEvents() shouldBeEmpty()
            }
        }
    }
})

/**
 * 测试用聚合根ID
 */
data class TestAggregateId(val value: String)

/**
 * 测试用聚合根
 */
class TestAggregate(id: TestAggregateId) : EventSourcedAggregateRoot<TestAggregateId>(id) {
    
    var testData: String = ""
        private set
    
    fun updateTestData(data: String) {
        val event = TestDomainEvent(getId().value, data)
        addDomainEvent(event)
        applyTestDataUpdated(event)
    }
    
    private fun applyTestDataUpdated(event: TestDomainEvent) {
        testData = event.data
    }
    
    override fun applyEvent(event: DomainEvent) {
        when (event) {
            is TestDomainEvent -> applyTestDataUpdated(event)
        }
        incrementVersion()
    }
}

/**
 * 测试用领域事件
 */
data class TestDomainEvent(
    override val aggregateId: String,
    val data: String,
    override val eventId: String = UUID.randomUUID().toString(),
    override val eventType: String = "TestDomainEvent",
    override val version: Long = 0,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent