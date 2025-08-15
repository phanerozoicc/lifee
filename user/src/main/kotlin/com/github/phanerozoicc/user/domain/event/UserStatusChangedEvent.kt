package com.github.phanerozoicc.user.domain.event

import com.github.phanerozoicc.base.event.DomainEvent
import com.github.phanerozoicc.user.domain.model.UserId
import com.github.phanerozoicc.user.domain.model.UserStatus


/**
 * 用户状态变更事件
 * 当用户状态发生变更时发布
 */
data class UserStatusChangedEvent(
    val userId: UserId,
    val oldStatus: UserStatus,
    val newStatus: UserStatus,
    val reason: String? = null,
    val changedBy: UserId? = null, // 操作者ID
    val ipAddress: String? = null,
) : DomainEvent(userId.value, "UserStatusChanged") {

    /**
     * 检查是否为激活操作
     */
    fun isActivation(): Boolean {
        return newStatus.isActive() && !oldStatus.isActive()
    }

    /**
     * 检查是否为停用操作
     */
    fun isDeactivation(): Boolean {
        return !newStatus.isActive() && oldStatus.isActive()
    }

    /**
     * 检查是否为锁定操作
     */
    fun isLocking(): Boolean {
        return newStatus.isLocked() && !oldStatus.isLocked()
    }

    /**
     * 检查是否为解锁操作
     */
    fun isUnlocking(): Boolean {
        return !newStatus.isLocked() && oldStatus.isLocked()
    }
}
