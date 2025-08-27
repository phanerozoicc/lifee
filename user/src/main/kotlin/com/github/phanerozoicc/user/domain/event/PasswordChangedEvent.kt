package com.github.phanerozoicc.user.domain.event

import com.github.phanerozoicc.base.event.DomainEvent
import com.github.phanerozoicc.user.domain.model.UserId
import java.time.LocalDateTime


/**
 * 密码变更事件
 * 当用户密码发生变更时发布
 */
class PasswordChangedEvent(
    private val userId: UserId,
    private val changeTime: LocalDateTime = LocalDateTime.now(),
    private val ipAddress: String? = null,
    private val isAdminReset: Boolean = false,
    private val resetBy: UserId? = null, // 如果是管理员重置，记录操作者
) : DomainEvent()

