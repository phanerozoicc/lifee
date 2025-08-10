package com.lifee.user.domain

/**
 * 用户状态枚举
 */
enum class UserStatus {
    /**
     * 待激活 - 用户已注册但尚未激活邮箱
     */
    PENDING_ACTIVATION,
    
    /**
     * 活跃 - 用户已激活且可正常使用
     */
    ACTIVE,
    
    /**
     * 已停用 - 用户账户被管理员停用
     */
    SUSPENDED,
    
    /**
     * 已删除 - 用户账户被软删除
     */
    DELETED;
    
    /**
     * 检查用户是否可以登录
     */
    fun canLogin(): Boolean {
        return this == ACTIVE
    }
    
    /**
     * 检查用户是否需要激活
     */
    fun needsActivation(): Boolean {
        return this == PENDING_ACTIVATION
    }
    
    /**
     * 检查用户是否被停用
     */
    fun isSuspended(): Boolean {
        return this == SUSPENDED
    }
    
    /**
     * 检查用户是否被删除
     */
    fun isDeleted(): Boolean {
        return this == DELETED
    }
    
    /**
     * 检查用户是否处于活跃状态
     */
    fun isActive(): Boolean {
        return this == ACTIVE
    }
}