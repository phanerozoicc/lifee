package com.lifee.user.app.handlers

import com.lifee.common.cqrs.queries.AsyncQueryHandler
import com.lifee.user.app.dto.UserDto
import com.lifee.user.app.queries.GetUserByIdQuery
import com.lifee.user.domain.UserId
import com.lifee.user.domain.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 根据ID查询用户处理器
 */
@Component
class GetUserByIdQueryHandler(
    private val userRepository: UserRepository
) : AsyncQueryHandler<GetUserByIdQuery, UserDto?> {
    
    private val logger = LoggerFactory.getLogger(GetUserByIdQueryHandler::class.java)
    
    @Transactional(readOnly = true)
    override suspend fun handle(query: GetUserByIdQuery): UserDto? {
        logger.debug("处理根据ID查询用户: userId={}", query.userId)
        
        val userId = UserId.fromString(query.userId)
        val user = userRepository.findById(userId)
        
        return user?.let { UserDto.fromDomain(it) }
    }
}