package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import java.time.Instant
import java.util.*

/**
 * 用户密码更改事件
 */
data class UserPasswordChangedEvent(
    val userId: UserId
) : DomainEvent(userId.value)