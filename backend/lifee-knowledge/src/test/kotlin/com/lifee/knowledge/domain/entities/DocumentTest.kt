package com.lifee.knowledge.domain.entities

import com.lifee.knowledge.domain.valueobjects.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.assertions.throwables.shouldThrow
import java.time.Instant
import java.util.UUID

/**
 * 文档实体测试
 */
class DocumentTest : BehaviorSpec({
    
    given("文档实体") {
        
        `when`("创建新文档") {
            val documentId = DocumentId(UUID.randomUUID())
            val title = DocumentTitle("测试文档")
            val content = DocumentContent("这是测试文档的内容")
            val type = DocumentType.TEXT
            val now = Instant.now()
            
            val document = Document(
                id = documentId,
                title = title,
                content = content,
                type = type,
                createdAt = now,
                updatedAt = now
            )
            
            then("应该正确初始化文档") {
                document.getId() shouldBe documentId
                document.getTitle() shouldBe title
                document.getContent() shouldBe content
                document.getType() shouldBe type
                document.getCreatedAt() shouldBe now
                document.getUpdatedAt() shouldBe now
            }
        }
        
        `when`("更新文档内容") {
            val document = createTestDocument()
            val newTitle = DocumentTitle("更新后的标题")
            val newContent = DocumentContent("更新后的内容")
            val originalUpdatedAt = document.getUpdatedAt()
            
            // 等待一毫秒确保时间戳不同
            Thread.sleep(1)
            
            document.updateContent(newTitle, newContent)
            
            then("应该成功更新内容") {
                document.getTitle() shouldBe newTitle
                document.getContent() shouldBe newContent
                document.getUpdatedAt() shouldNotBe originalUpdatedAt
            }
        }
        
        `when`("更新文档类型") {
            val document = createTestDocument()
            val newType = DocumentType.MARKDOWN
            val originalUpdatedAt = document.getUpdatedAt()
            
            Thread.sleep(1)
            
            document.updateType(newType)
            
            then("应该成功更新类型") {
                document.getType() shouldBe newType
                document.getUpdatedAt() shouldNotBe originalUpdatedAt
            }
        }
        
        `when`("检查文档相等性") {
            val documentId = DocumentId(UUID.randomUUID())
            val document1 = Document(
                id = documentId,
                title = DocumentTitle("文档1"),
                content = DocumentContent("内容1"),
                type = DocumentType.TEXT,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            
            val document2 = Document(
                id = documentId, // 相同的ID
                title = DocumentTitle("文档2"), // 不同的标题
                content = DocumentContent("内容2"), // 不同的内容
                type = DocumentType.MARKDOWN, // 不同的类型
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            
            then("具有相同ID的文档应该相等") {
                document1 shouldBe document2
                document1.hashCode() shouldBe document2.hashCode()
            }
        }
        
        `when`("检查文档不相等性") {
            val document1 = createTestDocument()
            val document2 = createTestDocument() // 不同的ID
            
            then("具有不同ID的文档应该不相等") {
                document1 shouldNotBe document2
            }
        }
        
        `when`("获取文档摘要") {
            val longContent = "这是一个很长的文档内容，" + "用于测试文档摘要功能。".repeat(20)
            val document = Document(
                id = DocumentId(UUID.randomUUID()),
                title = DocumentTitle("长文档"),
                content = DocumentContent(longContent),
                type = DocumentType.TEXT,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            
            then("应该返回截断的摘要") {
                val summary = document.getSummary(50)
                summary.length shouldBe 50
                summary shouldBe longContent.substring(0, 50)
            }
        }
        
        `when`("获取短文档的摘要") {
            val shortContent = "短内容"
            val document = Document(
                id = DocumentId(UUID.randomUUID()),
                title = DocumentTitle("短文档"),
                content = DocumentContent(shortContent),
                type = DocumentType.TEXT,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            
            then("应该返回完整内容") {
                val summary = document.getSummary(50)
                summary shouldBe shortContent
            }
        }
        
        `when`("检查文档是否为空") {
            val emptyDocument = Document(
                id = DocumentId(UUID.randomUUID()),
                title = DocumentTitle("空文档"),
                content = DocumentContent(""),
                type = DocumentType.TEXT,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            
            val nonEmptyDocument = createTestDocument()
            
            then("空文档应该被识别为空") {
                emptyDocument.isEmpty() shouldBe true
                nonEmptyDocument.isEmpty() shouldBe false
            }
        }
    }
})

/**
 * 创建测试用的文档
 */
private fun createTestDocument(): Document {
    return Document(
        id = DocumentId(UUID.randomUUID()),
        title = DocumentTitle("测试文档"),
        content = DocumentContent("这是测试文档的内容"),
        type = DocumentType.TEXT,
        createdAt = Instant.now(),
        updatedAt = Instant.now()
    )
}