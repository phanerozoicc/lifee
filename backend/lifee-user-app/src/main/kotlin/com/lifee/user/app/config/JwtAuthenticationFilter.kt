package com.lifee.user.app.config

import com.lifee.user.domain.services.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT认证过滤器
 */
@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {
    
    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = extractTokenFromRequest(request)
            
            if (token != null && jwtService.validateToken(token)) {
                val userId = jwtService.extractUserId(token)
                val email = jwtService.extractEmail(token)
                
                if (userId != null && email != null) {
                    // 创建认证对象
                    val authentication = UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        emptyList() // 暂时不处理角色权限
                    )
                    
                    // 设置认证详情
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    
                    // 设置到安全上下文
                    SecurityContextHolder.getContext().authentication = authentication
                    
                    logger.debug("JWT认证成功: userId={}, email={}", userId, email)
                }
            }
        } catch (e: Exception) {
            logger.debug("JWT认证失败: {}", e.message)
            // 清除安全上下文
            SecurityContextHolder.clearContext()
        }
        
        filterChain.doFilter(request, response)
    }
    
    /**
     * 从请求中提取JWT令牌
     */
    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}