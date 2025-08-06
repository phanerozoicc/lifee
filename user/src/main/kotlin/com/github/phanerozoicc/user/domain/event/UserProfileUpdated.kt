package com.github.phanerozoicc.user.domain.event

import com.github.phanerozoicc.base.domain.DomainEvent
import com.github.phanerozoicc.user.domain.model.UserId
import com.github.phanerozoicc.user.domain.model.UserProfile
import java.time.Instant
import java.util.*

/**
 * 用户资料更新事件
 * 当用户资料信息发生变更时发布
 */
data class UserProfileUpdated(
    val userId: UserId,
    val oldProfile: UserProfile,
    val newProfile: UserProfile,
    val changedFields: Set<String>,
    val updatedBy: UserId? = null, // 如果是管理员更新，记录操作者
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserProfileUpdated"
) : DomainEvent {

    /**
     * 检查特定字段是否发生变更
     */
    fun isFieldChanged(fieldName: String): Boolean {
        return changedFields.contains(fieldName)
    }
}
