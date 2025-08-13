package com.lifee.user.domain.services

import com.lifee.user.domain.*

/**
 * 用户工厂领域服务
 * 负责封装用户创建逻辑，包括ID生成
 */
interface UserFactory {
    
    /**
     * 创建新用户
     * 自动生成用户ID，无需外部传入
     * 
     * @param email 用户邮箱
     * @param password 用户密码
     * @param firstName 名字
     * @param lastName 姓氏
     * @return 创建的用户实体
     */
    fun createUser(
        email: Email,
        password: Password,
        firstName: String,
        lastName: String
    ): User
    
    /**
     * 创建用户（指定ID）
     * 用于特殊场景，如数据迁移等
     * 
     * @param id 指定的用户ID
     * @param email 用户邮箱
     * @param password 用户密码
     * @param firstName 名字
     * @param lastName 姓氏
     * @return 创建的用户实体
     */
    fun createUserWithId(
        id: UserId,
        email: Email,
        password: Password,
        firstName: String,
        lastName: String
    ): User
}