package com.lifee.user.app.handlers

import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.exceptions.BusinessRuleException
import com.lifee.user.app.commands.UpdateUserProfileCommand
import com.lifee.user.domain.UserId
import com.lifee.user.domain.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

/**
 * 更新用户档案命令处理器
 */
@Component
class UpdateUserProfileCommandHandler(
    private val userRepository: UserRepository,
    private val eventBus: EventBus,
    private val transactionTemplate: TransactionTemplate
) : AsyncCommandHandler<UpdateUserProfileCommand, Unit> {
    
    private val logger = LoggerFactory.getLogger(UpdateUserProfileCommandHandler::class.java)
    
    override suspend fun handle(command: UpdateUserProfileCommand) {
        logger.info("处理更新用户档案命令: userId={}", command.userId)
        
        transactionTemplate.execute { _ ->
            // 查找用户
        val userId = UserId.fromString(command.userId)
        val user = userRepository.findById(userId)
            ?: throw BusinessRuleException("用户不存在: ${command.userId}")
        
        // 更新用户档案
        user.updateProfile(
            firstName = command.firstName,
            lastName = command.lastName,
            dateOfBirth = command.dateOfBirth,
            phoneNumber = command.phoneNumber,
            avatar = command.avatar
        )
        
        // 保存用户
        val savedUser = userRepository.save(user)
        
        // 发布领域事件
        eventBus.publishAll(savedUser.getDomainEvents())
        savedUser.clearDomainEvents()
        
            logger.info("用户档案更新成功: userId={}", command.userId)
        }
    }
}