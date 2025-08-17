package com.lifee.gateway.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

/**
 * 降级处理控制器
 * 当微服务不可用时提供降级响应
 */
@RestController
@RequestMapping("/fallback")
class FallbackController {
    
    /**
     * 用户服务降级
     */
    @GetMapping("/user")
    fun userServiceFallback(): ResponseEntity<FallbackResponse> {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(FallbackResponse(
                service = "user-service",
                message = "用户服务暂时不可用，请稍后重试",
                suggestion = "您可以尝试刷新页面或联系客服",
                timestamp = Instant.now()
            ))
    }
    
    /**
     * 知识库服务降级
     */
    @GetMapping("/knowledge")
    fun knowledgeServiceFallback(): ResponseEntity<FallbackResponse> {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(FallbackResponse(
                service = "knowledge-service",
                message = "知识库服务暂时不可用，请稍后重试",
                suggestion = "您可以尝试使用缓存的知识库内容或稍后重试",
                timestamp = Instant.now()
            ))
    }
    
    /**
     * 对话服务降级
     */
    @GetMapping("/conversation")
    fun conversationServiceFallback(): ResponseEntity<FallbackResponse> {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(FallbackResponse(
                service = "conversation-service",
                message = "对话服务暂时不可用，请稍后重试",
                suggestion = "您可以尝试重新发起对话或查看历史对话记录",
                timestamp = Instant.now()
            ))
    }
    
    /**
     * 推荐服务降级
     */
    @GetMapping("/recommendation")
    fun recommendationServiceFallback(): ResponseEntity<FallbackResponse> {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(FallbackResponse(
                service = "recommendation-service",
                message = "推荐服务暂时不可用，请稍后重试",
                suggestion = "您可以手动浏览知识库内容或稍后查看推荐",
                timestamp = Instant.now()
            ))
    }
    
    /**
     * 配置服务降级
     */
    @GetMapping("/configuration")
    fun configurationServiceFallback(): ResponseEntity<FallbackResponse> {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(FallbackResponse(
                service = "configuration-service",
                message = "配置服务暂时不可用，使用默认配置",
                suggestion = "系统将使用默认配置继续运行，部分个性化设置可能不可用",
                timestamp = Instant.now()
            ))
    }
    
    /**
     * 默认降级处理
     */
    @GetMapping("/default")
    fun defaultFallback(): ResponseEntity<FallbackResponse> {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(FallbackResponse(
                service = "unknown",
                message = "请求的服务暂时不可用",
                suggestion = "请检查请求路径是否正确，或稍后重试",
                timestamp = Instant.now()
            ))
    }
    
    /**
     * 系统维护降级
     */
    @GetMapping("/maintenance")
    fun maintenanceFallback(): ResponseEntity<FallbackResponse> {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(FallbackResponse(
                service = "system",
                message = "系统正在维护中",
                suggestion = "系统维护预计需要30分钟，请稍后访问",
                timestamp = Instant.now()
            ))
    }
}

/**
 * 降级响应数据类
 */
data class FallbackResponse(
    val service: String,
    val message: String,
    val suggestion: String,
    val timestamp: Instant,
    val code: String = "SERVICE_UNAVAILABLE",
    val retryAfter: Long = 30 // 建议重试时间（秒）
)