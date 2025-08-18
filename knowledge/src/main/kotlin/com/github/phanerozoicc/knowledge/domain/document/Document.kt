package com.github.phanerozoicc.knowledge.domain.document

import com.github.phanerozoicc.base.domain.AggregateRoot
import com.github.phanerozoicc.base.event.Event
import com.github.phanerozoicc.knowledge.domain.knowledgebase.KnowledgeBaseId
import java.math.BigInteger
import java.security.MessageDigest
import java.time.Instant
import java.util.*

@JvmInline
value class DocumentId(val value: String) {
    companion object {
        fun generate(): DocumentId = DocumentId(UUID.randomUUID().toString())
        
        fun fromPathName(pathName: String): DocumentId {
            // 将路径名md5 hash为id
            return DocumentId(pathName.md5())
        }

        private fun String.md5(): String {
            MessageDigest.getInstance("MD5").let { md5 ->
                return BigInteger(1, md5.digest(toByteArray())).toString(16)
                    .padStart(32, '0')
            }
        }
    }
}

@JvmInline
value class ChunkId(val value: String) {
    companion object {
        fun generate(): ChunkId = ChunkId(UUID.randomUUID().toString())
    }
}

enum class ProcessingStatus {
    PENDING, PROCESSING, COMPLETED, FAILED
}

data class DocumentMetadata(
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val mimeType: String,
    val contentHash: String,
    val uploadedAt: Instant,
    val lastModified: Instant,
    val chunkSettings: ChunkSettings = ChunkSettings()
)

data class ChunkSettings(
    val chunkSize: Int = 1000,
    val chunkOverlap: Int = 200
)

data class DocumentChunk(
    val id: ChunkId,
    val documentId: DocumentId,
    val content: String,
    val position: Int,
    val startOffset: Int,
    val endOffset: Int,
    val embedding: EmbeddingVector? = null
)

data class EmbeddingVector(
    val model: String,
    val dimensions: Int,
    val values: FloatArray
) {
    fun cosineSimilarity(other: EmbeddingVector): Double {
        require(this.dimensions == other.dimensions) { "Vector dimensions must match" }
        return cosineDistance(this.values, other.values)
    }
    
    private fun cosineDistance(a: FloatArray, b: FloatArray): Double {
        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0
        
        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        
        return if (normA == 0.0 || normB == 0.0) 0.0 else dotProduct / (Math.sqrt(normA) * Math.sqrt(normB))
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EmbeddingVector
        return model == other.model && 
               dimensions == other.dimensions && 
               values.contentEquals(other.values)
    }
    
    override fun hashCode(): Int {
        var result = model.hashCode()
        result = 31 * result + dimensions
        result = 31 * result + values.contentHashCode()
        return result
    }
}

class Document(
    private val id: DocumentId,
    val knowledgeBaseId: KnowledgeBaseId,
    var metadata: DocumentMetadata,
    var processingStatus: ProcessingStatus = ProcessingStatus.PENDING,
    private val chunks: MutableList<DocumentChunk> = mutableListOf(),
    private val embeddings: MutableList<EmbeddingVector> = mutableListOf(),
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
) : AggregateRoot<DocumentId>() {
    
    override fun getId(): DocumentId = id
    
    fun processContent(extractedContent: ExtractedContent, chunkSettings: ChunkSettings): List<DocumentChunk> {
        updateStatus(ProcessingStatus.PROCESSING)
        
        try {
            val newChunks = chunkContent(extractedContent.text, chunkSettings)
            
            chunks.clear()
            chunks.addAll(newChunks)
            
            updateStatus(ProcessingStatus.COMPLETED)
            publish(DocumentProcessedEvent(id, chunks.size))
            
            return chunks.toList()
        } catch (e: Exception) {
            updateStatus(ProcessingStatus.FAILED)
            publish(DocumentProcessingFailedEvent(id, e.message ?: "Unknown error"))
            throw e
        }
    }
    
    private fun chunkContent(text: String, settings: ChunkSettings): List<DocumentChunk> {
        val chunkSize = settings.chunkSize
        val overlap = settings.chunkOverlap
        val chunks = mutableListOf<DocumentChunk>()
        var position = 0
        var startOffset = 0
        
        while (startOffset < text.length) {
            val endOffset = minOf(startOffset + chunkSize, text.length)
            val chunkText = text.substring(startOffset, endOffset)
            
            val chunk = DocumentChunk(
                id = ChunkId.generate(),
                documentId = id,
                content = chunkText,
                position = position++,
                startOffset = startOffset,
                endOffset = endOffset
            )
            
            chunks.add(chunk)
            
            startOffset = endOffset - overlap
            if (startOffset >= text.length - overlap) break
        }
        
        return chunks
    }
    
    fun updateContent(newContentHash: String): Boolean {
        if (metadata.contentHash != newContentHash) {
            metadata = metadata.copy(
                contentHash = newContentHash, 
                lastModified = Instant.now()
            )
            updatedAt = Instant.now()
            updateStatus(ProcessingStatus.PENDING)
            publish(DocumentContentChangedEvent(id, metadata.contentHash, newContentHash))
            return true
        }
        return false
    }
    
    private fun updateStatus(status: ProcessingStatus) {
        this.processingStatus = status
        this.updatedAt = Instant.now()
        publish(DocumentStatusChangedEvent(id, status))
    }
    
    fun getChunks(): List<DocumentChunk> = chunks.toList()
    fun getChunkCount(): Int = chunks.size
    
    fun isProcessingComplete(): Boolean = processingStatus == ProcessingStatus.COMPLETED
    fun isProcessingFailed(): Boolean = processingStatus == ProcessingStatus.FAILED
}

data class ExtractedContent(
    val text: String,
    val mimeType: String,
    val metadata: Map<String, Any> = emptyMap(),
    val extractedAt: Instant = Instant.now()
)

// Domain Events
data class DocumentProcessedEvent(
    val documentId: DocumentId,
    val chunksCreated: Int,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "DocumentProcessed"
) : Event

data class DocumentProcessingFailedEvent(
    val documentId: DocumentId,
    val errorMessage: String,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "DocumentProcessingFailed"
) : Event

data class DocumentContentChangedEvent(
    val documentId: DocumentId,
    val oldHash: String,
    val newHash: String,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "DocumentContentChanged"
) : Event

data class DocumentStatusChangedEvent(
    val documentId: DocumentId,
    val status: ProcessingStatus,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "DocumentStatusChanged"
) : Event