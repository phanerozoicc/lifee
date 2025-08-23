package com.lifee.chat.domain.services

import com.lifee.chat.domain.exceptions.InvalidMessageContentException
import com.lifee.chat.domain.valueobjects.MessageContent
import com.lifee.chat.domain.valueobjects.MessageType
import org.springframework.stereotype.Service

/**
 * 消息验证服务
 * 负责消息内容的业务规则验证
 */
@Service
class MessageValidationService {
    
    companion object {
        // 消息长度限制
        const val MIN_MESSAGE_LENGTH = 1
        const val MAX_USER_MESSAGE_LENGTH = 5000
        const val MAX_ASSISTANT_MESSAGE_LENGTH = 10000
        const val MAX_SYSTEM_MESSAGE_LENGTH = 1000
        
        // 敏感词列表（实际项目中应该从配置文件或数据库加载）
        private val SENSITIVE_WORDS = setOf(
            "垃圾", "废物", "傻逼", "操你妈", "去死", "滚蛋",
            "政治敏感词", "法轮功", "六四", "天安门",
            "暴力", "恐怖主义", "炸弹", "杀人",
            "色情", "黄色", "裸体", "性交"
        )
        
        // 连续重复字符限制
        const val MAX_CONSECUTIVE_CHARS = 10
    }
    
    /**
     * 验证消息内容
     */
    fun validateMessage(content: MessageContent, type: MessageType) {
        validateLength(content, type)
        validateSensitiveWords(content)
        validateFormat(content)
    }
    
    /**
     * 验证消息长度
     */
    private fun validateLength(content: MessageContent, type: MessageType) {
        val length = content.length
        
        if (length < MIN_MESSAGE_LENGTH) {
            throw InvalidMessageContentException("消息内容不能为空")
        }
        
        val maxLength = when (type) {
            MessageType.USER -> MAX_USER_MESSAGE_LENGTH
            MessageType.ASSISTANT -> MAX_ASSISTANT_MESSAGE_LENGTH
            MessageType.SYSTEM -> MAX_SYSTEM_MESSAGE_LENGTH
        }
        
        if (length > maxLength) {
            throw InvalidMessageContentException("${type.name}消息长度不能超过${maxLength}个字符，当前长度：${length}")
        }
    }
    
    /**
     * 验证敏感词
     */
    private fun validateSensitiveWords(content: MessageContent) {
        val text = content.value.lowercase()
        
        for (sensitiveWord in SENSITIVE_WORDS) {
            if (text.contains(sensitiveWord.lowercase())) {
                throw InvalidMessageContentException("消息内容包含敏感词汇，请修改后重试")
            }
        }
    }
    
    /**
     * 验证消息格式
     */
    private fun validateFormat(content: MessageContent) {
        val text = content.value
        
        // 检查连续重复字符
        if (hasExcessiveRepeatedChars(text)) {
            throw InvalidMessageContentException("消息包含过多连续重复字符")
        }
        
        // 检查是否全是空白字符
        if (text.trim().isEmpty()) {
            throw InvalidMessageContentException("消息内容不能全是空白字符")
        }
        
        // 检查是否包含过多特殊字符
        if (hasExcessiveSpecialChars(text)) {
            throw InvalidMessageContentException("消息包含过多特殊字符")
        }
    }
    
    /**
     * 检查是否有过多连续重复字符
     */
    private fun hasExcessiveRepeatedChars(text: String): Boolean {
        var count = 1
        var prevChar = text.firstOrNull() ?: return false
        
        for (i in 1 until text.length) {
            val currentChar = text[i]
            if (currentChar == prevChar) {
                count++
                if (count > MAX_CONSECUTIVE_CHARS) {
                    return true
                }
            } else {
                count = 1
                prevChar = currentChar
            }
        }
        
        return false
    }
    
    /**
     * 检查是否包含过多特殊字符
     */
    private fun hasExcessiveSpecialChars(text: String): Boolean {
        val specialCharCount = text.count { !it.isLetterOrDigit() && !it.isWhitespace() }
        val totalLength = text.length
        
        // 如果特殊字符超过总长度的50%，认为异常
        return specialCharCount > totalLength * 0.5
    }
    
    /**
     * 清理消息内容（移除多余空白字符）
     */
    fun cleanMessageContent(content: String): String {
        return content
            .trim()
            .replace(Regex("\\s+"), " ") // 将多个连续空白字符替换为单个空格
            .replace(Regex("\\n{3,}"), "\n\n") // 将多个连续换行符替换为最多两个
    }
}