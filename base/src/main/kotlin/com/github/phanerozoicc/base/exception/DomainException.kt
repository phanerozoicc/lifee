package com.github.phanerozoicc.base.exception

import com.github.phanerozoicc.base.domain.AggregateRoot

open class DomainException(message: String, cause: Throwable?): Exception(message,  cause) {
}

class ConcurrencyDomainException(
    val aggregateId: String,
    val expectedVersion: Long,
    val actualVersion: Long,
    message: String = "concurrency confilict for aggregete: $aggregateId. expected version: $expectedVersion, actual version: $actualVersion"
): DomainException(message, null)