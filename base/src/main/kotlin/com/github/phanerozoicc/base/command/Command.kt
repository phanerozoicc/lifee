package com.github.phanerozoicc.base.command

import java.time.LocalDateTime
import java.util.*

/**
 * 定义出通用的
 */
abstract class Command {
    val commandType: String = this::class.simpleName ?: throw IllegalStateException("commandType is null")
    val commandId: String = UUID.randomUUID().toString()
    val timestamp: LocalDateTime = LocalDateTime.now()
}


