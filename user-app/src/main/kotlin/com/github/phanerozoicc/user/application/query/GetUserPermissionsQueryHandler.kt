package com.github.phanerozoicc.user.bak.application.query

import com.github.phanerozoicc.user.domain.cqrs.GetUserPermissionsQuery
import com.github.phanerozoicc.user.domain.cqrs.QueryHandler
import com.github.phanerozoicc.user.domain.cqrs.QueryResult
import com.github.phanerozoicc.user.domain.query.UserPermissionsDTO
import com.github.phanerozoicc.user.domain.repository.UserRepository
import org.springframework.stereotype.Service

/**
 * 获取用户权限查询处理器
 */
@Service
class GetUserPermissionsQueryHandler(
    private val userRepository: UserRepository
) : QueryHandler<GetUserPermissionsQuery, UserPermissionsDTO> {

    override suspend fun handle(query: GetUserPermissionsQuery): QueryResult<UserPermissionsDTO> {
        try {
            val user = userRepository.findById(query.userId)
                ?: return QueryResult.NotFound("用户不存在")

            val permissions = UserPermissionsDTO.fromUser(user)

            return QueryResult.Success(permissions)

        } catch (e: Exception) {
            return QueryResult.Error("获取用户权限失败: ${e.message}")
        }
    }
}
