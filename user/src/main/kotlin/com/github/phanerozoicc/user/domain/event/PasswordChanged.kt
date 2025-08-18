package com.github.phanerozoicc.user.domain.event

import com.github.phanerozoicc.base.event.Event
import com.github.phanerozoicc.user.domain.model.UserId
import java.time.Instant
import java.time.LocalDateTime
import java.util.*


/**
 * 密码变更事件
 * 当用户密码发生变更时发布
 */
data class PasswordChanged(
    private val userId: UserId,
    private val changeTime: LocalDateTime = LocalDateTime.now(),
    private val ipAddress: String? = null,
    private val isAdminReset: Boolean = false,
    private val resetBy: UserId? = null, // 如果是管理员重置，记录操作者
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "PasswordChanged"
) : Event

