package com.github.phanerozoicc.user.domain.event

import com.github.phanerozoicc.base.event.DomainEvent
import com.github.phanerozoicc.base.event.EventHandler
import com.github.phanerozoicc.user.domain.model.UserId
import com.github.phanerozoicc.user.domain.model.UserProfile
import java.time.Instant
import java.util.*

/**
 * 用户资料更新事件
 * 当用户资料信息发生变更时发布
 */
class UserProfileUpdatedEvent(
    val userId: UserId,
    val oldProfile: UserProfile,
    val newProfile: UserProfile,
    val changedFields: Set<String>,
    val updatedBy: UserId? = null, // 如果是管理员更新，记录操作者
    version: Long = 0,
    eventId: String = UUID.randomUUID().toString(),
    occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserProfileUpdated"
) : DomainEvent(userId.value, version, eventId, occurredOn) {

    /**
     * 检查特定字段是否发生变更
     */
    fun isFieldChanged(fieldName: String): Boolean {
        return changedFields.contains(fieldName)
    }

    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: String
    ): DomainEvent {
        return UserProfileUpdatedEvent(
            userId = UserId(aggregateId),
            oldProfile = this.oldProfile,
            newProfile = this.newProfile,
            changedFields = this.changedFields,
            updatedBy = this.updatedBy,
            version = version,
            eventId = eventId,
            occurredOn = occurredOn
        )
    }
}


class UserProfileUpdatedEventHandler(

): EventHandler<UserProfileUpdatedEvent, Unit> {

    override fun onEvent(event: UserProfileUpdatedEvent) {
        // 处理用户资料更新事件的逻辑
        // 例如，记录日志、同步缓存等
        // 这里暂时什么都不做
    }
}
