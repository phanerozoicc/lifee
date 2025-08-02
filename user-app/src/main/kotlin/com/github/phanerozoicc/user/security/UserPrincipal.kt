package com.github.phanerozoicc.infrastructure.security

import com.github.phanerozoicc.user.domain.User
import com.github.phanerozoicc.user.domain.UserStatus
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * 用户主体
 */
data class UserPrincipal(
    val userId: String,
    private val username: String,
    private val email: String,
    private val password: String,
    private val status: UserStatus,
    private val authorities: Collection<GrantedAuthority>
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = status != UserStatus.SUSPENDED

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = status == UserStatus.ACTIVE

    companion object {
        fun create(user: User): UserPrincipal {
            val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
            
            return UserPrincipal(
                userId = user.id.toString(),
                username = user.getUsername().value,
                email = user.getEmail().value,
                password = user.getPasswordHash().value,
                status = user.getStatus(),
                authorities = authorities
            )
        }
    }
}