package com.lifee.user.app.handlers

import com.lifee.common.cqrs.queries.AsyncQueryHandler
import com.lifee.user.app.dto.UserDto
import com.lifee.user.app.queries.GetUserByEmailQuery
import com.lifee.user.domain.Email
import com.lifee.user.domain.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 根据邮箱查询用户处理器
 */
@Component
class GetUserByEmailQueryHandler(
    private val userRepository: UserRepository
) : AsyncQueryHandler<GetUserByEmailQuery, UserDto?> {
    
    private val logger = LoggerFactory.getLogger(GetUserByEmailQueryHandler::class.java)
    
    @Transactional(readOnly = true)
    override suspend fun handle(query: GetUserByEmailQuery): UserDto? {
        logger.debug("处理根据邮箱查询用户: email={}", query.email)
        
        val email = Email.of(query.email)
        val user = userRepository.findByEmail(email)
        
        return user?.let { UserDto.fromDomain(it) }
    }
}