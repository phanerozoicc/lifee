package com.github.phanerozoicc.user.domain.model

import java.time.LocalDateTime

/**
 * 用户状态值对象
 * 封装用户状态及其变更逻辑
 */
data class UserStatus(
    val status: StatusEnum,
    val reason: String? = null,
    val changedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        /**
         * 创建活跃状态
         */
        fun active(): UserStatus {
            return UserStatus(StatusEnum.ACTIVE, "用户激活")
        }
        
        /**
         * 创建待激活状态
         */
        fun pending(reason: String = "等待邮箱验证"): UserStatus {
            return UserStatus(StatusEnum.PENDING, reason)
        }
        
        /**
         * 创建停用状态
         */
        fun inactive(reason: String): UserStatus {
            require(reason.isNotBlank()) { "停用原因不能为空" }
            return UserStatus(StatusEnum.INACTIVE, reason)
        }
        
        /**
         * 创建锁定状态
         */
        fun locked(reason: String): UserStatus {
            require(reason.isNotBlank()) { "锁定原因不能为空" }
            return UserStatus(StatusEnum.LOCKED, reason)
        }
        
        /**
         * 创建删除状态
         */
        fun deleted(reason: String = "用户主动删除"): UserStatus {
            return UserStatus(StatusEnum.DELETED, reason)
        }

        fun valueOf(status: String): UserStatus? {
            return StatusEnum.of(status)?.let { UserStatus(it) }
        }
    }
    
    /**
     * 检查是否为活跃状态
     */
    fun isActive(): Boolean = status == StatusEnum.ACTIVE
    
    /**
     * 检查是否为待激活状态
     */
    fun isPending(): Boolean = status == StatusEnum.PENDING
    
    /**
     * 检查是否为停用状态
     */
    fun isInactive(): Boolean = status == StatusEnum.INACTIVE
    
    /**
     * 检查是否为锁定状态
     */
    fun isLocked(): Boolean = status == StatusEnum.LOCKED
    
    /**
     * 检查是否为删除状态
     */
    fun isDeleted(): Boolean = status == StatusEnum.DELETED
    
    /**
     * 检查用户是否可以登录
     */
    fun canLogin(): Boolean {
        return status == StatusEnum.ACTIVE
    }
    
    /**
     * 检查用户是否可以执行操作
     */
    fun canPerformActions(): Boolean {
        return status == StatusEnum.ACTIVE
    }
    
    /**
     * 激活用户
     */
    fun activate(): UserStatus {
        return when (status) {
            StatusEnum.PENDING -> UserStatus(StatusEnum.ACTIVE, "用户激活")
            StatusEnum.INACTIVE -> UserStatus(StatusEnum.ACTIVE, "用户重新激活")
            StatusEnum.ACTIVE -> this // 已经是活跃状态
            StatusEnum.LOCKED -> throw IllegalStateException("锁定状态的用户需要先解锁")
            StatusEnum.DELETED -> throw IllegalStateException("已删除的用户无法激活")
        }
    }
    
    /**
     * 停用用户
     */
    fun deactivate(reason: String): UserStatus {
        require(reason.isNotBlank()) { "停用原因不能为空" }
        return when (status) {
            StatusEnum.ACTIVE -> UserStatus(StatusEnum.INACTIVE, reason)
            StatusEnum.PENDING -> UserStatus(StatusEnum.INACTIVE, reason)
            StatusEnum.INACTIVE -> this // 已经是停用状态
            StatusEnum.LOCKED -> throw IllegalStateException("锁定状态的用户需要先解锁")
            StatusEnum.DELETED -> throw IllegalStateException("已删除的用户无法停用")
        }
    }
    
    /**
     * 锁定用户
     */
    fun lock(reason: String): UserStatus {
        require(reason.isNotBlank()) { "锁定原因不能为空" }
        return when (status) {
            StatusEnum.ACTIVE, StatusEnum.PENDING, StatusEnum.INACTIVE -> 
                UserStatus(StatusEnum.LOCKED, reason)
            StatusEnum.LOCKED -> this // 已经是锁定状态
            StatusEnum.DELETED -> throw IllegalStateException("已删除的用户无法锁定")
        }
    }
    
    /**
     * 解锁用户
     */
    fun unlock(): UserStatus {
        return when (status) {
            StatusEnum.LOCKED -> UserStatus(StatusEnum.ACTIVE, "用户解锁")
            else -> throw IllegalStateException("只有锁定状态的用户才能解锁")
        }
    }
    
    /**
     * 删除用户
     */
    fun delete(reason: String = "用户主动删除"): UserStatus {
        return when (status) {
            StatusEnum.DELETED -> this // 已经是删除状态
            else -> UserStatus(StatusEnum.DELETED, reason)
        }
    }
    

    /**
     * 获取状态显示名称
     */
    fun getDisplayName(): String = status.displayName
    
    /**
     * 转换为字符串
     */
    override fun toString(): String {
        return "UserStatus(status=${status.displayName}, reason=$reason, changedAt=$changedAt)"
    }
}

/**
 * 用户状态枚举
 */
enum class StatusEnum(val displayName: String, val description: String) {
    ACTIVE("活跃", "用户账户正常，可以正常使用所有功能"),
    PENDING("待激活", "用户已注册但尚未激活，需要验证邮箱或手机号"),
    INACTIVE("停用", "用户账户被停用，无法登录和使用功能"),
    LOCKED("锁定", "用户账户被锁定，通常由于安全原因或违规行为"),
    DELETED("已删除", "用户账户已被删除，数据可能被软删除保留");

    companion object {
        fun of(status: String): StatusEnum? = entries.find { it.displayName == status }
    }
}