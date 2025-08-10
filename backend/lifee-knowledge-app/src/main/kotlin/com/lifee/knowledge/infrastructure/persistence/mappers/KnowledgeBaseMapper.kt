package com.lifee.knowledge.infrastructure.persistence.mappers

import com.lifee.knowledge.domain.aggregates.KnowledgeBase
import com.lifee.knowledge.domain.entities.Document
import com.lifee.knowledge.domain.valueobjects.*
import com.lifee.knowledge.infrastructure.persistence.entities.DocumentEntity
import com.lifee.knowledge.infrastructure.persistence.entities.DocumentTypeEnum
import com.lifee.knowledge.infrastructure.persistence.entities.KnowledgeBaseEntity
import org.springframework.stereotype.Component

/**
 * 知识库领域模型与JPA实体映射器
 */
@Component
class KnowledgeBaseMapper {
    
    /**
     * 将领域模型转换为JPA实体
     */
    fun toEntity(knowledgeBase: KnowledgeBase): KnowledgeBaseEntity {
        val entity = KnowledgeBaseEntity().apply {
            id = knowledgeBase.getId().value
            name = knowledgeBase.getName().value
            description = knowledgeBase.getDescription().value
            ownerId = knowledgeBase.getOwnerId().value
            createdAt = knowledgeBase.getCreatedAt()
            updatedAt = knowledgeBase.getUpdatedAt()
        }
        
        // 转换文档
        entity.documents = knowledgeBase.getDocuments().map { document ->
            toDocumentEntity(document, entity)
        }.toMutableSet()
        
        return entity
    }
    
    /**
     * 将JPA实体转换为领域模型
     */
    fun toDomain(entity: KnowledgeBaseEntity): KnowledgeBase {
        val knowledgeBase = KnowledgeBase(
            id = KnowledgeBaseId(entity.id),
            name = KnowledgeBaseName(entity.name),
            description = KnowledgeBaseDescription(entity.description),
            ownerId = UserId(entity.ownerId),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
        
        // 转换文档
        entity.documents.forEach { documentEntity ->
            val document = toDocumentDomain(documentEntity)
            knowledgeBase.addDocumentInternal(document)
        }
        
        return knowledgeBase
    }
    
    /**
     * 将文档领域模型转换为JPA实体
     */
    private fun toDocumentEntity(document: Document, knowledgeBaseEntity: KnowledgeBaseEntity): DocumentEntity {
        return DocumentEntity().apply {
            id = document.getId().value
            title = document.getTitle().value
            content = document.getContent().value
            type = toDocumentTypeEnum(document.getType())
            createdAt = document.getCreatedAt()
            updatedAt = document.getUpdatedAt()
            knowledgeBase = knowledgeBaseEntity
        }
    }
    
    /**
     * 将文档JPA实体转换为领域模型
     */
    private fun toDocumentDomain(entity: DocumentEntity): Document {
        return Document(
            id = DocumentId(entity.id),
            title = DocumentTitle(entity.title),
            content = DocumentContent(entity.content),
            type = toDocumentType(entity.type),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
    
    /**
     * 将领域文档类型转换为JPA枚举
     */
    private fun toDocumentTypeEnum(documentType: DocumentType): DocumentTypeEnum {
        return when (documentType.value) {
            "MARKDOWN" -> DocumentTypeEnum.MARKDOWN
            "TEXT" -> DocumentTypeEnum.TEXT
            "PDF" -> DocumentTypeEnum.PDF
            "WORD" -> DocumentTypeEnum.WORD
            "HTML" -> DocumentTypeEnum.HTML
            "JSON" -> DocumentTypeEnum.JSON
            "XML" -> DocumentTypeEnum.XML
            else -> DocumentTypeEnum.TEXT
        }
    }
    
    /**
     * 将JPA枚举转换为领域文档类型
     */
    private fun toDocumentType(documentTypeEnum: DocumentTypeEnum): DocumentType {
        return when (documentTypeEnum) {
            DocumentTypeEnum.MARKDOWN -> DocumentType.MARKDOWN
            DocumentTypeEnum.TEXT -> DocumentType.TEXT
            DocumentTypeEnum.PDF -> DocumentType.PDF
            DocumentTypeEnum.WORD -> DocumentType.WORD
            DocumentTypeEnum.HTML -> DocumentType.HTML
            DocumentTypeEnum.JSON -> DocumentType.JSON
            DocumentTypeEnum.XML -> DocumentType.XML
        }
    }
}