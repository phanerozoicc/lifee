package com.lifee.user.app.handlers

import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.exceptions.BusinessRuleException
import com.lifee.user.app.commands.RegisterUserCommand
import com.lifee.user.domain.*
import com.lifee.user.domain.services.UserFactory
import com.lifee.user.domain.events.UserRegisteredEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

/**
 * 用户注册命令处理器
 * 
 * 负责处理用户注册业务逻辑，包括：
 * 1. 验证邮箱唯一性
 * 2. 创建用户实体
 * 3. 生成激活令牌
 * 4. 发布用户注册事件
 * 
 * @param userRepository 用户仓储，用于用户数据的持久化操作
 * @param activationTokenRepository 激活令牌仓储，用于激活令牌的持久化操作
 * @param eventBus 事件总线，用于发布领域事件
 * @param transactionTemplate 事务模板，用于事务管理
 * @param userFactory 用户工厂，用于创建用户实体
 */
@Component
class RegisterUserCommandHandler(
    private val userRepository: UserRepository,
    private val activationTokenRepository: ActivationTokenRepository,
    private val eventBus: EventBus,
    private val transactionTemplate: TransactionTemplate,
    private val userFactory: UserFactory
) : AsyncCommandHandler<RegisterUserCommand, Unit> {
    
    private val logger = LoggerFactory.getLogger(RegisterUserCommandHandler::class.java)
    
    /**
     * 处理用户注册命令
     * 
     * 执行完整的用户注册流程：
     * 1. 验证邮箱唯一性，防止重复注册
     * 2. 创建密码值对象，确保密码安全性
     * 3. 通过用户工厂创建用户聚合根
     * 4. 持久化用户数据到数据库
     * 5. 生成激活令牌用于邮箱验证
     * 6. 发布用户注册事件，触发后续业务流程
     * 
     * @param command 用户注册命令，包含注册所需的用户信息
     * @throws BusinessRuleException 当邮箱已存在时抛出业务规则异常
     * @throws ValidationException 当输入参数不符合业务规则时抛出验证异常
     */
    override suspend fun handle(command: RegisterUserCommand) {
        logger.info("处理用户注册命令: {}", command.email)
        
        // 步骤1: 验证邮箱是否已存在，确保邮箱唯一性
        val email = Email.of(command.email)
        val emailExists = userRepository.existsByEmail(email)
        
        BusinessRuleException.throwIf(
            emailExists,
            "邮箱 ${command.email} 已被注册"
        )
        
        // 步骤2: 创建密码值对象，自动进行密码加密
        val password = Password.fromPlainText(command.password)
        
        // 步骤3: 通过用户工厂创建用户聚合根，确保业务规则一致性
        val user = userFactory.createUser(
            email = email,
            password = password,
            firstName = command.firstName,
            lastName = command.lastName
        )
        
        // 步骤4: 持久化用户数据，获取生成的用户ID
        val savedUser = userRepository.save(user)
        
        // 步骤5: 生成激活令牌，用于邮箱验证流程
        val activationToken = ActivationToken.generate(savedUser.getId())
        activationTokenRepository.save(activationToken)
        
        // 步骤6: 发布领域事件，触发邮件发送等后续流程
        val domainEvents = savedUser.getDomainEvents()
        eventBus.publishAll(domainEvents)
        savedUser.clearDomainEvents()
        
        logger.info("用户注册成功: userId={}, email={}", savedUser.getId(), command.email)
    }
}