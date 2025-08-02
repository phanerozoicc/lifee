package com.github.phanerozoicc.knowledge.controller

import com.github.phanerozoicc.knowledge.domain.document2.DocumentEmbeddingCmd
import com.github.phanerozoicc.knowledge.domain.document2.DocumentEmbeddingCommandHandler
import com.github.phanerozoicc.response.Response
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/document")
class DocumentController(
    val documentEmbeddingCommandHandler: DocumentEmbeddingCommandHandler
) {

    @RequestMapping("/embedding")
    fun embedding(@RequestParam("file") file: MultipartFile,
                  @RequestParam("ragTag") ragTag: String): Response<Nothing> {

        documentEmbeddingCommandHandler.handle(DocumentEmbeddingCmd(file.resource, ragTag))
        return Response()
    }

}
