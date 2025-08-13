package com.github.phanerozoicc.user.domain.model

import java.time.ZoneId
import java.util.*

/**
 * 用户偏好设置值对象
 * 封装用户的个性化配置
 */
data class UserPreferences(
    val language: Locale = Locale.SIMPLIFIED_CHINESE,
    val timezone: ZoneId = ZoneId.of("Asia/Shanghai"),
    val theme: Theme = Theme.AUTO,
    val notificationSettings: NotificationSettings = NotificationSettings.default()
) {
    companion object {
        /**
         * 创建默认偏好设置
         */
        fun default(): UserPreferences {
            return UserPreferences()
        }
        
        /**
         * 创建自定义偏好设置
         */
        fun of(
            language: Locale = Locale.SIMPLIFIED_CHINESE,
            timezone: ZoneId = ZoneId.of("Asia/Shanghai"),
            theme: Theme = Theme.LIGHT,
            notificationSettings: NotificationSettings = NotificationSettings.default()
        ): UserPreferences {
            return UserPreferences(language, timezone, theme, notificationSettings)
        }

        /**
         * 检测偏好设置变更
         */
        fun detectPreferencesChanges(
            oldPreferences: UserPreferences,
            newPreferences: UserPreferences
        ): Set<String> {
            val changes = mutableSetOf<String>()

            if (oldPreferences.language != newPreferences.language) changes.add("language")
            if (oldPreferences.timezone.id != newPreferences.timezone.id) changes.add("timezone")
            if (oldPreferences.theme != newPreferences.theme) changes.add("theme")
            if (oldPreferences.notificationSettings != newPreferences.notificationSettings) {
                changes.add("notificationSettings")
            }
            return changes
        }
    }
    
    /**
     * 更新语言设置
     */
    fun withLanguage(language: Locale): UserPreferences {
        return copy(language = language)
    }
    
    /**
     * 更新时区设置
     */
    fun withTimezone(timezone: ZoneId): UserPreferences {
        return copy(timezone = timezone)
    }
    
    /**
     * 更新主题设置
     */
    fun withTheme(theme: Theme): UserPreferences {
        return copy(theme = theme)
    }
    
    /**
     * 更新通知设置
     */
    fun withNotificationSettings(notificationSettings: NotificationSettings): UserPreferences {
        return copy(notificationSettings = notificationSettings)
    }
    
    /**
     * 获取语言代码
     */
    fun getLanguageCode(): String = language.language
    
    /**
     * 获取国家代码
     */
    fun getCountryCode(): String = language.country
    
    /**
     * 获取时区ID
     */
    fun getTimezoneId(): String = timezone.id
    
    /**
     * 检查是否为中文环境
     */
    fun isChinese(): Boolean {
        return language.language == "zh"
    }
    
    /**
     * 检查是否为英文环境
     */
    fun isEnglish(): Boolean {
        return language.language == "en"
    }
    
    /**
     * 检查是否为深色主题
     */
    fun isDarkTheme(): Boolean {
        return theme == Theme.DARK
    }
}

/**
 * 主题枚举
 */
enum class Theme(val displayName: String, val cssClass: String) {
    LIGHT("浅色主题", "theme-light"),
    DARK("深色主题", "theme-dark"),
    AUTO("自动主题", "theme-auto")
}


/**
 * 通知设置值对象
 */
data class NotificationSettings(
    private val emailNotification: Boolean = true,
    private val pushNotification: Boolean = true,
    private val smsNotification: Boolean = false,
    private val marketingEmail: Boolean = false
) {
    companion object {
        /**
         * 创建默认通知设置
         */
        fun default(): NotificationSettings {
            return NotificationSettings()
        }
        
        /**
         * 创建全部启用的通知设置
         */
        fun allEnabled(): NotificationSettings {
            return NotificationSettings(
                emailNotification = true,
                pushNotification = true,
                smsNotification = true,
                marketingEmail = true
            )
        }
        
        /**
         * 创建全部禁用的通知设置
         */
        fun allDisabled(): NotificationSettings {
            return NotificationSettings(
                emailNotification = false,
                pushNotification = false,
                smsNotification = false,
                marketingEmail = false
            )
        }
    }
    
    /**
     * 启用邮件通知
     */
    fun enableEmailNotification(): NotificationSettings {
        return copy(emailNotification = true)
    }
    
    /**
     * 禁用邮件通知
     */
    fun disableEmailNotification(): NotificationSettings {
        return copy(emailNotification = false)
    }
    
    /**
     * 启用推送通知
     */
    fun enablePushNotification(): NotificationSettings {
        return copy(pushNotification = true)
    }
    
    /**
     * 禁用推送通知
     */
    fun disablePushNotification(): NotificationSettings {
        return copy(pushNotification = false)
    }
    
    /**
     * 启用短信通知
     */
    fun enableSmsNotification(): NotificationSettings {
        return copy(smsNotification = true)
    }
    
    /**
     * 禁用短信通知
     */
    fun disableSmsNotification(): NotificationSettings {
        return copy(smsNotification = false)
    }
    
    /**
     * 启用营销邮件
     */
    fun enableMarketingEmail(): NotificationSettings {
        return copy(marketingEmail = true)
    }
    
    /**
     * 禁用营销邮件
     */
    fun disableMarketingEmail(): NotificationSettings {
        return copy(marketingEmail = false)
    }
    
    /**
     * 检查是否启用邮件通知
     */
    fun isEmailNotificationEnabled(): Boolean = emailNotification
    
    /**
     * 检查是否启用推送通知
     */
    fun isPushNotificationEnabled(): Boolean = pushNotification
    
    /**
     * 检查是否启用短信通知
     */
    fun isSmsNotificationEnabled(): Boolean = smsNotification
    
    /**
     * 检查是否启用营销邮件
     */
    fun isMarketingEmailEnabled(): Boolean = marketingEmail
    
    /**
     * 检查是否有任何通知启用
     */
    fun hasAnyNotificationEnabled(): Boolean {
        return emailNotification || pushNotification || smsNotification
    }
}