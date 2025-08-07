package com.github.phanerozoicc.user.application.command

import com.github.phanerozoicc.user.domain.model.UserId
import java.time.LocalDateTime
import java.util.*

/**
 * 命令基类
 */
abstract class UserCommand {
    val commandId: String = UUID.randomUUID().toString()
    val timestamp: LocalDateTime = LocalDateTime.now()
    abstract val userId: UserId?
    abstract val ipAddress: String?
    abstract val userAgent: String?
}

/**
 * 命令执行结果
 */
sealed class CommandResult {
    data class Success(val message: String? = null, val data: Any? = null) : CommandResult()
    data class Failure(val error: String, val errorCode: String? = null, val details: Map<String, Any>? = null) : CommandResult()
    data class ValidationError(val errors: Map<String, List<String>>) : CommandResult()
}

/**
 * 命令处理器接口
 */
interface CommandHandler<T : UserCommand> {
    /**
     * 处理命令
     * @param command 要处理的命令
     * @return 命令执行结果
     */
    suspend fun handle(command: T): CommandResult

    /**
     * 验证命令
     * @param command 要验证的命令
     * @return 验证结果
     */
    fun validate(command: T): CommandResult
}

