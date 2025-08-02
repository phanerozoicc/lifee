package com.github.phanerozoicc.knowledge.service

import com.github.phanerozoicc.knowledge.consts.RedisKey
import com.github.phanerozoicc.knowledge.domain.document2.VectorService
import mu.KLogging
import org.redisson.api.RedissonClient
import org.springframework.ai.reader.tika.TikaDocumentReader
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.pgvector.PgVectorStore
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

@Service
class VectorServiceImpl(
    val tokenTextSplitter: TokenTextSplitter,
    val pgVectorStore: PgVectorStore,
    val redissonClient: RedissonClient
) : VectorService {

    companion object: KLogging()

    override fun parse(resource: Resource, ragTag: String) {
        logger.debug("开始解析文件到向量库, file: ${resource.filename}")
        val documentReader = TikaDocumentReader(resource)
        val splitDoc = tokenTextSplitter.apply(documentReader.get())
        splitDoc.forEach {
            it.metadata.put("knowledge", ragTag)
        }
        pgVectorStore.add(splitDoc)
        logger.debug("上传到向量库完成, file:${resource.filename}")
        val tagList = redissonClient.getList<String>(RedisKey.TAG_LIST)
        if (!tagList.contains(ragTag)) {
            tagList.add(ragTag)
        }
    }

}
