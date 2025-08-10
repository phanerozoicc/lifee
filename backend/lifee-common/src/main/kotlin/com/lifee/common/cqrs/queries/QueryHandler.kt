package com.lifee.common.cqrs.queries

/**
 * 同步查询处理器接口
 * 用于处理不需要异步操作的查询，如简单的数据转换和计算
 */
interface QueryHandler<T : Query, R> {
    fun handle(query: T): R
}

/**
 * 异步查询处理器接口
 * 用于处理需要异步操作的查询，如数据库查询、外部API调用等
 */
interface AsyncQueryHandler<T : Query, R> {
    suspend fun handle(query: T): R
}