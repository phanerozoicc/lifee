package com.lifee.user.app.config

import com.lifee.user.domain.services.UserIdGenerator
import com.lifee.user.infrastructure.services.RedisUserIdGenerator
import org.redisson.api.RedissonClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 用户ID生成器配置
 */
@Configuration
class UserIdGeneratorConfig {
    
    @Bean
    fun userIdGenerator(redissonClient: RedissonClient): UserIdGenerator {
        return RedisUserIdGenerator(redissonClient)
    }
}