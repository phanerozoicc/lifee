package com.github.phanerozoicc.base.command

interface CommandHandler<T> {
    handle(command: T): Comm
}

abstract class CommandBus {
    protected val handlers = mutableMapOf<String, (Any) -> Unit>()

    fun <T> registerHandler(commandHandler: CommandH) {
        handler::class.supertypes.first { it.arguments.isNotEmpty() }
            .arguments[0].
    }

    fun dispatch(command: Any) {
        handlers[command::class.qualifiedName]?.invoke(command)
            ?: throw IllegalArgumentException("未注册处理器: ${command::class.simpleName}")
    }
}