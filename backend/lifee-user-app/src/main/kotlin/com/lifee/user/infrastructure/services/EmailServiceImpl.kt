package com.lifee.user.infrastructure.services

import com.lifee.user.domain.Email
import com.lifee.user.domain.UserId
import com.lifee.user.domain.services.EmailService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

/**
 * 邮件服务实现
 */
@Service
class EmailServiceImpl(
    private val mailSender: JavaMailSender,
    @Value("\${lifee.mail.from:noreply@lifee.com}") private val fromEmail: String,
    @Value("\${lifee.mail.activation-url:http://localhost:8080/activate}") private val activationBaseUrl: String
) : EmailService {
    
    private val logger = LoggerFactory.getLogger(EmailServiceImpl::class.java)
    
    override suspend fun sendActivationEmail(userId: UserId, email: Email, firstName: String, lastName: String, activationToken: String?) {
        withContext(Dispatchers.IO) {
            try {
                val message = SimpleMailMessage().apply {
                    setFrom(fromEmail)
                    setTo(email.value)
                    subject = "欢迎注册 Lifee - 请激活您的账户"
                    text = buildActivationEmailContent(userId, firstName, lastName, activationToken)
                }
                
                mailSender.send(message)
                logger.info("激活邮件已发送至: {}", email.value)
            } catch (e: Exception) {
                logger.error("发送激活邮件失败: {}", email.value, e)
                throw e
            }
        }
    }
    
    override suspend fun sendPasswordResetEmail(userId: UserId, email: Email, firstName: String, lastName: String, resetToken: String?) {
        withContext(Dispatchers.IO) {
            try {
                val message = SimpleMailMessage().apply {
                    setFrom(fromEmail)
                    setTo(email.value)
                    subject = "Lifee - 密码重置请求"
                    text = buildPasswordResetEmailContent(userId, firstName, lastName, resetToken)
                }
                
                mailSender.send(message)
                logger.info("密码重置邮件已发送至: {}", email.value)
            } catch (e: Exception) {
                logger.error("发送密码重置邮件失败: {}", email.value, e)
                throw e
            }
        }
    }
    
    override suspend fun sendWelcomeEmail(userId: UserId, email: Email, firstName: String, lastName: String) {
        withContext(Dispatchers.IO) {
            try {
                val message = SimpleMailMessage().apply {
                    setFrom(fromEmail)
                    setTo(email.value)
                    subject = "欢迎加入 Lifee！"
                    text = buildWelcomeEmailContent(firstName, lastName)
                }
                
                mailSender.send(message)
                logger.info("欢迎邮件已发送至: {}", email.value)
            } catch (e: Exception) {
                logger.error("发送欢迎邮件失败: {}", email.value, e)
                throw e
            }
        }
    }
    
    private fun buildActivationEmailContent(userId: UserId, firstName: String, lastName: String, activationToken: String?): String {
        val token = activationToken ?: userId.value
        val activationUrl = "$activationBaseUrl?token=$token"
        return """
            亲爱的 $firstName $lastName，
            
            欢迎注册 Lifee！
            
            为了完成注册，请点击以下链接激活您的账户：
            $activationUrl
            
            如果您没有注册 Lifee 账户，请忽略此邮件。
            
            此链接将在24小时后失效。
            
            感谢您选择 Lifee！
            
            Lifee 团队
        """.trimIndent()
    }
    
    private fun buildPasswordResetEmailContent(userId: UserId, firstName: String, lastName: String, resetToken: String?): String {
        val token = resetToken ?: userId.value
        val resetUrl = "http://localhost:8080/reset-password?token=$token"
        return """
            亲爱的 $firstName $lastName，
            
            我们收到了您的密码重置请求。
            
            请点击以下链接重置您的密码：
            $resetUrl
            
            如果您没有请求重置密码，请忽略此邮件。
            
            此链接将在1小时后失效。
            
            Lifee 团队
        """.trimIndent()
    }
    
    private fun buildWelcomeEmailContent(firstName: String, lastName: String): String {
        return """
            亲爱的 $firstName $lastName，
            
            欢迎加入 Lifee 大家庭！
            
            您的账户已成功激活。现在您可以：
            - 完善个人资料
            - 探索我们的功能
            - 开始您的 Lifee 之旅
            
            如果您有任何问题，请随时联系我们的客服团队。
            
            再次欢迎您！
            
            Lifee 团队
        """.trimIndent()
    }
}