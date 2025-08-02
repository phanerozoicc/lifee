package com.github.phanerozoicc.infrastructure.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

/**
 * JWT令牌提供者
 */
@Component
class JwtTokenProvider {

    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    @Value("\${app.jwt.secret:mySecretKey}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.expiration:86400000}")
    private var jwtExpirationMs: Long = 86400000 // 24小时

    @Value("\${app.jwt.refresh-expiration:604800000}")
    private var jwtRefreshExpirationMs: Long = 604800000 // 7天

    private val key: Key by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    /**
     * 生成访问令牌
     */
    fun generateAccessToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserPrincipal
        return generateToken(userPrincipal.userId, userPrincipal.username, jwtExpirationMs, "access")
    }

    /**
     * 生成刷新令牌
     */
    fun generateRefreshToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserPrincipal
        return generateToken(userPrincipal.userId, userPrincipal.username, jwtRefreshExpirationMs, "refresh")
    }

    /**
     * 生成令牌
     */
    private fun generateToken(userId: String, username: String, expiration: Long, tokenType: String): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .setSubject(userId)
            .claim("username", username)
            .claim("type", tokenType)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    /**
     * 从令牌中获取用户ID
     */
    fun getUserIdFromToken(token: String): String {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        return claims.subject
    }

    /**
     * 从令牌中获取用户名
     */
    fun getUsernameFromToken(token: String): String {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        return claims["username"] as String
    }

    /**
     * 获取令牌类型
     */
    fun getTokenType(token: String): String {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        return claims["type"] as String
    }

    /**
     * 验证令牌
     */
    fun validateToken(token: String): Boolean {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            return true
        } catch (ex: SecurityException) {
            logger.error("Invalid JWT signature: {}", ex.message)
        } catch (ex: MalformedJwtException) {
            logger.error("Invalid JWT token: {}", ex.message)
        } catch (ex: ExpiredJwtException) {
            logger.error("Expired JWT token: {}", ex.message)
        } catch (ex: UnsupportedJwtException) {
            logger.error("Unsupported JWT token: {}", ex.message)
        } catch (ex: IllegalArgumentException) {
            logger.error("JWT claims string is empty: {}", ex.message)
        }
        return false
    }

    /**
     * 检查令牌是否即将过期（1小时内）
     */
    fun isTokenExpiringSoon(token: String): Boolean {
        try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body

            val expiration = claims.expiration
            val now = Date()
            val oneHourFromNow = Date(now.time + 3600000) // 1小时

            return expiration.before(oneHourFromNow)
        } catch (ex: Exception) {
            return true
        }
    }

    /**
     * 获取令牌过期时间
     */
    fun getExpirationFromToken(token: String): Date? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
            claims.expiration
        } catch (ex: Exception) {
            null
        }
    }
}