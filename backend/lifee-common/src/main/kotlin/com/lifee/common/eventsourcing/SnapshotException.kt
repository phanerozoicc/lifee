package com.lifee.common.eventsourcing

/**
 * 快照相关异常
 */
class SnapshotException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)