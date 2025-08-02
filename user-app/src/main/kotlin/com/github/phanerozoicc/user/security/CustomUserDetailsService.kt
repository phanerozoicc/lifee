package com.github.phanerozoicc.infrastructure.security

import com.github.phanerozoicc.user.domain.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 自定义用户详情服务
 */
@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found with username: $username")
        
        return UserPrincipal.create(user)
    }

    @Transactional(readOnly = true)
    fun loadUserById(userId: String): UserDetails {
        val user = userRepository.findById(userId)
            ?: throw UsernameNotFoundException("User not found with id: $userId")
        
        return UserPrincipal.create(user)
    }
}