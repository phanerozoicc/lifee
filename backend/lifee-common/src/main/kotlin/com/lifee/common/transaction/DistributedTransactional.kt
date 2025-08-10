package com.lifee.common.transaction

import io.seata.spring.annotation.GlobalTransactional
import org.springframework.core.annotation.AliasFor
import org.springframework.transaction.annotation.Transactional

/**
 * 分布式事务注解
 * 结合本地事务和分布式事务
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Transactional
@GlobalTransactional
annotation class DistributedTransactional(
    /**
     * 事务名称
     */
    @get:AliasFor(annotation = GlobalTransactional::class, attribute = "name")
    val name: String = "",
    
    /**
     * 事务超时时间（毫秒）
     */
    @get:AliasFor(annotation = GlobalTransactional::class, attribute = "timeoutMills")
    val timeoutMills: Int = 60000,
    
    /**
     * 回滚异常类型
     */
    @get:AliasFor(annotation = GlobalTransactional::class, attribute = "rollbackFor")
    val rollbackFor: Array<kotlin.reflect.KClass<out Throwable>> = [Exception::class]
)