package com.lifee.knowledge.domain.valueobjects

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.assertions.throwables.shouldThrow
import java.util.UUID

/**
 * 知识库值对象测试
 */
class ValueObjectsTest : BehaviorSpec({
    
    given("知识库名称值对象") {
        
        `when`("创建有效的知识库名称") {
            val name = KnowledgeBaseName("有效的知识库名称")
            
            then("应该成功创建") {
                name.value shouldBe "有效的知识库名称"
            }
        }
        
        `when`("创建空的知识库名称") {
            then("应该抛出异常") {
                shouldThrow<IllegalArgumentException> {
                    KnowledgeBaseName("")
                }
            }
        }
        
        `when`("创建过长的知识库名称") {
            val longName = "a".repeat(256) // 假设最大长度为255
            
            then("应该抛出异常") {
                shouldThrow<IllegalArgumentException> {
                    KnowledgeBaseName(longName)
                }
            }
        }
        
        `when`("比较知识库名称") {
            val name1 = KnowledgeBaseName("测试名称")
            val name2 = KnowledgeBaseName("测试名称")
            val name3 = KnowledgeBaseName("不同名称")
            
            then("相同名称应该相等") {
                name1 shouldBe name2
                name1.hashCode() shouldBe name2.hashCode()
            }
            
            then("不同名称应该不相等") {
                name1 shouldNotBe name3
            }
        }
    }
    
    given("知识库描述值对象") {
        
        `when`("创建有效的描述") {
            val description = KnowledgeBaseDescription("这是一个有效的描述")
            
            then("应该成功创建") {
                description.value shouldBe "这是一个有效的描述"
            }
        }
        
        `when`("创建空描述") {
            val description = KnowledgeBaseDescription("")
            
            then("应该允许空描述") {
                description.value shouldBe ""
            }
        }
        
        `when`("创建过长的描述") {
            val longDescription = "a".repeat(1001) // 假设最大长度为1000
            
            then("应该抛出异常") {
                shouldThrow<IllegalArgumentException> {
                    KnowledgeBaseDescription(longDescription)
                }
            }
        }
    }
    
    given("文档标题值对象") {
        
        `when`("创建有效的文档标题") {
            val title = DocumentTitle("有效的文档标题")
            
            then("应该成功创建") {
                title.value shouldBe "有效的文档标题"
            }
        }
        
        `when`("创建空的文档标题") {
            then("应该抛出异常") {
                shouldThrow<IllegalArgumentException> {
                    DocumentTitle("")
                }
            }
        }
        
        `when`("创建过长的文档标题") {
            val longTitle = "a".repeat(256) // 假设最大长度为255
            
            then("应该抛出异常") {
                shouldThrow<IllegalArgumentException> {
                    DocumentTitle(longTitle)
                }
            }
        }
    }
    
    given("文档内容值对象") {
        
        `when`("创建有效的文档内容") {
            val content = DocumentContent("这是有效的文档内容")
            
            then("应该成功创建") {
                content.value shouldBe "这是有效的文档内容"
            }
        }
        
        `when`("创建空的文档内容") {
            val content = DocumentContent("")
            
            then("应该允许空内容") {
                content.value shouldBe ""
            }
        }
        
        `when`("获取内容长度") {
            val content = DocumentContent("测试内容")
            
            then("应该返回正确的长度") {
                content.length() shouldBe 4
            }
        }
        
        `when`("检查内容是否为空") {
            val emptyContent = DocumentContent("")
            val nonEmptyContent = DocumentContent("非空内容")
            
            then("应该正确识别空内容") {
                emptyContent.isEmpty() shouldBe true
                nonEmptyContent.isEmpty() shouldBe false
            }
        }
    }
    
    given("文档类型枚举") {
        
        `when`("检查所有文档类型") {
            then("应该包含所有预期的类型") {
                DocumentType.values() shouldBe arrayOf(
                    DocumentType.TEXT,
                    DocumentType.MARKDOWN,
                    DocumentType.PDF,
                    DocumentType.WORD,
                    DocumentType.HTML
                )
            }
        }
        
        `when`("检查文档类型属性") {
            then("TEXT类型应该有正确的属性") {
                DocumentType.TEXT.displayName shouldBe "纯文本"
                DocumentType.TEXT.extension shouldBe "txt"
                DocumentType.TEXT.mimeType shouldBe "text/plain"
            }
            
            then("MARKDOWN类型应该有正确的属性") {
                DocumentType.MARKDOWN.displayName shouldBe "Markdown"
                DocumentType.MARKDOWN.extension shouldBe "md"
                DocumentType.MARKDOWN.mimeType shouldBe "text/markdown"
            }
            
            then("PDF类型应该有正确的属性") {
                DocumentType.PDF.displayName shouldBe "PDF文档"
                DocumentType.PDF.extension shouldBe "pdf"
                DocumentType.PDF.mimeType shouldBe "application/pdf"
            }
        }
    }
    
    given("文档ID值对象") {
        
        `when`("创建文档ID") {
            val uuid = UUID.randomUUID()
            val documentId = DocumentId(uuid)
            
            then("应该正确存储UUID") {
                documentId.value shouldBe uuid
            }
        }
        
        `when`("比较文档ID") {
            val uuid = UUID.randomUUID()
            val id1 = DocumentId(uuid)
            val id2 = DocumentId(uuid)
            val id3 = DocumentId(UUID.randomUUID())
            
            then("相同UUID的ID应该相等") {
                id1 shouldBe id2
                id1.hashCode() shouldBe id2.hashCode()
            }
            
            then("不同UUID的ID应该不相等") {
                id1 shouldNotBe id3
            }
        }
    }
    
    given("知识库ID值对象") {
        
        `when`("创建知识库ID") {
            val uuid = UUID.randomUUID()
            val knowledgeBaseId = KnowledgeBaseId(uuid)
            
            then("应该正确存储UUID") {
                knowledgeBaseId.value shouldBe uuid
            }
        }
        
        `when`("生成新的知识库ID") {
            val id1 = KnowledgeBaseId.generate()
            val id2 = KnowledgeBaseId.generate()
            
            then("每次生成的ID应该不同") {
                id1 shouldNotBe id2
            }
        }
    }
    
    given("用户ID值对象") {
        
        `when`("创建用户ID") {
            val uuid = UUID.randomUUID()
            val userId = UserId(uuid)
            
            then("应该正确存储UUID") {
                userId.value shouldBe uuid
            }
        }
        
        `when`("从字符串创建用户ID") {
            val uuidString = UUID.randomUUID().toString()
            val userId = UserId.fromString(uuidString)
            
            then("应该正确解析UUID") {
                userId.value.toString() shouldBe uuidString
            }
        }
        
        `when`("从无效字符串创建用户ID") {
            then("应该抛出异常") {
                shouldThrow<IllegalArgumentException> {
                    UserId.fromString("invalid-uuid")
                }
            }
        }
    }
})