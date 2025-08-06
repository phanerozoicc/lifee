package com.github.phanerozoicc.user.bak.domain.repository

import com.github.phanerozoicc.user.domain.model.UserStatus
import java.time.LocalDateTime


/**
 * 用户搜索条件
 */
data class UserSearchCriteria(
    val keyword: String? = null,
    val status: UserStatus? = null,
    val emailVerified: Boolean? = null,
    val createdAfter: LocalDateTime? = null,
    val createdBefore: LocalDateTime? = null,
    val lastLoginAfter: LocalDateTime? = null,
    val lastLoginBefore: LocalDateTime? = null,
    val limit: Int = 50,
    val offset: Int = 0,
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC"
)