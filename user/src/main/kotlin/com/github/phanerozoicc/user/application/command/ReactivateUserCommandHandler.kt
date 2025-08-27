package com.github.phanerozoicc.user.application.command

import com.github.phanerozoicc.base.command.Command
import com.github.phanerozoicc.base.command.CommandHandler
import com.github.phanerozoicc.user.application.service.EmailService
import com.github.phanerozoicc.user.domain.model.ActivationToken
import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.repository.ActivationTokenRepository
import com.github.phanerozoicc.user.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.time.ZoneId


class ReactivateUserCommand(
    val email: Email
):  Command()


@Component
class ReactivateUserCommandHandler(
    private val userRepository: UserRepository,
    private val activationTokenRepository: ActivationTokenRepository,
    private val transitionTemplate: TransactionTemplate,
    private val emailService: EmailService
): CommandHandler<ReactivateUserCommand,  Unit> {


    /**
     * 处理重新激活的逻辑
     */
    override fun handle(command: ReactivateUserCommand) {
        // 根据email获取用户
        val user = userRepository.findByEmail(command.email)?: throw Exception("user not exist")
        // 检查用户状态
        if (user.getStatus().isActive()) {
            throw IllegalStateException("user is activated")
        }
        // 获取最近的邮件重发时间
        var lastEmailSent = activationTokenRepository.findByUserId(user.id)
        val lastEmailSentTime = lastEmailSent?.let {
            LocalDateTime.ofInstant(it.createdAt, ZoneId.systemDefault())
        }
        if (user.canReactivate(lastEmailSentTime)) {
            // 判断激活信息是否过期 若过期则重新生成 未过期则使用已有信息
            lastEmailSent?.let {
                if (it.isExpired()) {
                    activationTokenRepository.delete(it)
                    lastEmailSent = null
                }
            }
            if (lastEmailSent == null) {
                lastEmailSent = ActivationToken.generate(user.id)
                transitionTemplate.execute {
                    activationTokenRepository.save(lastEmailSent)
                }
            }
            CoroutineScope(Dispatchers.IO).async {
                emailService.sendActivationEmail(
                    user.id,
                    user.getEmail(),
                    ActivationToken.generate(user.id).value,
                    user.getProfile().nickname
                )
            }
        }
    }

}