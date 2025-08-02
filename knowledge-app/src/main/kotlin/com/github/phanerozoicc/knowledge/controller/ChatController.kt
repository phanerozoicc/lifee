package com.github.phanerozoicc.knowledge.controller

import com.github.phanerozoicc.response.Response
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chat")
class ChatController(
    createChatCmdHandler: createChatCmdHandler
) {

    @RequestMapping("/create")
    fun create(): Response<String> {

    }
}
