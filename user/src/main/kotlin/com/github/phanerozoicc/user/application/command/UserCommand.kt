package com.github.phanerozoicc.user.application.command

import com.github.phanerozoicc.user.bak.domain.cqrs.CommandResult
import com.github.phanerozoicc.user.domain.model.UserId
import java.time.LocalDateTime

/**
 * 命令基类
 */
abstract class UserCommand {
    abstract val commandId: String
    abstract val timestamp: LocalDateTime
    abstract val userId: UserId?
    abstract val ipAddress: String?
    abstract val userAgent: String?
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



