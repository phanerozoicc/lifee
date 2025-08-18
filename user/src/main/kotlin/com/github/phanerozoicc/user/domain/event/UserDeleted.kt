package com.github.phanerozoicc.user.domain.event

import com.github.phanerozoicc.base.event.Event
import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.model.UserId
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

/**
 * 用户删除事件
 * 当用户账户被删除时发布
 */
data class UserDeleted(
    private val userId: UserId,
    private val email: Email,
    private val deletionTime: LocalDateTime = LocalDateTime.now(),
    private val reason: String? = null,
    private val deletedBy: UserId? = null, // 操作者ID
    private val isHardDelete: Boolean = false, // 是否为硬删除
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserDeleted"
) : Event
