package com.lifee.knowledge.domain.valueobjects

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.assertions.throwables.shouldThrow
import java.util.UUID

/**
 * 知识库值对象测试
 */
class ValueObjectsTest : FunSpec({
    
    context("知识库名称值对象") {
        
        test("创建有效的知识库名称") {
            val name = KnowledgeBaseName("有效的知识库名称")
            name.value shouldBe "有效的知识库名称"
        }
        
        test("创建空的知识库名称应该抛出异常") {
            shouldThrow<IllegalArgumentException> {
                KnowledgeBaseName("")
            }
        }
        
        test("创建过长的知识库名称应该抛出异常") {
            val longName = "a".repeat(256) // 假设最大长度为255
            shouldThrow<IllegalArgumentException> {
                KnowledgeBaseName(longName)
            }
        }
        
        test("比较知识库名称") {
            val name1 = KnowledgeBaseName("测试名称")
            val name2 = KnowledgeBaseName("测试名称")
            val name3 = KnowledgeBaseName("不同名称")
            
            name1 shouldBe name2
            name1.hashCode() shouldBe name2.hashCode()
            name1 shouldNotBe name3
        }
    }
    
    context("知识库描述值对象") {
        
        test("创建有效的描述") {
            val description = KnowledgeBaseDescription("这是一个有效的描述")
            description.value shouldBe "这是一个有效的描述"
        }
        
        test("创建空描述") {
            val description = KnowledgeBaseDescription("")
            description.value shouldBe ""
        }
        
        test("创建过长的描述应该抛出异常") {
            val longDescription = "a".repeat(1001) // 假设最大长度为1000
            shouldThrow<IllegalArgumentException> {
                KnowledgeBaseDescription(longDescription)
            }
        }
    }
    
    context("文档标题值对象") {
        
        test("创建有效的文档标题") {
            val title = DocumentTitle("有效的文档标题")
            title.value shouldBe "有效的文档标题"
        }
        
        test("创建空的文档标题应该抛出异常") {
            shouldThrow<IllegalArgumentException> {
                DocumentTitle("")
            }
        }
        
        test("创建过长的文档标题应该抛出异常") {
            val longTitle = "a".repeat(256) // 假设最大长度为255
            shouldThrow<IllegalArgumentException> {
                DocumentTitle(longTitle)
            }
        }
    }
    
    context("文档内容值对象") {
        
        test("创建有效的文档内容") {
            val content = DocumentContent("这是有效的文档内容")
            content.value shouldBe "这是有效的文档内容"
        }
        
        test("创建空的文档内容") {
            val content = DocumentContent("")
            content.value shouldBe ""
        }
        
        test("获取内容长度") {
            val content = DocumentContent("测试内容")
            content.length() shouldBe 4
        }
        
        test("检查内容是否为空") {
            val emptyContent = DocumentContent("")
            val nonEmptyContent = DocumentContent("非空内容")
            
            emptyContent.isEmpty() shouldBe true
            nonEmptyContent.isEmpty() shouldBe false
        }
    }
    
    context("文档类型枚举") {
        
        test("检查所有文档类型") {
            DocumentType.values() shouldBe arrayOf(
                DocumentType.TEXT,
                DocumentType.MARKDOWN,
                DocumentType.PDF,
                DocumentType.WORD,
                DocumentType.HTML
            )
        }
        
        test("检查TEXT类型属性") {
            DocumentType.TEXT.displayName shouldBe "纯文本"
            DocumentType.TEXT.extension shouldBe "txt"
            DocumentType.TEXT.mimeType shouldBe "text/plain"
        }
        
        test("检查MARKDOWN类型属性") {
            DocumentType.MARKDOWN.displayName shouldBe "Markdown"
            DocumentType.MARKDOWN.extension shouldBe "md"
            DocumentType.MARKDOWN.mimeType shouldBe "text/markdown"
        }
        
        test("检查PDF类型属性") {
            DocumentType.PDF.displayName shouldBe "PDF文档"
            DocumentType.PDF.extension shouldBe "pdf"
            DocumentType.PDF.mimeType shouldBe "application/pdf"
        }
    }
    
    context("文档ID值对象") {
        
        test("创建文档ID") {
            val uuid = UUID.randomUUID()
            val documentId = DocumentId(uuid)
            documentId.value shouldBe uuid
        }
        
        test("比较文档ID") {
            val uuid = UUID.randomUUID()
            val id1 = DocumentId(uuid)
            val id2 = DocumentId(uuid)
            val id3 = DocumentId(UUID.randomUUID())
            
            id1 shouldBe id2
            id1.hashCode() shouldBe id2.hashCode()
            id1 shouldNotBe id3
        }
    }
    
    context("知识库ID值对象") {
        
        test("创建知识库ID") {
            val uuid = UUID.randomUUID()
            val knowledgeBaseId = KnowledgeBaseId(uuid)
            knowledgeBaseId.value shouldBe uuid
        }
        
        test("生成新的知识库ID") {
            val id1 = KnowledgeBaseId.generate()
            val id2 = KnowledgeBaseId.generate()
            id1 shouldNotBe id2
        }
    }
    
    context("用户ID值对象") {
        
        test("创建用户ID") {
            val uuid = UUID.randomUUID()
            val userId = UserId(uuid)
            userId.value shouldBe uuid
        }
        
        test("从字符串创建用户ID") {
            val uuidString = UUID.randomUUID().toString()
            val userId = UserId.fromString(uuidString)
            userId.value.toString() shouldBe uuidString
        }
        
        test("从无效字符串创建用户ID应该抛出异常") {
            shouldThrow<IllegalArgumentException> {
                UserId.fromString("invalid-uuid")
            }
        }
    }
})