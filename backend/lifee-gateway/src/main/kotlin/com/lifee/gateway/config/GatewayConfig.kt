package com.lifee.gateway.config

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

/**
 * API网关配置
 * 负责微服务路由、负载均衡、跨域处理等
 * 
 * 扩展优化策略：
 * 1. 路由优化：
 *    - 动态路由：支持运行时动态添加、修改、删除路由规则
 *    - 路由版本管理：支持API版本控制和灰度发布
 *    - 智能路由：基于请求内容、用户特征进行智能路由
 *    - 路由缓存：缓存路由匹配结果提高性能
 * 
 * 2. 负载均衡优化：
 *    - 多种算法：支持轮询、加权轮询、最少连接、一致性哈希等
 *    - 健康检查：实时监控后端服务健康状态
 *    - 故障转移：自动故障检测和流量切换
 *    - 粘性会话：支持基于用户的会话保持
 * 
 * 3. 安全增强：
 *    - 认证授权：集成JWT、OAuth2、RBAC等认证机制
 *    - 限流熔断：实现令牌桶、滑动窗口等限流算法
 *    - 安全头：自动添加安全相关HTTP头
 *    - IP白名单：支持IP白名单和黑名单过滤
 * 
 * 4. 性能优化：
 *    - 连接池：优化HTTP连接池配置
 *    - 请求缓存：缓存GET请求响应结果
 *    - 压缩传输：启用Gzip压缩减少传输量
 *    - 异步处理：使用响应式编程提高并发性能
 * 
 * 5. 监控告警：
 *    - 指标收集：收集请求量、响应时间、错误率等指标
 *    - 链路追踪：集成Zipkin、Jaeger等分布式追踪
 *    - 日志聚合：统一日志格式和收集
 *    - 实时告警：基于阈值的实时告警机制
 */
@Configuration
class GatewayConfig {
    
    /**
     * 配置路由规则
     * 
     * 扩展优化策略：
     * - 路由优先级：基于路径匹配精确度设置路由优先级
     * - 条件路由：支持基于请求头、参数、时间等条件的路由
     * - 路由分组：按业务域或服务类型对路由进行分组管理
     * - 路由监控：监控每个路由的性能指标和健康状态
     */
    @Bean
    fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            // 用户服务路由
            // TODO: 扩展优化 - 用户服务路由增强
            // 建议添加以下功能：
            // 1. 认证过滤器：验证JWT token和用户权限
            // 2. 限流过滤器：基于用户ID或IP的请求限流
            // 3. 缓存过滤器：缓存用户信息查询结果
            // 4. 审计过滤器：记录用户操作日志
            .route("user-service") { r ->
                r.path("/api/users/**")
                    .filters { f ->
                        f.stripPrefix(2) // 移除 /api/users 前缀
                            .addRequestHeader("X-Service", "user-service")
                            .addResponseHeader("X-Response-Time", System.currentTimeMillis().toString())
                            .circuitBreaker { config ->
                                config.name("user-service-cb")
                                    .fallbackUri("forward:/fallback/user")
                            }
                    }
                    .uri("lb://user-service") // 负载均衡到用户服务
            }
            
            // 知识库服务路由
            .route("knowledge-service") { r ->
                r.path("/api/knowledge/**")
                    .filters { f ->
                        f.stripPrefix(2)
                            .addRequestHeader("X-Service", "knowledge-service")
                            .addResponseHeader("X-Response-Time", System.currentTimeMillis().toString())
                            .circuitBreaker { config ->
                                config.name("knowledge-service-cb")
                                    .fallbackUri("forward:/fallback/knowledge")
                            }
                    }
                    .uri("lb://knowledge-service")
            }
            
            // 对话服务路由
            .route("conversation-service") { r ->
                r.path("/api/conversations/**")
                    .filters { f ->
                        f.stripPrefix(2)
                            .addRequestHeader("X-Service", "conversation-service")
                            .addResponseHeader("X-Response-Time", System.currentTimeMillis().toString())
                            .circuitBreaker { config ->
                                config.name("conversation-service-cb")
                                    .fallbackUri("forward:/fallback/conversation")
                            }
                    }
                    .uri("lb://conversation-service")
            }
            
            // 推荐服务路由
            .route("recommendation-service") { r ->
                r.path("/api/recommendations/**")
                    .filters { f ->
                        f.stripPrefix(2)
                            .addRequestHeader("X-Service", "recommendation-service")
                            .addResponseHeader("X-Response-Time", System.currentTimeMillis().toString())
                            .circuitBreaker { config ->
                                config.name("recommendation-service-cb")
                                    .fallbackUri("forward:/fallback/recommendation")
                            }
                    }
                    .uri("lb://recommendation-service")
            }
            
            // 配置服务路由
            .route("configuration-service") { r ->
                r.path("/api/configurations/**")
                    .filters { f ->
                        f.stripPrefix(2)
                            .addRequestHeader("X-Service", "configuration-service")
                            .addResponseHeader("X-Response-Time", System.currentTimeMillis().toString())
                            .circuitBreaker { config ->
                                config.name("configuration-service-cb")
                                    .fallbackUri("forward:/fallback/configuration")
                            }
                    }
                    .uri("lb://configuration-service")
            }
            
            // 健康检查路由
            .route("health-check") { r ->
                r.path("/health/**")
                    .filters { f ->
                        f.addRequestHeader("X-Health-Check", "true")
                    }
                    .uri("lb://health-service")
            }
            
            // 监控服务路由
            .route("monitoring") { r ->
                r.path("/api/monitoring/**")
                    .filters { f ->
                        f.stripPrefix(2)
                            .addRequestHeader("X-Service", "monitoring-service")
                    }
                    .uri("lb://monitoring-service")
            }
            
            // WebSocket路由（用于实时对话）
            .route("websocket-chat") { r ->
                r.path("/ws/**")
                    .filters { f ->
                        f.addRequestHeader("X-WebSocket", "true")
                    }
                    .uri("lb://conversation-service")
            }
            
            // 文件上传路由
            // TODO: 扩展优化 - 文件上传路由增强
            // 建议添加以下功能：
            // 1. 文件类型验证：检查文件扩展名和MIME类型
            // 2. 病毒扫描：集成病毒扫描引擎
            // 3. 上传进度：支持大文件分片上传和进度跟踪
            // 4. 存储优化：支持多种存储后端（本地、OSS、S3）
            .route("file-upload") { r ->
                r.path("/api/files/**")
                    .and()
                    .method("POST", "PUT")
                    .filters { f ->
                        f.stripPrefix(2)
                            .addRequestHeader("X-Service", "file-service")
                            .requestSize(50 * 1024 * 1024) // 限制文件大小50MB
                    }
                    .uri("lb://knowledge-service")
            }
            
            // 认证路由
            .route("auth") { r ->
                r.path("/api/auth/**")
                    .filters { f ->
                        f.stripPrefix(2)
                            .addRequestHeader("X-Service", "auth-service")
                    }
                    .uri("lb://user-service")
            }
            
            // 默认路由（兜底）
            .route("default") { r ->
                r.path("/**")
                    .filters { f ->
                        f.addRequestHeader("X-Default-Route", "true")
                    }
                    .uri("forward:/fallback/default")
            }
            
            .build()
    }
    
    /**
     * CORS配置
     * 
     * 扩展优化策略：
     * - 动态CORS：支持运行时动态修改CORS配置
     * - 环境适配：基于部署环境自动调整CORS策略
     * - 安全增强：限制允许的源和方法，防止CSRF攻击
     * - 性能优化：缓存预检请求结果，减少OPTIONS请求
     */
    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val corsConfig = CorsConfiguration().apply {
            // 允许的源
            allowedOriginPatterns = listOf(
                "http://localhost:*",
                "https://localhost:*",
                "http://127.0.0.1:*",
                "https://127.0.0.1:*",
                "https://*.lifee.com",
                "https://lifee.com"
            )
            
            // 允许的HTTP方法
            allowedMethods = listOf(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
            )
            
            // 允许的请求头
            allowedHeaders = listOf(
                "*"
            )
            
            // 允许携带认证信息
            allowCredentials = true
            
            // 预检请求的缓存时间
            maxAge = 3600L
            
            // 暴露的响应头
            exposedHeaders = listOf(
                "X-Response-Time",
                "X-Service",
                "X-Request-ID",
                "Authorization"
            )
        }
        
        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", corsConfig)
        }
        
        return CorsWebFilter(source)
    }
}