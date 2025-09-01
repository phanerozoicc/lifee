package com.github.phanerozoicc.user.application.service

import com.github.phanerozoicc.user.domain.model.User

interface JwtService {
    fun generateAccessToken(user: User, rememberMe: Boolean, sessionId: String?)
    fun generateRefreshToken(user: User, rememberMe: Boolean, sessionId: String?)
}