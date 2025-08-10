package com.lifee.knowledge.domain.exceptions

/**
 * 知识库领域异常基类
 */
abstract class KnowledgeBaseDomainException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 知识库不存在异常
 */
class KnowledgeBaseNotFoundException(
    knowledgeBaseId: String
) : KnowledgeBaseDomainException("Knowledge base not found: $knowledgeBaseId")

/**
 * 知识库名称重复异常
 */
class DuplicateKnowledgeBaseNameException(
    name: String
) : KnowledgeBaseDomainException("Knowledge base with name '$name' already exists")

/**
 * 文档不存在异常
 */
class DocumentNotFoundException(
    documentId: String
) : KnowledgeBaseDomainException("Document not found: $documentId")

/**
 * 文档已存在异常
 */
class DocumentAlreadyExistsException(
    documentId: String
) : KnowledgeBaseDomainException("Document already exists: $documentId")

/**
 * 无权限访问异常
 */
class UnauthorizedAccessException(
    userId: String,
    knowledgeBaseId: String
) : KnowledgeBaseDomainException("User $userId is not authorized to access knowledge base $knowledgeBaseId")

/**
 * 知识库容量超限异常
 */
class KnowledgeBaseCapacityExceededException(
    currentSize: Int,
    maxSize: Int
) : KnowledgeBaseDomainException("Knowledge base capacity exceeded: current=$currentSize, max=$maxSize")

/**
 * 文档类型不支持异常
 */
class UnsupportedDocumentTypeException(
    documentType: String
) : KnowledgeBaseDomainException("Unsupported document type: $documentType")