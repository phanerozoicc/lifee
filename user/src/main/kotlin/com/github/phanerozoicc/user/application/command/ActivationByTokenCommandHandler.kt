package com.github.phanerozoicc.user.application.command

import com.github.phanerozoicc.base.command.Command
import com.github.phanerozoicc.base.command.CommandHandler
import com.github.phanerozoicc.user.domain.repository.ActivationTokenRepository
import org.springframework.stereotype.Component

class ActivationByTokenCommand(
    val token: String,
): Command()


@Component
class ActivationByTokenCommandHandler(
    private val activationTokenRepository: ActivationTokenRepository
): CommandHandler<ActivationByTokenCommand, Unit> {
    override fun handle(command: ActivationByTokenCommand) {
        // 根据token查询用户
        val activationToken = activationTokenRepository.findByToken(command.token)
            ?: throw IllegalArgumentException("Invalid token: ${command.token}")

        TODO("Not yet implemented")
    }

}