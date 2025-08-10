package com.lifee.common.cqrs.commands

/**
 * 同步命令处理器接口
 * 用于处理不需要异步操作的命令，如简单的业务逻辑计算
 */
interface CommandHandler<T : Command, R> {
    fun handle(command: T): R
}

/**
 * 异步命令处理器接口
 * 用于处理需要异步操作的命令，如数据库I/O、外部API调用、邮件发送等
 */
interface AsyncCommandHandler<T : Command, R> {
    suspend fun handle(command: T): R
}
