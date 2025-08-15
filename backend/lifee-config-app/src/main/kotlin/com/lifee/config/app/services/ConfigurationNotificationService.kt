package com.lifee.config.app.services

import com.lifee.config.domain.aggregates.Configuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.Instant

/**
 * 配置通知服务
 * 负责配置变更、发布、回滚等事件的通知
 */
@Service
class ConfigurationNotificationService(
    private val mailSender: JavaMailSender,
    private val restTemplate: RestTemplate,
    @Value("\${lifee.config.notification.enabled:true}")
    private val notificationEnabled: Boolean,
    @Value("\${lifee.config.notification.channels}")
    private val enabledChannels: List<String>,
    @Value("\${lifee.config.notification.email.recipients}")
    private val emailRecipients: List<String>,
    @Value("\${lifee.config.notification.webhook.urls}")
    private val webhookUrls: List<String>
) {
    
    private val logger = LoggerFactory.getLogger(ConfigurationNotificationService::class.java)
    
    /**
     * 通知配置发布
     */
    suspend fun notifyConfigurationPublished(
        configuration: Configuration,
        version: String,
        publisherId: String,
        releaseNotes: String
    ) {
        if (!notificationEnabled) {
            logger.debug("通知功能已禁用，跳过配置发布通知")
            return
        }
        
        logger.info("发送配置发布通知: configurationId={}, version={}", 
            configuration.getId().value, version)
        
        val notification = ConfigurationNotification(
            type = NotificationType.CONFIGURATION_PUBLISHED,
            configurationId = configuration.getId().value,
            namespace = configuration.getNamespace(),
            environment = configuration.getEnvironment(),
            version = version,
            publisherId = publisherId,
            releaseNotes = releaseNotes,
            timestamp = Instant.now()
        )
        
        sendNotification(notification)
    }
    
    /**
     * 通知配置回滚
     */
    suspend fun notifyConfigurationRolledBack(
        configuration: Configuration,
        targetVersion: String,
        rollbackReason: String,
        operatorId: String
    ) {
        if (!notificationEnabled) {
            logger.debug("通知功能已禁用，跳过配置回滚通知")
            return
        }
        
        logger.info("发送配置回滚通知: configurationId={}, targetVersion={}", 
            configuration.getId().value, targetVersion)
        
        val notification = ConfigurationNotification(
            type = NotificationType.CONFIGURATION_ROLLED_BACK,
            configurationId = configuration.getId().value,
            namespace = configuration.getNamespace(),
            environment = configuration.getEnvironment(),
            version = targetVersion,
            publisherId = operatorId,
            releaseNotes = "回滚原因: $rollbackReason",
            timestamp = Instant.now()
        )
        
        sendNotification(notification)
    }
    
    /**
     * 通知配置验证失败
     */
    suspend fun notifyConfigurationValidationFailed(
        configuration: Configuration,
        errors: List<String>,
        operatorId: String
    ) {
        if (!notificationEnabled) {
            logger.debug("通知功能已禁用，跳过配置验证失败通知")
            return
        }
        
        logger.info("发送配置验证失败通知: configurationId={}, errorCount={}", 
            configuration.getId().value, errors.size)
        
        val notification = ConfigurationNotification(
            type = NotificationType.CONFIGURATION_VALIDATION_FAILED,
            configurationId = configuration.getId().value,
            namespace = configuration.getNamespace(),
            environment = configuration.getEnvironment(),
            version = "N/A",
            publisherId = operatorId,
            releaseNotes = "验证错误: ${errors.joinToString("; ")}",
            timestamp = Instant.now()
        )
        
        sendNotification(notification)
    }
    
    /**
     * 通知配置项更新
     */
    suspend fun notifyConfigurationItemUpdated(
        configuration: Configuration,
        updatedKeys: List<String>,
        operatorId: String
    ) {
        if (!notificationEnabled) {
            logger.debug("通知功能已禁用，跳过配置项更新通知")
            return
        }
        
        logger.info("发送配置项更新通知: configurationId={}, updatedKeys={}", 
            configuration.getId().value, updatedKeys)
        
        val notification = ConfigurationNotification(
            type = NotificationType.CONFIGURATION_ITEM_UPDATED,
            configurationId = configuration.getId().value,
            namespace = configuration.getNamespace(),
            environment = configuration.getEnvironment(),
            version = "N/A",
            publisherId = operatorId,
            releaseNotes = "更新的配置项: ${updatedKeys.joinToString(", ")}",
            timestamp = Instant.now()
        )
        
        sendNotification(notification)
    }
    
    /**
     * 发送通知
     */
    private suspend fun sendNotification(notification: ConfigurationNotification) {
        try {
            withContext(Dispatchers.IO) {
                val notificationTasks = enabledChannels.map { channel ->
                    async {
                        when (channel.lowercase()) {
                            "email" -> sendEmailNotification(notification)
                            "webhook" -> sendWebhookNotification(notification)
                            "slack" -> sendSlackNotification(notification)
                            "teams" -> sendTeamsNotification(notification)
                            else -> logger.warn("不支持的通知渠道: {}", channel)
                        }
                    }
                }
                
                notificationTasks.awaitAll()
            }
            
            logger.info("通知发送完成: type={}, configurationId={}", 
                notification.type, notification.configurationId)
            
        } catch (e: Exception) {
            logger.error("发送通知失败: type={}, configurationId={}", 
                notification.type, notification.configurationId, e)
        }
    }
    
    /**
     * 发送邮件通知
     */
    private suspend fun sendEmailNotification(notification: ConfigurationNotification) {
        try {
            if (emailRecipients.isEmpty()) {
                logger.debug("邮件收件人列表为空，跳过邮件通知")
                return
            }
            
            val subject = buildEmailSubject(notification)
            val content = buildEmailContent(notification)
            
            val message = SimpleMailMessage().apply {
                setTo(*emailRecipients.toTypedArray())
                setSubject(subject)
                setText(content)
                setFrom("noreply@lifee.com")
            }
            
            mailSender.send(message)
            logger.debug("邮件通知发送成功: recipients={}", emailRecipients)
            
        } catch (e: Exception) {
            logger.error("发送邮件通知失败", e)
        }
    }
    
    /**
     * 发送Webhook通知
     */
    private suspend fun sendWebhookNotification(notification: ConfigurationNotification) {
        try {
            if (webhookUrls.isEmpty()) {
                logger.debug("Webhook URL列表为空，跳过Webhook通知")
                return
            }
            
            val payload = buildWebhookPayload(notification)
            
            webhookUrls.forEach { url ->
                try {
                    restTemplate.postForEntity(url, payload, String::class.java)
                    logger.debug("Webhook通知发送成功: url={}", url)
                } catch (e: Exception) {
                    logger.error("发送Webhook通知失败: url={}", url, e)
                }
            }
            
        } catch (e: Exception) {
            logger.error("发送Webhook通知失败", e)
        }
    }
    
    /**
     * 发送Slack通知
     */
    private suspend fun sendSlackNotification(notification: ConfigurationNotification) {
        try {
            // TODO: 实现Slack通知
            logger.debug("Slack通知功能待实现")
        } catch (e: Exception) {
            logger.error("发送Slack通知失败", e)
        }
    }
    
    /**
     * 发送Teams通知
     */
    private suspend fun sendTeamsNotification(notification: ConfigurationNotification) {
        try {
            // TODO: 实现Teams通知
            logger.debug("Teams通知功能待实现")
        } catch (e: Exception) {
            logger.error("发送Teams通知失败", e)
        }
    }
    
    /**
     * 构建邮件主题
     */
    private fun buildEmailSubject(notification: ConfigurationNotification): String {
        return when (notification.type) {
            NotificationType.CONFIGURATION_PUBLISHED -> 
                "[配置发布] ${notification.namespace}/${notification.environment} - ${notification.version}"
            NotificationType.CONFIGURATION_ROLLED_BACK -> 
                "[配置回滚] ${notification.namespace}/${notification.environment} - ${notification.version}"
            NotificationType.CONFIGURATION_VALIDATION_FAILED -> 
                "[配置验证失败] ${notification.namespace}/${notification.environment}"
            NotificationType.CONFIGURATION_ITEM_UPDATED -> 
                "[配置更新] ${notification.namespace}/${notification.environment}"
        }
    }
    
    /**
     * 构建邮件内容
     */
    private fun buildEmailContent(notification: ConfigurationNotification): String {
        return buildString {
            appendLine("配置变更通知")
            appendLine("=".repeat(50))
            appendLine("事件类型: ${notification.type.description}")
            appendLine("配置ID: ${notification.configurationId}")
            appendLine("命名空间: ${notification.namespace}")
            appendLine("环境: ${notification.environment}")
            if (notification.version != "N/A") {
                appendLine("版本: ${notification.version}")
            }
            appendLine("操作人: ${notification.publisherId}")
            appendLine("时间: ${notification.timestamp}")
            appendLine()
            appendLine("详细信息:")
            appendLine(notification.releaseNotes)
            appendLine()
            appendLine("此邮件由Lifee配置管理系统自动发送，请勿回复。")
        }
    }
    
    /**
     * 构建Webhook载荷
     */
    private fun buildWebhookPayload(notification: ConfigurationNotification): Map<String, Any> {
        return mapOf(
            "type" to notification.type.name,
            "configurationId" to notification.configurationId,
            "namespace" to notification.namespace,
            "environment" to notification.environment,
            "version" to notification.version,
            "publisherId" to notification.publisherId,
            "releaseNotes" to notification.releaseNotes,
            "timestamp" to notification.timestamp.toString()
        )
    }
}

/**
 * 配置通知
 */
data class ConfigurationNotification(
    val type: NotificationType,
    val configurationId: String,
    val namespace: String,
    val environment: String,
    val version: String,
    val publisherId: String,
    val releaseNotes: String,
    val timestamp: Instant
)

/**
 * 通知类型
 */
enum class NotificationType(val description: String) {
    CONFIGURATION_PUBLISHED("配置发布"),
    CONFIGURATION_ROLLED_BACK("配置回滚"),
    CONFIGURATION_VALIDATION_FAILED("配置验证失败"),
    CONFIGURATION_ITEM_UPDATED("配置项更新")
}