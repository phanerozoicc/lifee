package com.github.phanerozoicc.base.queries

import java.time.LocalDateTime
import java.util.*

abstract class Query(
    val queryType: String,
    val queryId: String = UUID.randomUUID().toString(),
    val timestamp: LocalDateTime = LocalDateTime.now()
)