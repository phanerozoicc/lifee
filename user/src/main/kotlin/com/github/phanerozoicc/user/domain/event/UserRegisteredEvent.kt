package com.github.phanerozoicc.user.domain.event

import com.github.phanerozoicc.base.event.Event
import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.model.UserId
import java.time.LocalDateTime

/**
 * 用户注册事件
 * 当新用户成功注册时发布
 */
data class UserRegisteredEvent(
    val userId: UserId,
    val email: Email,
    val nickname: String,
    val registrationTime: LocalDateTime = LocalDateTime.now(),
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val activationToken: String? = null,
) : Event(userId.value, "UserRegistered")

