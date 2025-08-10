package com.lifee.user.app.handlers

import com.lifee.shared.cqrs.AsyncCommandHandler
import com.lifee.user.app.commands.ActivateUserByTokenCommand
import com.lifee.user.domain.repositories.ActivationTokenRepository
import com.lifee.user.domain.repositories.UserRepository
import com.lifee.shared.cqrs.EventBus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 通过激活令牌激活用户命令处理器
 */
@Component
class ActivateUserByTokenCommandHandler(
    private val userRepository: UserRepository,
    private val activationTokenRepository: ActivationTokenRepository,
    private val eventBus: EventBus
) : AsyncCommandHandler<ActivateUserByTokenCommand> {
    
    private val logger = LoggerFactory.getLogger(ActivateUserByTokenCommandHandler::class.java)
    
    @Transactional
    override suspend fun handle(command: ActivateUserByTokenCommand) {
        logger.info("处理通过令牌激活用户命令: token={}", command.token)
        
        // 查找激活令牌
        val activationToken = activationTokenRepository.findByToken(command.token)
            ?: throw IllegalArgumentException("无效的激活令牌")
        
        // 检查令牌是否过期
        if (activationToken.isExpired()) {
            // 删除过期令牌
            activationTokenRepository.delete(activationToken)
            throw IllegalArgumentException("激活令牌已过期")
        }
        
        // 查找用户
        val user = userRepository.findById(activationToken.userId)
            ?: throw IllegalArgumentException("用户不存在")
        
        // 检查用户是否已激活
        if (user.isActivated()) {
            // 删除已使用的令牌
            activationTokenRepository.delete(activationToken)
            throw IllegalArgumentException("用户已激活")
        }
        
        // 激活用户
        user.activate()
        
        // 保存用户
        userRepository.save(user)
        
        // 删除已使用的激活令牌
        activationTokenRepository.delete(activationToken)
        
        // 发布领域事件
        eventBus.publishAll(user.getDomainEvents())
        user.clearDomainEvents()
        
        logger.info("用户激活成功: userId={}", user.id.