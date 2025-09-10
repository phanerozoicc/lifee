package com.github.phanerozoicc.user.application.service

import com.github.phanerozoicc.user.domain.model.User

class JwtServiceImpl(): JwtService {



    override fun generateAccessToken(
        user: User,
        rememberMe: Boolean,
        sessionId: String?
    ): String {
        TODO("Not yet implemented")
    }

    override fun generateRefreshToken(
        user: User,
        rememberMe: Boolean,
        sessionId: String?
    ): String {
        TODO("Not yet implemented")
    }

    override fun refreshToken(refreshToken: String): String {
        TODO("Not yet implemented")
    }
}