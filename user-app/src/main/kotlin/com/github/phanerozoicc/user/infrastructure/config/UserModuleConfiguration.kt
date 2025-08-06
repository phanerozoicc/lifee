package com.github.phanerozoicc.user.infrastructure.config

import com.github.phanerozoicc.base.domain.DomainEvent
import com.github.phanerozoicc.base.domain.DomainEventPublisher
import com.github.phanerozoicc.user.application.command.*
import com.github.phanerozoicc.user.application.query.*
import com.github.phanerozoicc.user.application.service.UserApplicationService
import com.github.phanerozoicc.user.domain.cqrs.CommandBus
import com.github.phanerozoicc.user.domain.cqrs.QueryBus
import com.github.phanerozoicc.user.domain.model.PasswordSpecification
import com.github.phanerozoicc.user.domain.model.UserSpecification
import com.github.phanerozoicc.user.domain.repository.UserRepository
import com.github.phanerozoicc.user.domain.service.UserDomainService
import com.github.phanerozoicc.user.infrastructure.cqrs.CommandBusImpl
import com.github.phanerozoicc.user.infrastructure.cqrs.QueryBusImpl
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * 用户模块配置类
 */
@Configuration
@EnableJpaRepositories(
    basePackages = ["com.github.phanerozoicc.user.infrastructure.persistence.repository"]
)
@EntityScan(
    basePackages = ["com.github.phanerozoicc.user.infrastructure.persistence.entity"]
)
@EnableTransactionManagement
class UserModuleConfiguration {
    
    /**
     * 密码策略
     */
    @Bean
    fun passwordSpecification(): PasswordSpecification {
        return PasswordSpecification()
    }
    
    /**
     * 用户策略
     */
    @Bean
    fun userSpecification(): UserSpecification {
        return UserSpecification()
    }
    
    /**
     * 领域事件发布器
     */
    // 暂时使用spring事件总线
    @Bean
    fun domainEventPublisher(applicationEventPublisher: ApplicationEventPublisher): DomainEventPublisher {
        return object : DomainEventPublisher {
            override fun publish(event: DomainEvent) {
                applicationEventPublisher.publishEvent(event)
            }
        }
    }
    
    /**
     * 用户域服务
     */
    @Bean
    fun userDomainService(
        userRepository: UserRepository
    ): UserDomainService {
        return UserDomainService(userRepository)
    }
    
    /**
     * 命令总线
     */
    @Bean
    fun commandBus(applicationContext: ApplicationContext): CommandBus {
        return CommandBusImpl(applicationContext)
    }
    
    /**
     * 查询总线
     */
    @Bean
    fun queryBus(applicationContext: ApplicationContext): QueryBus {
        return QueryBusImpl(applicationContext)
    }
    
    /**
     * 用户注册命令处理器
     */
    @Bean
    fun registerUserCommandHandler(
        userRepository: UserRepository,
        userDomainService: UserDomainService,
        domainEventPublisher: DomainEventPublisher
    ): RegisterUserCommandHandler {
        return RegisterUserCommandHandler(
            userRepository,
            userDomainService,
            domainEventPublisher
        )
    }
    
    /**
     * 用户登录命令处理器
     */
    @Bean
    fun loginUserCommandHandler(
        userRepository: UserRepository,
        domainEventPublisher: DomainEventPublisher
    ): LoginUserCommandHandler {
        return LoginUserCommandHandler(userRepository, domainEventPublisher)
    }
    
    /**
     * 更新用户资料命令处理器
     */
    @Bean
    fun updateUserProfileCommandHandler(
        userRepository: UserRepository,
        userDomainService: UserDomainService,
        domainEventPublisher: DomainEventPublisher
    ): UpdateUserProfileCommandHandler {
        return UpdateUserProfileCommandHandler(
            userRepository,
            userDomainService,
            domainEventPublisher
        )
    }
    
    /**
     * 修改密码命令处理器
     */
    @Bean
    fun changePasswordCommandHandler(
        userRepository: UserRepository,
        domainEventPublisher: DomainEventPublisher
    ): ChangePasswordCommandHandler {
        return ChangePasswordCommandHandler(userRepository, domainEventPublisher)
    }
    
    /**
     * 验证邮箱命令处理器
     */
    @Bean
    fun verifyEmailCommandHandler(
        userRepository: UserRepository,
        domainEventPublisher: DomainEventPublisher
    ): VerifyEmailCommandHandler {
        return VerifyEmailCommandHandler(userRepository, domainEventPublisher)
    }
    
    /**
     * 获取用户资料查询处理器
     */
    @Bean
    fun getUserProfileQueryHandler(
        userRepository: UserRepository
    ): GetUserProfileQueryHandler {
        return GetUserProfileQueryHandler(userRepository)
    }
    
    /**
     * 获取用户权限查询处理器
     */
    @Bean
    fun getUserPermissionsQueryHandler(
        userRepository: UserRepository
    ): GetUserPermissionsQueryHandler {
        return GetUserPermissionsQueryHandler(userRepository)
    }
    
    /**
     * 获取用户列表查询处理器
     */
    @Bean
    fun getUserListQueryHandler(
        userRepository: UserRepository
    ): GetUserListQueryHandler {
        return GetUserListQueryHandler(userRepository)
    }
    
    /**
     * 搜索用户查询处理器
     */
    @Bean
    fun searchUsersQueryHandler(
        userRepository: UserRepository
    ): SearchUsersQueryHandler {
        return SearchUsersQueryHandler(userRepository)
    }
    
    /**
     * 获取用户统计查询处理器
     */
    @Bean
    fun getUserStatisticsQueryHandler(
        userRepository: UserRepository
    ): GetUserStatisticsQueryHandler {
        return GetUserStatisticsQueryHandler(userRepository)
    }
    
    /**
     * 获取用户偏好设置查询处理器
     */
    @Bean
    fun getUserPreferencesQueryHandler(
        userRepository: UserRepository
    ): GetUserPreferencesQueryHandler {
        return GetUserPreferencesQueryHandler(userRepository)
    }
    
    /**
     * 获取用户活动记录查询处理器
     */
    @Bean
    fun getUserActivityQueryHandler(
        userRepository: UserRepository
    ): GetUserActivityQueryHandler {
        return GetUserActivityQueryHandler(userRepository)
    }
    
    /**
     * 获取用户安全报告查询处理器
     */
    @Bean
    fun getUserSecurityReportQueryHandler(
        userRepository: UserRepository,
        userDomainService: UserDomainService
    ): GetUserSecurityReportQueryHandler {
        return GetUserSecurityReportQueryHandler(userRepository, userDomainService)
    }
    
    /**
     * 验证唯一性查询处理器
     */
    @Bean
    fun validateUniquenessQueryHandler(
        userRepository: UserRepository
    ): ValidateUniquenessQueryHandler {
        return ValidateUniquenessQueryHandler(userRepository)
    }
    
    /**
     * 获取需要关注的用户查询处理器
     */
    @Bean
    fun getUsersNeedingAttentionQueryHandler(
        userRepository: UserRepository,
        userDomainService: UserDomainService
    ): GetUsersNeedingAttentionQueryHandler {
        return GetUsersNeedingAttentionQueryHandler(userRepository, userDomainService)
    }
    
    /**
     * 导出用户数据查询处理器
     */
    @Bean
    fun exportUserDataQueryHandler(
        userRepository: UserRepository
    ): ExportUserDataQueryHandler {
        return ExportUserDataQueryHandler(userRepository)
    }
    
    /**
     * 用户应用服务
     */
    @Bean
    fun userApplicationService(
        commandBus: CommandBus,
        queryBus: QueryBus
    ): UserApplicationService {
        return UserApplicationService(commandBus, queryBus)
    }
}