package com.github.phanerozoicc.user.domain.event

import com.github.phanerozoicc.base.event.DomainEvent
import com.github.phanerozoicc.user.domain.model.UserId
import com.github.phanerozoicc.user.domain.model.UserPreferences
import java.time.Instant
import java.util.*


/**
 * 用户偏好设置更新事件
 * 当用户偏好设置发生变更时发布
 */
data class UserPreferencesUpdated(
    private val userId: UserId,
    private val oldPreferences: UserPreferences,
    private val newPreferences: UserPreferences,
    private val changedSettings: Set<String>,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserPreferencesUpdated"
) : DomainEvent {

    fun getUserId(): UserId = userId
    fun getOldPreferences(): UserPreferences = oldPreferences
    fun getNewPreferences(): UserPreferences = newPreferences
    fun getChangedSettings(): Set<String> = changedSettings

    /**
     * 检查特定设置是否发生变更
     */
    fun isSettingChanged(settingName: String): Boolean {
        return changedSettings.contains(settingName)
    }
}
