package com.github.phanerozoicc.base.eventsource

class EventStoreException(
    message: String,
    cause: Throwable? = null
): RuntimeException(message, cause)
