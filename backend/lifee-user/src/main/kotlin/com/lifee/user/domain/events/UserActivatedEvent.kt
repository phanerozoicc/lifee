package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import java.time.Instant
import java.util.*

/**
 * 用户激活事件
 */
data class UserActivatedEvent(
    val userId: UserId
) : DomainEvent(userId.value)