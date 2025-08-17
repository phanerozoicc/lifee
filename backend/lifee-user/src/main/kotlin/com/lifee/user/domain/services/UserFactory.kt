package com.lifee.user.domain.services

import com.lifee.user.domain.*

/**
 * 用户工厂接口
 * 负责创建用户实例，封装用户创建逻辑
 */
interface UserFactory {
    
    /**
     * 创建新用户
     * 自动生成用户ID
     * 
     * @param email 用户邮箱
     * @param password 用户密码
     * @param firstName 名字
     * @param lastName 姓氏
     * @return 创建的用户实例
     */
    fun createUser(
        email: Email,
        password: Password,
        firstName: String,
        lastName: String
    ): User
    
    /**
     * 使用指定ID创建用户
     * 
     * @param userId 指定的用户ID
     * @param email 用户邮箱
     * @param password 用户密码
     * @param firstName 名字
     * @param lastName 姓氏
     * @return 创建的用户实例
     */
    fun createUserWithId(
        userId: UserId,
        email: Email,
        password: Password,
        firstName: String,
        lastName: String
    ): User
}