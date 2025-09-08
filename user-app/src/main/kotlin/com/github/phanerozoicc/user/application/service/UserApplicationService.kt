package com.github.phanerozoicc.user.application.service

import mu.KLogging
import org.springframework.stereotype.Service


@Service
class UserApplicationService(
    private val jwtService: JwtService
) {

    companion object: KLogging()

    /**
     * 刷新令牌
     */
    fun refreshToken(refreshToken: String): String {
        return try {
            jwtService.refreshToken(refreshToken)
        } catch (e: Exception) {
            logger.warn("Failed to refresh token: ${e.message}", e)
            throw e
        }
    }
}