package com.github.phanerozoicc.lifee.knowledge.domain.document

import com.github.phanerozoicc.lifee.knowledge.domain.document.event.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.LocalDateTime

class DocumentTest : BehaviorSpec({
    
    given("一个新的文档路径和文件信息") {
        val path = DocumentPath("/test/document.pdf")
        val fileInfo = FileInfo(
            size = 1024L,
            lastModified = LocalDateTime.now(),
            hash = "abc123"
        )
        
        `when`("创建文档") {
            val document = Document.create(path, fileInfo)
            
            then("文档应该被正确创建") {
                document.id shouldNotBe null
                document.path shouldBe path
                document.status shouldBe DocumentStatus.DISCOVERED
                document.fileInfo shouldBe fileInfo
            }
            
            then("应该产生文档已发现事件") {
                val events = document.getDomainEvents()
                events.size shouldBe 1
                events[0].shouldBeInstanceOf<DocumentDiscoveredEvent>()
                val event = events[0] as DocumentDiscoveredEvent
                event.documentId shouldBe document.id
                event.path shouldBe path
            }
        }
    }
    
    given("一个已发现状态的文档") {
        val document = Document.create(
            DocumentPath("/test/document.pdf"),
            FileInfo(1024L, LocalDateTime.now(), "abc123")
        )
        document.clearDomainEvents()
        
        `when`("开始解析") {
            document.startParsing()
            
            then("文档状态应该变为解析中") {
                document.status shouldBe DocumentStatus.PARSING
            }
            
            then("应该产生解析开始事件") {
                val events = document.getDomainEvents()
                events.size shouldBe 1
                events[0].shouldBeInstanceOf<DocumentParsingStartedEvent>()
            }
        }
        
        `when`("尝试从非法状态开始解析") {
            document.startParsing() // 先设置为PARSING状态
            document.clearDomainEvents()
            
            then("应该抛出异常") {
                shouldThrow<IllegalArgumentException> {
                    document.startParsing()
                }
            }
        }
    }
    
    given("一个解析中状态的文档") {
        val document = Document.create(
            DocumentPath("/test/document.pdf"),
            FileInfo(1024L, LocalDateTime.now(), "abc123")
        )
        document.startParsing()
        document.clearDomainEvents()
        
        `when`("完成解析") {
            val segmentIds = listOf(
                ContentSegmentId("segment1"),
                ContentSegmentId("segment2")
            )
            document.completeParsing(segmentIds)
            
            then("文档状态应该变为已完成") {
                document.status shouldBe DocumentStatus.COMPLETED
                document.metadata.segmentIds shouldBe segmentIds
            }
            
            then("应该产生解析完成事件") {
                val events = document.getDomainEvents()
                events.size shouldBe 1
                events[0].shouldBeInstanceOf<DocumentParsingCompletedEvent>()
            }
        }
        
        `when`("解析失败") {
            val errorMessage = "解析失败：不支持的文件格式"
            document.failParsing(errorMessage)
            
            then("文档状态应该变为失败") {
                document.status shouldBe DocumentStatus.FAILED
                document.metadata.errorMessage shouldBe errorMessage
            }
            
            then("应该产生解析失败事件") {
                val events = document.getDomainEvents()
                events.size shouldBe 1
                events[0].shouldBeInstanceOf<DocumentParsingFailedEvent>()
            }
        }
    }
    
    given("一个已完成状态的文档") {
        val document = Document.create(
            DocumentPath("/test/document.pdf"),
            FileInfo(1024L, LocalDateTime.now(), "abc123")
        )
        document.startParsing()
        document.completeParsing(listOf(ContentSegmentId("segment1")))
        document.clearDomainEvents()
        
        `when`("标记为已入库") {
            document.markAsIndexed()
            
            then("文档状态应该变为已入库") {
                document.status shouldBe DocumentStatus.INDEXED
            }
            
            then("应该产生已入库事件") {
                val events = document.getDomainEvents()
                events.size shouldBe 1
                events[0].shouldBeInstanceOf<DocumentIndexedEvent>()
            }
        }
    }
})