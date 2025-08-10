package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import com.lifee.user.domain.UserProfile
import java.time.Instant
import java.util.*

/**
 * 用户档案更新事件
 */
data class UserProfileUpdatedEvent(
    val userId: UserId,
    val oldProfile: UserProfile,
    val newProfile: UserProfile
) : DomainEvent(userId.value)