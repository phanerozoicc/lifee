package com.lifee.user.domain.services

import com.lifee.user.domain.UserId

/**
 * 用户ID生成器接口
 * 负责生成全局唯一的用户ID
 */
interface UserIdGenerator {
    
    /**
     * 生成下一个用户ID
     * @return 新的用户ID
     */
    fun generateNext(): UserId
    
    /**
     * 获取当前序列号
     * @return 当前序列号
     */
    fun getCurrentSequence(): Long
}