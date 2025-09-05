package com.github.phanerozoicc.user.application.event

import com.github.phanerozoicc.base.event.DomainEvent
import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.model.UserId
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

/**
 * 用户删除事件
 * 当用户账户被删除时发布
 */
class UserDeletedEvent(
    val userId: UserId,
    val email: Email,
    val deletionTime: LocalDateTime = LocalDateTime.now(),
    val reason: String? = null,
    val deletedBy: UserId? = null, // 操作者ID
    val isHardDelete: Boolean = false, // 是否为硬删除
    version: Long = 0,
    eventId: String = UUID.randomUUID().toString(),
    occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserDeleted"
) : DomainEvent(userId.value, version, eventId, occurredOn) {

    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: String
    ): DomainEvent {
        return UserDeletedEvent(
            userId = UserId(aggregateId),
            email = this.email,
            deletionTime = this.deletionTime,
            reason = this.reason,
            deletedBy = this.deletedBy,
            isHardDelete = this.isHardDelete,
            version = version,
            eventId = eventId,
            occurredOn = occurredOn
        )
    }
}


//@Component
class UserDeletedEventHandler(

) {

    fun onEvent(event: UserDeletedEvent) {
        // 处理用户删除事件的逻辑
        // 例如，记录日志、发送通知等
        // 这里暂时什么都不做
    }
}