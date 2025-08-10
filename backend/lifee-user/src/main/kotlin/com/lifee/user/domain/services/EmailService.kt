package com.lifee.user.domain.services

import com.lifee.user.domain.Email
import com.lifee.user.domain.UserId

/**
 * 邮件服务接口
 */
interface EmailService {
    
    /**
     * 发送用户激活邮件
     */
    suspend fun sendActivationEmail(userId: UserId, email: Email, firstName: String, lastName: String, activationToken: String? = null)
    
    /**
     * 发送密码重置邮件
     */
    suspend fun sendPasswordResetEmail(userId: UserId, email: Email, firstName: String, lastName: String, resetToken: String? = null)
    
    /**
     * 发送欢迎邮件
     */
    suspend fun sendWelcomeEmail(userId: UserId, email: Email, firstName: String, lastName: String)
}