package com.lifee.gateway.filter

import com.lifee.common.monitoring.SystemMonitoringService
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * 请求监控过滤器
 * 收集API请求的性能指标和统计信息
 */
@Component
class RequestMonitoringFilter(
    private val monitoringService: SystemMonitoringService
) : GlobalFilter, Ordered {
    
    private val logger = LoggerFactory.getLogger(RequestMonitoringFilter::class.java)
    
    companion object {
        private const val REQUEST_START_TIME = "request_start_time"
        private const val REQUEST_ID = "request_id"
    }
    
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val response = exchange.response
        
        // 生成请求ID
        val requestId = UUID.randomUUID().toString()
        exchange.attributes[REQUEST_ID] = requestId
        
        // 记录请求开始时间
        val startTime = System.currentTimeMillis()
        exchange.attributes[REQUEST_START_TIME] = startTime
        
        // 添加请求ID到响应头
        response.headers.add("X-Request-ID", requestId)
        
        // 记录请求信息
        logger.info("请求开始: {} {} - RequestID: {}", 
            request.method, request.uri, requestId)
        
        return chain.filter(exchange)
            .doOnSuccess {
                recordRequestMetrics(exchange, false)
            }
            .doOnError { error ->
                logger.error("请求处理异常: RequestID: {}", requestId, error)
                recordRequestMetrics(exchange, true)
            }
            .doFinally {
                logRequestCompletion(exchange)
            }
    }
    
    /**
     * 记录请求指标
     */
    private fun recordRequestMetrics(exchange: ServerWebExchange, isError: Boolean) {
        try {
            val startTime = exchange.attributes[REQUEST_START_TIME] as? Long ?: return
            val endTime = System.currentTimeMillis()
            val responseTime = endTime - startTime
            
            val request = exchange.request
            val response = exchange.response
            val requestId = exchange.attributes[REQUEST_ID] as? String ?: "unknown"
            
            // 记录到监控服务
            monitoringService.recordRequest(responseTime, isError)
            
            // 记录详细的请求信息
            val requestInfo = RequestInfo(
                requestId = requestId,
                method = request.method?.name ?: "UNKNOWN",
                uri = request.uri.toString(),
                userAgent = request.headers.getFirst("User-Agent") ?: "unknown",
                remoteAddress = getClientIpAddress(exchange),
                statusCode = response.statusCode?.value() ?: 0,
                responseTime = responseTime,
                timestamp = Instant.ofEpochMilli(startTime),
                isError = isError,
                errorMessage = if (isError) getErrorMessage(exchange) else null
            )
            
            // 异步记录请求信息（避免阻塞主流程）
            recordRequestInfo(requestInfo)
            
        } catch (e: Exception) {
            logger.error("记录请求指标失败", e)
        }
    }
    
    /**
     * 记录请求完成日志
     */
    private fun logRequestCompletion(exchange: ServerWebExchange) {
        try {
            val startTime = exchange.attributes[REQUEST_START_TIME] as? Long ?: return
            val endTime = System.currentTimeMillis()
            val responseTime = endTime - startTime
            
            val request = exchange.request
            val response = exchange.response
            val requestId = exchange.attributes[REQUEST_ID] as? String ?: "unknown"
            val statusCode = response.statusCode?.value() ?: 0
            
            logger.info("请求完成: {} {} - Status: {} - Time: {}ms - RequestID: {}",
                request.method, request.uri, statusCode, responseTime, requestId)
                
        } catch (e: Exception) {
            logger.error("记录请求完成日志失败", e)
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private fun getClientIpAddress(exchange: ServerWebExchange): String {
        val request = exchange.request
        
        // 尝试从各种代理头中获取真实IP
        val headers = listOf(
            "X-Forwarded-For",
            "X-Real-IP",
            "X-Original-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        )
        
        for (header in headers) {
            val ip = request.headers.getFirst(header)
            if (!ip.isNullOrBlank() && !"unknown".equals(ip, ignoreCase = true)) {
                // X-Forwarded-For可能包含多个IP，取第一个
                return ip.split(",")[0].trim()
            }
        }
        
        // 如果都没有，返回远程地址
        return request.remoteAddress?.address?.hostAddress ?: "unknown"
    }
    
    /**
     * 获取错误信息
     */
    private fun getErrorMessage(exchange: ServerWebExchange): String? {
        return exchange.attributes["error"] as? String
    }
    
    /**
     * 异步记录请求信息
     */
    private fun recordRequestInfo(requestInfo: RequestInfo) {
        // TODO: 可以将请求信息存储到数据库或发送到日志收集系统
        // 这里暂时只记录到日志
        logger.debug("请求信息: {}", requestInfo)
    }
    
    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE + 1
    }
}

/**
 * 请求信息数据类
 */
data class RequestInfo(
    val requestId: String,
    val method: String,
    val uri: String,
    val userAgent: String,
    val remoteAddress: String,
    val statusCode: Int,
    val responseTime: Long,
    val timestamp: Instant,
    val isError: Boolean,
    val errorMessage: String? = null
)