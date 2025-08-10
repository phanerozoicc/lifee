package com.lifee.knowledge.domain.services

import com.lifee.knowledge.domain.exceptions.InvalidDocumentException
import com.lifee.knowledge.domain.valueobjects.DocumentContent
import com.lifee.knowledge.domain.valueobjects.DocumentTitle
import com.lifee.knowledge.domain.valueobjects.DocumentType
import org.springframework.stereotype.Service
import java.util.regex.Pattern

/**
 * 文档验证服务
 * 负责文档内容的业务规则验证
 */
@Service
class DocumentValidationService {
    
    companion object {
        // 文档大小限制（字节）
        const val MAX_DOCUMENT_SIZE_BYTES = 50 * 1024 * 1024 // 50MB
        const val MAX_TEXT_DOCUMENT_LENGTH = 500_000 // 50万字符
        const val MAX_MARKDOWN_DOCUMENT_LENGTH = 1_000_000 // 100万字符
        const val MIN_DOCUMENT_LENGTH = 10 // 最少10个字符
        
        // 标题限制
        const val MAX_TITLE_LENGTH = 200
        const val MIN_TITLE_LENGTH = 1
        
        // 支持的文档格式
        private val SUPPORTED_TEXT_FORMATS = setOf("TEXT", "MARKDOWN", "HTML")
        private val SUPPORTED_BINARY_FORMATS = setOf("PDF", "DOC", "DOCX", "XLS", "XLSX", "PPT", "PPTX")
        
        // 危险内容模式
        private val SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val IFRAME_PATTERN = Pattern.compile("<iframe[^>]*>.*?</iframe>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        private val OBJECT_PATTERN = Pattern.compile("<object[^>]*>.*?</object>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        
        // 敏感词列表
        private val SENSITIVE_WORDS = setOf(
            "机密", "绝密", "内部资料", "商业机密",
            "密码", "口令", "token", "secret",
            "政治敏感内容", "法轮功", "六四"
        )
    }
    
    /**
     * 验证文档
     */
    fun validateDocument(
        title: DocumentTitle,
        content: DocumentContent,
        type: DocumentType,
        fileSizeBytes: Long? = null
    ) {
        validateTitle(title)
        validateDocumentType(type)
        validateDocumentSize(content, type, fileSizeBytes)
        validateDocumentContent(content, type)
        validateSecurity(content, type)
    }
    
    /**
     * 验证文档标题
     */
    private fun validateTitle(title: DocumentTitle) {
        val titleValue = title.value
        
        if (titleValue.length < MIN_TITLE_LENGTH) {
            throw InvalidDocumentException("文档标题不能为空")
        }
        
        if (titleValue.length > MAX_TITLE_LENGTH) {
            throw InvalidDocumentException("文档标题长度不能超过${MAX_TITLE_LENGTH}个字符，当前长度：${titleValue.length}")
        }
        
        // 检查标题中的特殊字符
        if (titleValue.contains(Regex("[<>\"'&]")) && !titleValue.contains("&lt;") && !titleValue.contains("&gt;")) {
            throw InvalidDocumentException("文档标题包含不允许的特殊字符")
        }
        
        // 检查是否全是空白字符
        if (titleValue.trim().isEmpty()) {
            throw InvalidDocumentException("文档标题不能全是空白字符")
        }
    }
    
    /**
     * 验证文档类型
     */
    private fun validateDocumentType(type: DocumentType) {
        val typeValue = type.value.uppercase()
        
        if (typeValue !in SUPPORTED_TEXT_FORMATS && typeValue !in SUPPORTED_BINARY_FORMATS) {
            throw InvalidDocumentException(
                "不支持的文档格式：${type.value}。支持的格式：${(SUPPORTED_TEXT_FORMATS + SUPPORTED_BINARY_FORMATS).joinString()}"
            )
        }
    }
    
    /**
     * 验证文档大小
     */
    private fun validateDocumentSize(
        content: DocumentContent,
        type: DocumentType,
        fileSizeBytes: Long?
    ) {
        val typeValue = type.value.uppercase()
        
        // 验证文件大小（如果提供）
        if (fileSizeBytes != null && fileSizeBytes > MAX_DOCUMENT_SIZE_BYTES) {
            throw InvalidDocumentException(
                "文档大小超过限制。最大允许：${MAX_DOCUMENT_SIZE_BYTES / 1024 / 1024}MB，当前：${fileSizeBytes / 1024 / 1024}MB"
            )
        }
        
        // 验证文本内容长度
        if (typeValue in SUPPORTED_TEXT_FORMATS) {
            val contentLength = content.getLength()
            
            if (contentLength < MIN_DOCUMENT_LENGTH) {
                throw InvalidDocumentException("文档内容太短，至少需要${MIN_DOCUMENT_LENGTH}个字符")
            }
            
            val maxLength = when (typeValue) {
                "MARKDOWN" -> MAX_MARKDOWN_DOCUMENT_LENGTH
                else -> MAX_TEXT_DOCUMENT_LENGTH
            }
            
            if (contentLength > maxLength) {
                throw InvalidDocumentException(
                    "${typeValue}文档内容长度超过限制。最大允许：${maxLength}字符，当前：${contentLength}字符"
                )
            }
        }
    }
    
    /**
     * 验证文档内容格式
     */
    private fun validateDocumentContent(content: DocumentContent, type: DocumentType) {
        val typeValue = type.value.uppercase()
        val contentValue = content.value
        
        when (typeValue) {
            "MARKDOWN" -> validateMarkdownContent(contentValue)
            "HTML" -> validateHtmlContent(contentValue)
            "TEXT" -> validateTextContent(contentValue)
        }
    }
    
    /**
     * 验证Markdown内容
     */
    private fun validateMarkdownContent(content: String) {
        // 检查是否有过多的嵌套标题
        val headerCount = content.count { it == '#' }
        if (headerCount > content.length * 0.1) {
            throw InvalidDocumentException("Markdown文档包含过多的标题标记")
        }
        
        // 检查是否有恶意链接
        val linkPattern = Pattern.compile("\\[.*?\\]\\(.*?\\)")
        val matcher = linkPattern.matcher(content)
        while (matcher.find()) {
            val link = matcher.group()
            if (link.contains("javascript:") || link.contains("data:")) {
                throw InvalidDocumentException("Markdown文档包含不安全的链接")
            }
        }
    }
    
    /**
     * 验证HTML内容
     */
    private fun validateHtmlContent(content: String) {
        // 检查危险的HTML标签
        if (SCRIPT_PATTERN.matcher(content).find()) {
            throw InvalidDocumentException("HTML文档不能包含script标签")
        }
        
        if (IFRAME_PATTERN.matcher(content).find()) {
            throw InvalidDocumentException("HTML文档不能包含iframe标签")
        }
        
        if (OBJECT_PATTERN.matcher(content).find()) {
            throw InvalidDocumentException("HTML文档不能包含object标签")
        }
        
        // 检查是否有过多的HTML标签
        val tagCount = content.count { it == '<' }
        if (tagCount > content.length * 0.2) {
            throw InvalidDocumentException("HTML文档包含过多的标签")
        }
    }
    
    /**
     * 验证纯文本内容
     */
    private fun validateTextContent(content: String) {
        // 检查是否包含过多的特殊字符
        val specialCharCount = content.count { !it.isLetterOrDigit() && !it.isWhitespace() }
        if (specialCharCount > content.length * 0.3) {
            throw InvalidDocumentException("文本文档包含过多的特殊字符")
        }
        
        // 检查是否有过多的重复字符
        if (hasExcessiveRepeatedChars(content)) {
            throw InvalidDocumentException("文本文档包含过多的重复字符")
        }
    }
    
    /**
     * 验证安全性
     */
    private fun validateSecurity(content: DocumentContent, type: DocumentType) {
        val contentValue = content.value.lowercase()
        
        // 检查敏感词
        for (sensitiveWord in SENSITIVE_WORDS) {
            if (contentValue.contains(sensitiveWord.lowercase())) {
                throw InvalidDocumentException("文档内容包含敏感信息，请检查后重新提交")
            }
        }
        
        // 检查是否包含可疑的编码内容
        if (contentValue.contains("base64") && contentValue.contains("data:")) {
            throw InvalidDocumentException("文档不能包含base64编码的数据")
        }
    }
    
    /**
     * 检查是否有过多连续重复字符
     */
    private fun hasExcessiveRepeatedChars(text: String, maxRepeats: Int = 20): Boolean {
        var count = 1
        var prevChar = text.firstOrNull() ?: return false
        
        for (i in 1 until text.length) {
            val currentChar = text[i]
            if (currentChar == prevChar) {
                count++
                if (count > maxRepeats) {
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
     * 清理文档内容
     */
    fun cleanDocumentContent(content: String, type: DocumentType): String {
        val typeValue = type.value.uppercase()
        
        return when (typeValue) {
            "TEXT" -> cleanTextContent(content)
            "MARKDOWN" -> cleanMarkdownContent(content)
            "HTML" -> cleanHtmlContent(content)
            else -> content.trim()
        }
    }
    
    /**
     * 清理文本内容
     */
    private fun cleanTextContent(content: String): String {
        return content
            .trim()
            .replace(Regex("\\s+"), " ") // 将多个连续空白字符替换为单个空格
            .replace(Regex("\\n{4,}"), "\n\n\n") // 将多个连续换行符限制为最多3个
    }
    
    /**
     * 清理Markdown内容
     */
    private fun cleanMarkdownContent(content: String): String {
        return content
            .trim()
            .replace(Regex("\\n{4,}"), "\n\n\n") // 限制连续换行符
    }
    
    /**
     * 清理HTML内容
     */
    private fun cleanHtmlContent(content: String): String {
        return content
            .trim()
            .replace(Regex("\\s+"), " ") // 清理多余空白
            .replace(Regex(">\\s+<"), "><") // 清理标签间的空白
    }
}