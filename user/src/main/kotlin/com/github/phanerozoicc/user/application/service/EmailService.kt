package com.github.phanerozoicc.user.application.service

import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.model.UserId



// TODO: 实现邮件发送功能
interface EmailService {
    suspend fun sendActivationEmail(userId: UserId, email: Email, activationToken: String, nickname: String)
}