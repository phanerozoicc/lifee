package com.lifee.common.saga

import com.lifee.common.cqrs.events.Event
import com.lifee.common.domain.DomainEvent
import java.time.Instant
import java.util.*
import kotlin.reflect.KClass

/**
 * Saga接口
 */
interface Saga {
    val id: String
    val type: String
}

/**
 * Saga实例
 */
data class SagaInstance(
    val id: String,
    val sagaType: String,
    var status: SagaStatus,
    var currentStep: Int = 0,
    var currentStepName: String? = null,
    val data: MutableMap<String, Any> = mutableMapOf(),
    val correlationId: String? = null,
    val startedAt: Instant,
    var updatedAt: Instant? = null,
    var completedAt: Instant? = null,
    var errorMessage: String? = null,
    var compensationReason: String? = null,
    val definition: SagaDefinition
)

/**
 * Saga状态
 */
enum class SagaStatus {
    STARTED,        // 已启动
    EXECUTING,      // 执行中
    WAITING,        // 等待外部事件
    COMPLETED,      // 已完成
    COMPENSATING,   // 补偿中
    COMPENSATED,    // 已补偿
    FAILED,         // 失败
    CANCELLED       // 已取消
}

/**
 * Saga定义
 */
data class SagaDefinition(
    val name: String,
    val description: String? = null,
    val steps: List<SagaStep>,
    val eventTriggers: List<SagaEventTrigger> = emptyList(),
    val timeout: Long? = null, // 超时时间（毫秒）
    val retryPolicy: SagaRetryPolicy? = null
)

/**
 * Saga步骤
 */
data class SagaStep(
    val name: String,
    val description: String? = null,
    val action: SagaAction,
    val compensationAction: SagaAction? = null,
    val waitingForEvent: KClass<out DomainEvent>? = null,
    val timeout: Long? = null,
    val retryPolicy: SagaRetryPolicy? = null
) {
    /**
     * 执行步骤
     */
    suspend fun execute(data: Map<String, Any>): SagaStepResult {
        return action.execute(data)
    }
}

/**
 * Saga动作接口
 */
interface SagaAction {
    suspend fun execute(data: Map<String, Any>): SagaStepResult
}

/**
 * Saga步骤结果
 */
data class SagaStepResult(
    val status: Status,
    val data: Map<String, Any> = emptyMap(),
    val error: Throwable? = null,
    val message: String? = null
) {
    enum class Status {
        SUCCESS,    // 成功
        WAITING,    // 等待
        FAILED      // 失败
    }
    
    companion object {
        fun success(data: Map<String, Any> = emptyMap(), message: String? = null): SagaStepResult {
            return SagaStepResult(Status.SUCCESS, data, null, message)
        }
        
        fun waiting(data: Map<String, Any> = emptyMap(), message: String? = null): SagaStepResult {
            return SagaStepResult(Status.WAITING, data, null, message)
        }
        
        fun failed(error: Throwable, data: Map<String, Any> = emptyMap()): SagaStepResult {
            return SagaStepResult(Status.FAILED, data, error, error.message)
        }
    }
}

/**
 * Saga事件触发器
 */
data class SagaEventTrigger(
    val eventType: KClass<out DomainEvent>,
    val correlationProperty: String? = null // 用于关联的事件属性名
)

/**
 * Saga重试策略
 */
data class SagaRetryPolicy(
    val maxRetries: Int = 3,
    val retryDelay: Long = 1000, // 重试延迟（毫秒）
    val backoffMultiplier: Double = 2.0, // 退避倍数
    val maxRetryDelay: Long = 30000 // 最大重试延迟（毫秒）
)

/**
 * Saga步骤状态
 */
enum class SagaStepStatus {
    PENDING,                // 待执行
    EXECUTING,              // 执行中
    COMPLETED,              // 已完成
    WAITING,                // 等待中
    FAILED,                 // 失败
    COMPENSATING,           // 补偿中
    COMPENSATED,            // 已补偿
    COMPENSATION_FAILED,    // 补偿失败
    SKIPPED                 // 跳过
}

/**
 * Saga完成事件
 */
data class SagaCompletedEvent(
    val sagaId: String,
    val sagaType: String,
    val correlationId: String?,
    val completedAt: Instant,
    override val aggregateId: String = sagaId,
    override val version: Long = 1L,
    override val occurredOn: Instant = Instant.now(),
    override val eventId: UUID = UUID.randomUUID()
) : DomainEvent(aggregateId, version, occurredOn, eventId) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return SagaCompletedEvent(
            sagaId = sagaId,
            sagaType = sagaType,
            correlationId = correlationId,
            completedAt = completedAt,
            aggregateId = aggregateId,
            version = version,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
}

/**
 * Saga失败事件
 */
data class SagaFailedEvent(
    val sagaId: String,
    val sagaType: String,
    val correlationId: String?,
    val failedAt: Instant,
    val reason: String?,
    override val aggregateId: String = sagaId,
    override val version: Long = 1L,
    override val occurredOn: Instant = Instant.now(),
    override val eventId: UUID = UUID.randomUUID()
) : DomainEvent(aggregateId, version, occurredOn, eventId) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return SagaFailedEvent(
            sagaId = sagaId,
            sagaType = sagaType,
            correlationId = correlationId,
            failedAt = failedAt,
            reason = reason,
            aggregateId = aggregateId,
            version = version,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
}

/**
 * Saga补偿完成事件
 */
data class SagaCompensatedEvent(
    val sagaId: String,
    val sagaType: String,
    val correlationId: String?,
    val compensatedAt: Instant,
    val reason: String?,
    override val aggregateId: String = sagaId,
    override val version: Long = 1L,
    override val occurredOn: Instant = Instant.now(),
    override val eventId: UUID = UUID.randomUUID()
) : DomainEvent(aggregateId, version, occurredOn, eventId) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return SagaCompensatedEvent(
            sagaId = sagaId,
            sagaType = sagaType,
            correlationId = correlationId,
            compensatedAt = compensatedAt,
            reason = reason,
            aggregateId = aggregateId,
            version = version,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
}

/**
 * 抽象Saga动作基类
 */
abstract class AbstractSagaAction : SagaAction {
    
    /**
     * 执行前置检查
     */
    protected open suspend fun preExecute(data: Map<String, Any>): Boolean {
        return true
    }
    
    /**
     * 执行后置处理
     */
    protected open suspend fun postExecute(data: Map<String, Any>, result: SagaStepResult) {
        // 默认不做任何处理
    }
    
    /**
     * 实际执行逻辑
     */
    protected abstract suspend fun doExecute(data: Map<String, Any>): SagaStepResult
    
    override suspend fun execute(data: Map<String, Any>): SagaStepResult {
        return try {
            if (!preExecute(data)) {
                return SagaStepResult.failed(Exception("Pre-execution check failed"))
            }
            
            val result = doExecute(data)
            postExecute(data, result)
            result
            
        } catch (e: Exception) {
            SagaStepResult.failed(e)
        }
    }
}

/**
 * 命令执行Saga动作
 */
class CommandSagaAction(
    private val commandExecutor: suspend (Map<String, Any>) -> Any
) : AbstractSagaAction() {
    
    override suspend fun doExecute(data: Map<String, Any>): SagaStepResult {
        return try {
            val result = commandExecutor(data)
            SagaStepResult.success(mapOf("result" to result))
        } catch (e: Exception) {
            SagaStepResult.failed(e)
        }
    }
}

/**
 * 事件发布Saga动作
 */
class EventPublishSagaAction(
    private val eventFactory: (Map<String, Any>) -> DomainEvent,
    private val eventBus: com.lifee.common.cqrs.events.EventBus
) : AbstractSagaAction() {
    
    override suspend fun doExecute(data: Map<String, Any>): SagaStepResult {
        return try {
            val event = eventFactory(data)
            eventBus.publish(event)
            SagaStepResult.success(mapOf("publishedEvent" to event))
        } catch (e: Exception) {
            SagaStepResult.failed(e)
        }
    }
}

/**
 * 等待事件Saga动作
 */
class WaitForEventSagaAction(
    private val eventType: KClass<out DomainEvent>,
    private val timeout: Long? = null
) : AbstractSagaAction() {
    
    override suspend fun doExecute(data: Map<String, Any>): SagaStepResult {
        // 这个动作总是返回等待状态
        // 实际的事件处理由SagaOrchestrator负责
        return SagaStepResult.waiting(
            mapOf("waitingForEvent" to (eventType.simpleName ?: "Unknown")),
            "Waiting for event: ${eventType.simpleName ?: "Unknown"}"
        )
    }
}