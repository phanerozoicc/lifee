package com.lifee.time.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * 时间追踪模块配置
 */
@Configuration
@ComponentScan(basePackages = ["com.lifee.time"])
@EntityScan(basePackages = ["com.lifee.time.infrastructure.entity"])
@EnableJpaRepositories(basePackages = ["com.lifee.time.infrastructure.repository"])
@EnableTransactionManagement
class TimeTrackingConfiguration