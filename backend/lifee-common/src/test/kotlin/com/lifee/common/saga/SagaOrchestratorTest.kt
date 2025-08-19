package com.lifee.common.saga

import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.domain.DomainEvent
import com.lifee.common.eventsourcing.EventStore
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.BehaviorSpec.Given
import io.kotest.core.spec.style.BehaviorSpec.When
import io.kotest.core.spec.style.BehaviorSpec.Then
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlinx.coroutines.runBlocking
import io.mockk.*
import java.time.Instant
import java.util.*
import kotlin.reflect.KClass

/**
 * Saga协调器测试
 */
class SagaOrchestratorTest : BehaviorSpec() {
    
    init {
        Given("一个Saga协调器") {
            val eventBus = mockk<EventBus>()
            val eventStore = mockk<EventStore>()
            val orchestrator = SagaOrchestrator(eventBus, eventStore)
            
            When("注册Saga定义") {
                val sagaClass = TestSaga::class
                val sagaDefinition = SagaDefinition(
                    name = "TestSaga",
                    description = "测试Saga",
                    steps = listOf()
                )
                
                Then("应该成功注册") {
                    orchestrator.registerSaga(sagaClass, sagaDefinition)
                    // 验证注册成功（通过后续操作验证）
                }
            }
            
            When("启动一个新的Saga") {
                val sagaClass = TestSaga::class
                val sagaDefinition = SagaDefinition(
                    name = "TestSaga",
                    description = "测试Saga",
                    steps = listOf()
                )
                val initialData = mapOf("userId" to "user123", "amount" to 100.0)
                
                orchestrator.registerSaga(sagaClass, sagaDefinition)
                
                Then("应该返回Saga ID") {
                    runBlocking {
                        val sagaId = orchestrator.startSaga(sagaClass, initialData = initialData)
                        sagaId shouldNotBe null
                        sagaId.shouldNotBeEmpty()
                    }
                }
            }
            
            When("处理事件") {
                val sagaClass = TestSaga::class
                val sagaDefinition = SagaDefinition(
                    name = "TestSaga",
                    description = "测试Saga",
                    steps = listOf()
                )
                val event = TestDomainEvent("test-aggregate-id", "测试事件")
                
                orchestrator.registerSaga(sagaClass, sagaDefinition)
                
                Then("应该处理事件") {
                    runBlocking {
                        orchestrator.handleEvent(event)
                    }
                    // 验证事件处理（通过日志或状态变化）
                }
            }
            
            When("获取Saga状态") {
                val sagaClass = TestSaga::class
                val sagaDefinition = SagaDefinition(
                    name = "TestSaga",
                    description = "测试Saga",
                    steps = listOf()
                )
                
                orchestrator.registerSaga(sagaClass, sagaDefinition)
                
                Then("应该能获取活跃的Saga列表") {
                    runBlocking {
                        val sagaId = orchestrator.startSaga(sagaClass)
                        val activeSagas = orchestrator.getActiveSagas()
                        activeSagas.size shouldBe 1
                        
                        val sagaStatus = orchestrator.getSagaStatus(sagaId)
                        sagaStatus shouldNotBe null
                        sagaStatus?.id shouldBe sagaId
                    }
                }
            }
        }
    }
}

/**
 * 测试用Saga类
 */
class TestSaga : Saga {
    override val id: String = "test-saga"
    override val type: String = "TestSaga"
}

/**
 * 测试用领域事件
 */
class TestDomainEvent(
    aggregateId: String,
    val data: String,
    version: Long = 1,
    occurredOn: Instant = Instant.now(),
    eventId: UUID = UUID.randomUUID()
) : DomainEvent(aggregateId, version, occurredOn, eventId) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return TestDomainEvent(aggregateId, data, version, occurredOn, eventId)
    }
}