package com.github.phanerozoicc.user.application.event

import com.github.phanerozoicc.base.event.DomainEvent
import com.github.phanerozoicc.user.domain.model.UserId
import com.github.phanerozoicc.user.domain.model.UserPreferences
import java.time.Instant
import java.util.*


/**
 * 用户偏好设置更新事件
 * 当用户偏好设置发生变更时发布
 */
class UserPreferencesUpdatedEvent(
    val userId: UserId,
    val oldPreferences: UserPreferences,
    val newPreferences: UserPreferences,
    val changedSettings: Set<String>,
    version: Long = 0,
    eventId: String = UUID.randomUUID().toString(),
    occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserPreferencesUpdated"
) : DomainEvent(userId.value, version, eventId, occurredOn) {

    /**
     * 检查特定设置是否发生变更
     */
    fun isSettingChanged(settingName: String): Boolean {
        return changedSettings.contains(settingName)
    }

    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: String
    ): DomainEvent {
        return UserPreferencesUpdatedEvent(
            userId,
            oldPreferences,
            newPreferences,
            changedSettings,
            version,
            eventId,
            occurredOn
        )
    }
}
