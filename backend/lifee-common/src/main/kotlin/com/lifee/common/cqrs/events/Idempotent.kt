package com.lifee.common.cqrs.events

/**
 * 幂等性注解
 * 标记需要幂等性保证的事件处理方法
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Idempotent(
    /**
     * 幂等性键的生成策略
     */
    val keyStrategy: IdempotentKeyStrategy = IdempotentKeyStrategy.EVENT_ID,
    
    /**
     * 自定义键表达式（当keyStrategy为CUSTOM时使用）
     */
    val customKey: String = "",
    
    /**
     * 幂等性记录的过期时间（小时）
     */
    val expireHours: Long = 24
)

/**
 * 幂等性键生成策略
 */
enum class IdempotentKeyStrategy {
    /**
     * 使用事件ID作为幂等性键
     */
    EVENT_ID,
    
    /**
     * 使用聚合根ID + 事件类型作为幂等性键
     */
    AGGREGATE_EVENT_TYPE,
    
    /**
     * 使用自定义表达式生成幂等性键
     */
    CUSTOM
}