package com.lifee.common.saga

import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.domain.DomainEvent
import com.lifee.common.eventsourcing.EventStore
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Saga编排器
 * 负责协调长流程和补偿事务
 */
@Service
class SagaOrchestrator(
    private val eventBus: EventBus,
    private val eventStore: EventStore? = null
) {
    
    private val logger = LoggerFactory.getLogger(SagaOrchestrator::class.java)
    private val activeSagas = ConcurrentHashMap<String, SagaInstance>()
    private val sagaDefinitions = ConcurrentHashMap<KClass<out Saga>, SagaDefinition>()
    
    /**
     * 注册Saga定义
     */
    fun <T : Saga> registerSaga(sagaClass: KClass<T>, definition: SagaDefinition) {
        logger.info("Registering saga: {}", sagaClass.simpleName)
        sagaDefinitions[sagaClass] = definition
    }
    
    /**
     * 启动Saga实例
     */
    suspend fun <T : Saga> startSaga(
        sagaClass: KClass<T>,
        sagaId: String = UUID.randomUUID().toString(),
        initialData: Map<String, Any> = emptyMap(),
        correlationId: String? = null
    ): String = withContext(Dispatchers.IO) {
        
        val definition = sagaDefinitions[sagaClass]
            ?: throw SagaException("Saga definition not found for ${sagaClass.simpleName}")
        
        val sagaInstance = SagaInstance(
            id = sagaId,
            sagaType = sagaClass.simpleName ?: "Unknown",
            status = SagaStatus.STARTED,
            currentStep = 0,
            data = initialData.toMutableMap(),
            correlationId = correlationId,
            startedAt = Instant.now(),
            definition = definition
        )
        
        activeSagas[sagaId] = sagaInstance
        
        // 持久化Saga实例
        persistSagaInstance(sagaInstance)
        
        logger.info("Started saga {} with ID: {}", sagaClass.simpleName, sagaId)
        
        // 执行第一步
        executeNextStep(sagaId)
        
        sagaId
    }
    
    /**
     * 处理事件并推进Saga
     */
    suspend fun handleEvent(event: DomainEvent) = withContext(Dispatchers.IO) {
        logger.debug("Handling event {} for saga orchestration", event::class.simpleName)
        
        // 查找相关的Saga实例
        val relatedSagas = findSagasByEvent(event)
        
        relatedSagas.forEach { sagaInstance ->
            try {
                processSagaEvent(sagaInstance, event)
            } catch (e: Exception) {
                logger.error("Error processing event {} for saga {}: {}", 
                    event::class.simpleName, sagaInstance.id, e.message, e)
                handleSagaError(sagaInstance, e)
            }
        }
    }
    
    /**
     * 执行Saga的下一步
     */
    private suspend fun executeNextStep(sagaId: String) {
        val sagaInstance = activeSagas[sagaId]
            ?: throw SagaException("Saga instance not found: $sagaId")
        
        val definition = sagaInstance.definition
        val currentStep = sagaInstance.currentStep
        
        if (currentStep >= definition.steps.size) {
            // Saga完成
            completeSaga(sagaInstance)
            return
        }
        
        val step = definition.steps[currentStep]
        
        try {
            logger.debug("Executing step {} for saga {}", currentStep, sagaId)
            
            // 更新Saga状态
            sagaInstance.status = SagaStatus.EXECUTING
            sagaInstance.currentStepName = step.name
            sagaInstance.updatedAt = Instant.now()
            
            // 持久化步骤状态
            persistSagaStep(sagaInstance, step, SagaStepStatus.EXECUTING)
            
            // 执行步骤
            val result = step.execute(sagaInstance.data)
            
            // 更新Saga数据
            sagaInstance.data.putAll(result.data)
            
            when (result.status) {
                SagaStepResult.Status.SUCCESS -> {
                    // 步骤成功，移动到下一步
                    sagaInstance.currentStep++
                    persistSagaStep(sagaInstance, step, SagaStepStatus.COMPLETED)
                    executeNextStep(sagaId)
                }
                SagaStepResult.Status.WAITING -> {
                    // 步骤等待外部事件
                    sagaInstance.status = SagaStatus.WAITING
                    persistSagaStep(sagaInstance, step, SagaStepStatus.WAITING)
                }
                SagaStepResult.Status.FAILED -> {
                    // 步骤失败，开始补偿
                    persistSagaStep(sagaInstance, step, SagaStepStatus.FAILED)
                    startCompensation(sagaInstance, result.error)
                }
            }
            
            // 更新Saga实例
            persistSagaInstance(sagaInstance)
            
        } catch (e: Exception) {
            logger.error("Error executing step {} for saga {}: {}", currentStep, sagaId, e.message, e)
            persistSagaStep(sagaInstance, step, SagaStepStatus.FAILED)
            startCompensation(sagaInstance, e)
        }
    }
    
    /**
     * 开始补偿流程
     */
    private suspend fun startCompensation(sagaInstance: SagaInstance, error: Throwable?) {
        logger.warn("Starting compensation for saga {}: {}", sagaInstance.id, error?.message)
        
        sagaInstance.status = SagaStatus.COMPENSATING
        sagaInstance.compensationReason = error?.message
        sagaInstance.updatedAt = Instant.now()
        
        // 从当前步骤开始向后补偿
        var stepIndex = sagaInstance.currentStep - 1
        
        while (stepIndex >= 0) {
            val step = sagaInstance.definition.steps[stepIndex]
            
            if (step.compensationAction != null) {
                try {
                    logger.debug("Executing compensation for step {} in saga {}", stepIndex, sagaInstance.id)
                    
                    persistSagaStep(sagaInstance, step, SagaStepStatus.COMPENSATING)
                    
                    val compensationResult = step.compensationAction.execute(sagaInstance.data)
                    
                    if (compensationResult.status == SagaStepResult.Status.SUCCESS) {
                        persistSagaStep(sagaInstance, step, SagaStepStatus.COMPENSATED)
                        logger.debug("Compensation successful for step {} in saga {}", stepIndex, sagaInstance.id)
                    } else {
                        persistSagaStep(sagaInstance, step, SagaStepStatus.COMPENSATION_FAILED)
                        logger.error("Compensation failed for step {} in saga {}: {}", 
                            stepIndex, sagaInstance.id, compensationResult.error?.message)
                    }
                    
                } catch (e: Exception) {
                    persistSagaStep(sagaInstance, step, SagaStepStatus.COMPENSATION_FAILED)
                    logger.error("Error during compensation for step {} in saga {}: {}", 
                        stepIndex, sagaInstance.id, e.message, e)
                }
            }
            
            stepIndex--
        }
        
        // 标记Saga为已补偿
        sagaInstance.status = SagaStatus.COMPENSATED
        sagaInstance.completedAt = Instant.now()
        persistSagaInstance(sagaInstance)
        
        // 从活跃Saga中移除
        activeSagas.remove(sagaInstance.id)
        
        logger.info("Saga {} compensation completed", sagaInstance.id)
    }
    
    /**
     * 完成Saga
     */
    private suspend fun completeSaga(sagaInstance: SagaInstance) {
        logger.info("Completing saga {}", sagaInstance.id)
        
        sagaInstance.status = SagaStatus.COMPLETED
        sagaInstance.completedAt = Instant.now()
        persistSagaInstance(sagaInstance)
        
        // 从活跃Saga中移除
        activeSagas.remove(sagaInstance.id)
        
        // 发布Saga完成事件
        val completionEvent = SagaCompletedEvent(
            sagaId = sagaInstance.id,
            sagaType = sagaInstance.sagaType,
            correlationId = sagaInstance.correlationId,
            completedAt = sagaInstance.completedAt!!
        )
        
        eventBus.publish(completionEvent)
    }
    
    /**
     * 处理Saga错误
     */
    private suspend fun handleSagaError(sagaInstance: SagaInstance, error: Exception) {
        logger.error("Saga {} encountered error: {}", sagaInstance.id, error.message, error)
        
        sagaInstance.status = SagaStatus.FAILED
        sagaInstance.errorMessage = error.message
        sagaInstance.updatedAt = Instant.now()
        
        persistSagaInstance(sagaInstance)
        
        // 开始补偿
        startCompensation(sagaInstance, error)
    }
    
    /**
     * 根据事件查找相关的Saga实例
     */
    private fun findSagasByEvent(event: DomainEvent): List<SagaInstance> {
        return activeSagas.values.filter { saga ->
            saga.definition.eventTriggers.any { trigger ->
                trigger.eventType.isInstance(event) && 
                (trigger.correlationProperty == null || 
                 saga.correlationId == getEventProperty(event, trigger.correlationProperty))
            }
        }
    }
    
    /**
     * 处理Saga事件
     */
    private suspend fun processSagaEvent(sagaInstance: SagaInstance, event: DomainEvent) {
        // 更新Saga数据
        sagaInstance.data["lastEvent"] = event
        sagaInstance.updatedAt = Instant.now()
        
        // 如果Saga在等待状态，尝试继续执行
        if (sagaInstance.status == SagaStatus.WAITING) {
            val currentStep = sagaInstance.definition.steps[sagaInstance.currentStep]
            
            // 检查是否是期待的事件
            if (currentStep.waitingForEvent?.isInstance(event) == true) {
                sagaInstance.status = SagaStatus.EXECUTING
                executeNextStep(sagaInstance.id)
            }
        }
    }
    
    /**
     * 获取事件属性值
     */
    private fun getEventProperty(event: DomainEvent, propertyName: String): String? {
        return try {
            val field = event::class.java.getDeclaredField(propertyName)
            field.isAccessible = true
            field.get(event)?.toString()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 持久化Saga实例
     */
    private suspend fun persistSagaInstance(sagaInstance: SagaInstance) {
        // 这里应该持久化到数据库
        // 暂时只记录日志
        logger.debug("Persisting saga instance: {}", sagaInstance.id)
    }
    
    /**
     * 持久化Saga步骤
     */
    private suspend fun persistSagaStep(
        sagaInstance: SagaInstance, 
        step: SagaStep, 
        status: SagaStepStatus
    ) {
        // 这里应该持久化到数据库
        logger.debug("Persisting saga step: {} - {} - {}", sagaInstance.id, step.name, status)
    }
    
    /**
     * 获取Saga实例状态
     */
    fun getSagaStatus(sagaId: String): SagaInstance? {
        return activeSagas[sagaId]
    }
    
    /**
     * 获取所有活跃的Saga
     */
    fun getActiveSagas(): List<SagaInstance> {
        return activeSagas.values.toList()
    }
    
    /**
     * 取消Saga
     */
    suspend fun cancelSaga(sagaId: String, reason: String = "Manual cancellation") {
        val sagaInstance = activeSagas[sagaId]
            ?: throw SagaException("Saga instance not found: $sagaId")
        
        logger.info("Cancelling saga {}: {}", sagaId, reason)
        
        sagaInstance.status = SagaStatus.CANCELLED
        sagaInstance.errorMessage = reason
        sagaInstance.updatedAt = Instant.now()
        
        // 开始补偿
        startCompensation(sagaInstance, Exception(reason))
    }
}

/**
 * Saga异常
 */
class SagaException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)