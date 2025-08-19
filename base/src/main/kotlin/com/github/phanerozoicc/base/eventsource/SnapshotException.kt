package com.github.phanerozoicc.base.eventsource

class SnapshotException(
    message: String,
    cause: Throwable? = null
): RuntimeException(message, cause)