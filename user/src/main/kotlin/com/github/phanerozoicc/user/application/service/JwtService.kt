package com.github.phanerozoicc.user.application.service

import com.github.phanerozoicc.user.domain.model.User

interface JwtService {
    /**
     * 生成访问令牌
     * @param user 用户
     * @param rememberMe 是否记住我(延长过期时间)
     * @param sessionId 会话ID(可选 用于绑定会话和token失效)
     */
    fun generateAccessToken(user: User, rememberMe: Boolean, sessionId: String?): String
    /**
     * 生成刷新令牌
     * @param user 用户
     * @param rememberMe 是否记住我(延长过期时间)
     * @param sessionId 会话ID(可选 用于绑定会话和token失效)
     */
    fun generateRefreshToken(user: User, rememberMe: Boolean, sessionId: String?): String

    /**
     * 刷新令牌
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    fun refreshToken(refreshToken: String): String
}