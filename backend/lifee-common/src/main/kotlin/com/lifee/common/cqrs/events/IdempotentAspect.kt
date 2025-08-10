package com.lifee.common.cqrs.events

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 幂等性切面
 * 为标记了@Idempotent注解的方法提供幂等性保证
 */
@Aspect
@Component
class IdempotentAspect {
    
    private val logger = LoggerFactory.getLogger(IdempotentAspect::class.java)
    
    // 存储已处理的幂等性键
    private val processedKeys = ConcurrentHashMap<String, IdempotentRecord>()
    
    /**
     * 幂等性记录
     */
    data class IdempotentRecord(
        val key: String,
        val processedAt: Long = System.currentTimeMillis(),
        val result: Any? = null
    )
    
    /**
     * 环绕通知，处理幂等性逻辑
     */
    @Around("@annotation(idempotent)")
    fun handleIdempotent(joinPoint: ProceedingJoinPoint, idempotent: Idempotent): Any? {
        val idempotentKey = generateIdempotentKey(joinPoint, idempotent)
        
        // 检查是否已经处理过
        val existingRecord = processedKeys[idempotentKey]
        if (existingRecord != null) {
            logger.debug("Method already processed with idempotent key: {}, returning cached result", idempotentKey)
            return existingRecord.result
        }
        
        try {
            // 执行原方法
            val result = joinPoint.proceed()
            
            // 记录处理结果
            processedKeys[idempotentKey] = IdempotentRecord(
                key = idempotentKey,
                result = result
            )
            
            logger.debug("Method processed successfully with idempotent key: {}", idempotentKey)
            return result
        } catch (e: Exception) {
            logger.error("Error processing method with idempotent key: {}", idempotentKey, e)
            throw e
        }
    }
    
    /**
     * 生成幂等性键
     */
    private fun generateIdempotentKey(joinPoint: ProceedingJoinPoint, idempotent: Idempotent): String {
        val methodName = joinPoint.signature.name
        val className = joinPoint.target::class.simpleName
        
        return when (idempotent.keyStrategy) {
            IdempotentKeyStrategy.EVENT_ID -> {
                val event = findEventParameter(joinPoint)
                if (event != null) {
                    "${className}_${methodName}_${event.aggregateId}_${event::class.simpleName}_${event.occurredOn.toEpochMilli()}"
                } else {
                    "${className}_${methodName}_${System.currentTimeMillis()}"
                }
            }
            
            IdempotentKeyStrategy.AGGREGATE_EVENT_TYPE -> {
                val event = findEventParameter(joinPoint)
                if (event != null) {
                    "${className}_${methodName}_${event.aggregateId}_${event::class.simpleName}"
                } else {
                    "${className}_${methodName}_unknown"
                }
            }
            
            IdempotentKeyStrategy.CUSTOM -> {
                if (idempotent.customKey.isNotBlank()) {
                    evaluateCustomKey(joinPoint, idempotent.customKey)
                } else {
                    "${className}_${methodName}_${System.currentTimeMillis()}"
                }
            }
        }
    }
    
    /**
     * 查找事件参数
     */
    private fun findEventParameter(joinPoint: ProceedingJoinPoint): Event? {
        return joinPoint.args.find { it is Event } as? Event
    }
    
    /**
     * 评估自定义键表达式
     */
    private fun evaluateCustomKey(joinPoint: ProceedingJoinPoint, customKeyExpression: String): String {
        // 简单的表达式评估，可以根据需要扩展
        val className = joinPoint.target::class.simpleName
        val methodName = joinPoint.signature.name
        
        return customKeyExpression
            .replace("\${className}", className ?: "Unknown")
            .replace("\${methodName}", methodName)
            .replace("\${timestamp}", System.currentTimeMillis().toString())
    }
    
    /**
     * 清理过期的幂等性记录
     */
    fun cleanupExpiredRecords(maxAgeHours: Long = 24) {
        val cutoffTime = System.currentTimeMillis() - (maxAgeHours * 60 * 60 * 1000)
        
        val expiredKeys = processedKeys.filter { (_, record) ->
            record.processedAt < cutoffTime
        }.keys
        
        expiredKeys.forEach { key ->
            processedKeys.remove(key)
        }
        
        if (expiredKeys.isNotEmpty()) {
            logger.debug("Cleaned up {} expired idempotent records", expiredKeys.size)
        }
    }
    
    /**
     * 获取当前记录数量
     */
    fun getRecordCount(): Int {
        return processedKeys.size
    }
}