package com.github.phanerozoicc.user.application.command

import com.github.phanerozoicc.base.command.Command
import com.github.phanerozoicc.base.command.CommandHandler
import com.github.phanerozoicc.base.event.EventBus
import com.github.phanerozoicc.user.domain.model.UserId
import com.github.phanerozoicc.user.domain.repository.UserRepository
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

/**
 * 修改密码命令
 */
data class ChangePasswordCommand(
    val userId: UserId,
    val currentPassword: String,
    val newPassword: String,
    val ipAddress: String? = null,
) : Command()


/**
 * 修改密码命令处理器
 */
@Service
class ChangePasswordCommandHandler(
    private val userRepository: UserRepository,
    private val eventBus: EventBus,
    private val transactionTemplate: TransactionTemplate,
) : CommandHandler<ChangePasswordCommand, Unit> {

    companion object: KLogging()

    override fun handle(command: ChangePasswordCommand) {
        // 验证命令
        validate(command)

        try {
            val user = userRepository.findById(command.userId)
                ?: throw IllegalStateException("User with id ${command.userId} not found")

            // 修改密码
            user.changePassword(
                oldPassword = command.currentPassword,
                newPassword = command.newPassword,
                ipAddress = command.ipAddress
            )


            val savedUser = transactionTemplate.execute {
                runBlocking {
                    // 保存用户
                    userRepository.save(user)
                }
            } ?: throw IllegalStateException("Failed to save user ${command.userId}")
            eventBus.publishAll(savedUser.getUnCommittedEvents())
            savedUser.markEventsAsCommitted()
        } catch (e: Exception) {
            logger.error("Failed to change password for user ${command.userId}: ${e.message}", e)
            throw e
        }
    }

    private fun validate(command: ChangePasswordCommand) {
        val errors = mutableMapOf<String, MutableList<String>>()

        if (command.currentPassword.isBlank()) {
            errors.getOrPut("oldPassword") { mutableListOf() }.add("原密码不能为空")
        }

        if (command.newPassword.isBlank()) {
            errors.getOrPut("newPassword") { mutableListOf() }.add("新密码不能为空")
        }

        if (errors.isNotEmpty()) {
            throw IllegalArgumentException(errors.toString())
        }
    }
}
