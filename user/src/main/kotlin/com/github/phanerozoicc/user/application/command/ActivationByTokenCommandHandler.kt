package com.github.phanerozoicc.user.application.command

import com.github.phanerozoicc.base.command.Command
import com.github.phanerozoicc.base.command.CommandHandler
import com.github.phanerozoicc.base.event.EventBus
import com.github.phanerozoicc.user.domain.repository.ActivationTokenRepository
import com.github.phanerozoicc.user.domain.repository.UserRepository
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

class ActivationByTokenCommand(
    val token: String,
): Command()


@Component
class ActivationByTokenCommandHandler(
    private val userRepository: UserRepository,
    private val activationTokenRepository: ActivationTokenRepository,
    private val eventBus: EventBus,
    private val transactionTemplate: TransactionTemplate,
): CommandHandler<ActivationByTokenCommand, Unit> {

    companion object: KLogging()

    override fun handle(command: ActivationByTokenCommand) {
        // 根据token查询用户
        val activationToken = activationTokenRepository.findByToken(command.token)
            ?: throw IllegalArgumentException("Invalid token: ${command.token}")
        // 检测是否过期
        if (activationToken.isExpired()) {
            // 删除过期的token并抛出异常
            activationTokenRepository.delete(activationToken)
            throw IllegalStateException("Token expired: ${command.token}")
        }
        // 激活用户
        val user = userRepository.findById(activationToken.userId)?:
        throw IllegalStateException("User not found: ${activationToken.userId}")

        user.activate(user.id)
        val savedUser = transactionTemplate.execute {
            runBlocking {
                val savedUser = userRepository.save(user)
                // 删除已使用的 token
                activationTokenRepository.delete(activationToken)
                savedUser
            }
        } ?: throw IllegalStateException("Failed to save user ${activationToken.userId}")
        // 发布事件
        eventBus.publishAll(savedUser.getDomainEvents())
        savedUser.markEventsAsCommitted()
        logger.debug("user activated: {}",  user.id)
    }

}