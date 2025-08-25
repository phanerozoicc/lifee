package com.github.phanerozoicc.user.infrastructure.config

import com.github.phanerozoicc.base.command.CommandBus
import com.github.phanerozoicc.base.command.DefaultCommandBus
import com.github.phanerozoicc.base.queries.DefaultQueryBus
import com.github.phanerozoicc.base.queries.QueryBus
import com.github.phanerozoicc.user.application.command.*
import com.github.phanerozoicc.user.application.query.*
import com.github.phanerozoicc.user.bak.application.query.ExportUserDataQueryHandler
import com.github.phanerozoicc.user.bak.application.query.GetUserActivityQueryHandler
import com.github.phanerozoicc.user.bak.application.query.GetUserPermissionsQueryHandler
import com.github.phanerozoicc.user.bak.application.query.GetUserPreferencesQueryHandler
import com.github.phanerozoicc.user.application.query.GetUserProfileQueryHandler
import com.github.phanerozoicc.user.bak.application.query.GetUserSecurityReportQueryHandler
import com.github.phanerozoicc.user.bak.application.query.GetUserStatisticsQueryHandler
import com.github.phanerozoicc.user.bak.application.query.GetUsersNeedingAttentionQueryHandler
import com.github.phanerozoicc.user.bak.application.query.SearchUsersQueryHandler
import com.github.phanerozoicc.user.bak.application.query.ValidateUniquenessQueryHandler
import com.github.phanerozoicc.user.domain.repository.UserRepository
import com.github.phanerozoicc.user.domain.service.UserDomainService
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

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
class UserModuleConfiguration {

    /**
     * 命令总线
     */
    @Bean
    fun userCommandBus(): CommandBus {
        return DefaultCommandBus()
    }
    
//    // 暂时使用spring事件总线
//    @Bean
//    fun domainEventPublisher(applicationEventPublisher: ApplicationEventPublisher): DomainEventPublisher {
//        return object : DomainEventPublisher {
//            override fun publish(event: DomainEvent) {
//                applicationEventPublisher.publishEvent(event)
//            }
//        }
//    }



    /**
     * 查询总线
     */
    @Bean
    fun queryBus(): QueryBus {
        return DefaultQueryBus()
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
    
}