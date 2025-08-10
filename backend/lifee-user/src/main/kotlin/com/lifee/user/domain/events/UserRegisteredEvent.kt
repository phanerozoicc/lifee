package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.Email
import com.lifee.user.domain.UserId
import java.time.Instant
import java.util.*

/**
 * 用户注册事件
 */
data class UserRegisteredEvent(
    val userId: UserId,
    val email: Email,
    val firstName: String,
    val lastName: String,
    val registeredAt: Instant,
    val activationToken: String? = null
) : DomainEvent(userId.value)