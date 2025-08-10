package com.lifee.user.app.services

import com.lifee.user.domain.User
import com.lifee.user.domain.services.JwtService
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT服务实现
 */
@Service
class JwtServiceImpl(
    @Value("\${spring.security.jwt.secret}")
    private val jwtSecret: String,
    
    @Value("\${spring.security.jwt.expiration}")
    private val jwtExpiration: Long,
    
    @Value("\${spring.security.jwt.refresh-expiration:604800}")
    private val refreshTokenExpiration: Long = 604800 // 7天
) : JwtService {
    
    private val logger = LoggerFactory.getLogger(JwtServiceImpl::class.java)
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    
    override fun generateAccessToken(user: User): String {
        val now = Instant.now()
        val expiryDate = now.plus(jwtExpiration, ChronoUnit.SECONDS)
        
        return Jwts.builder()
            .setSubject(user.getId().value)
            .claim("email", user.getEmail())
            .claim("type", "access")
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiryDate))
            .signWith(secretKey, SignatureAlgorithm.HS512)
            .compact()
    }
    
    override fun generateRefreshToken(user: User): String {
        val now = Instant.now()
        val expiryDate = now.plus(refreshTokenExpiration, ChronoUnit.SECONDS)
        
        return Jwts.builder()
            .setSubject(user.getId().value)
            .claim("email", user.getEmail())
            .claim("type", "refresh")
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiryDate))
            .signWith(secretKey, SignatureAlgorithm.HS512)
            .compact()
    }
    
    override fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: JwtException) {
            logger.debug("JWT令牌验证失败: {}", e.message)
            false
        } catch (e: IllegalArgumentException) {
            logger.debug("JWT令牌格式错误: {}", e.message)
            false
        }
    }
    
    override fun extractUserId(token: String): String? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
            claims.subject
        } catch (e: Exception) {
            logger.debug("提取用户ID失败: {}", e.message)
            null
        }
    }
    
    override fun extractEmail(token: String): String? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
            claims["email"] as? String
        } catch (e: Exception) {
            logger.debug("提取邮箱失败: {}", e.message)
            null
        }
    }
    
    override fun getTokenExpiration(token: String): Instant? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
            claims.expiration?.toInstant()
        } catch (e: Exception) {
            logger.debug("提取令牌过期时间失败: {}", e.message)
            null
        }
    }
    
    override fun getAccessTokenExpirationSeconds(): Long {
        return jwtExpiration
    }
}