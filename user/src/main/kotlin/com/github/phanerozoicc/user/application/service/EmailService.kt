package com.github.phanerozoicc.user.application.service

import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.model.UserId



// TODO: 实现邮件发送功能
interface EmailService {
    /**
     * 发送激活邮件
     */
    suspend fun sendActivationEmail(userId: UserId, email: Email, activationToken: String, nickname: String)

    /**
     * 发送欢迎邮件
     */
    suspend fun sendWelcomeEmail(userId: UserId, email: Email, nickname: String)

}