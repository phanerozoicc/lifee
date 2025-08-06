package com.github.phanerozoicc.user.bak.application.query

import com.github.phanerozoicc.user.domain.cqrs.GetUserProfileQuery
import com.github.phanerozoicc.user.domain.cqrs.QueryHandler
import com.github.phanerozoicc.user.domain.cqrs.QueryResult
import com.github.phanerozoicc.user.domain.query.UserProfileDTO
import com.github.phanerozoicc.user.domain.repository.UserRepository
import org.springframework.stereotype.Service


/**
 * 获取用户资料查询处理器
 */
@Service
class GetUserProfileQueryHandler(
    private val userRepository: UserRepository
) : QueryHandler<GetUserProfileQuery, UserProfileDTO> {

    override suspend fun handle(query: GetUserProfileQuery): QueryResult<UserProfileDTO> {
        try {
            val user = userRepository.findById(query.userId)
                ?: return QueryResult.NotFound("用户不存在")

            val profile = UserProfileDTO.fromUser(user)

            return QueryResult.Success(profile)

        } catch (e: Exception) {
            return QueryResult.Error("获取用户资料失败: ${e.message}")
        }
    }
}
