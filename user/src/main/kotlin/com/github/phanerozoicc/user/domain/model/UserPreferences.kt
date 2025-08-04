package com.github.phanerozoicc.user.domain.model

import java.time.ZoneId
import java.util.*

/**
 * 用户偏好设置值对象
 * 封装用户的个性化配置
 */
data class UserPreferences(
    private val language: Locale = Locale.SIMPLIFIED_CHINESE,
    private val timezone: ZoneId = ZoneId.of("Asia/Shanghai"),
    private val theme: Theme = Theme.LIGHT,
    private val dateFormat: DateFormat = DateFormat.YYYY_MM_DD,
    private val notificationSettings: NotificationSettings = NotificationSettings.default()
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
            dateFormat: DateFormat = DateFormat.YYYY_MM_DD,
            notificationSettings: NotificationSettings = NotificationSettings.default()
        ): UserPreferences {
            return UserPreferences(language, timezone, theme, dateFormat, notificationSettings)
        }

        /**
         * 检测偏好设置变更
         */
        fun detectPreferencesChanges(
            oldPreferences: UserPreferences,
            newPreferences: UserPreferences
        ): Set<String> {
            val changes = mutableSetOf<String>()

            if (oldPreferences.getLanguage() != newPreferences.getLanguage()) changes.add("language")
            if (oldPreferences.getTimezone() != newPreferences.getTimezone()) changes.add("timezone")
            if (oldPreferences.getTheme() != newPreferences.getTheme()) changes.add("theme")
            if (oldPreferences.getDateFormat() != newPreferences.getDateFormat()) changes.add("dateFormat")
            if (oldPreferences.getNotificationSettings() != newPreferences.getNotificationSettings()) {
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
     * 更新日期格式设置
     */
    fun withDateFormat(dateFormat: DateFormat): UserPreferences {
        return copy(dateFormat = dateFormat)
    }
    
    /**
     * 更新通知设置
     */
    fun withNotificationSettings(notificationSettings: NotificationSettings): UserPreferences {
        return copy(notificationSettings = notificationSettings)
    }
    
    /**
     * 获取语言设置
     */
    fun getLanguage(): Locale = language
    
    /**
     * 获取时区设置
     */
    fun getTimezone(): ZoneId = timezone
    
    /**
     * 获取主题设置
     */
    fun getTheme(): Theme = theme
    
    /**
     * 获取日期格式设置
     */
    fun getDateFormat(): DateFormat = dateFormat
    
    /**
     * 获取通知设置
     */
    fun getNotificationSettings(): NotificationSettings = notificationSettings
    
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
 * 日期格式枚举
 */
enum class DateFormat(val displayName: String, val pattern: String) {
    YYYY_MM_DD("年-月-日", "yyyy-MM-dd"),
    MM_DD_YYYY("月/日/年", "MM/dd/yyyy"),
    DD_MM_YYYY("日/月/年", "dd/MM/yyyy"),
    YYYY_MM_DD_HH_MM("年-月-日 时:分", "yyyy-MM-dd HH:mm"),
    MM_DD_YYYY_HH_MM("月/日/年 时:分", "MM/dd/yyyy HH:mm")
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