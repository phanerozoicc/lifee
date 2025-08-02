package com.github.phanerozoicc.infrastructure.persistence

import com.github.phanerozoicc.infrastructure.persistence.entity.UserEntity
import com.github.phanerozoicc.infrastructure.persistence.jpa.UserJpaRepository
import com.github.phanerozoicc.user.domain.User
import com.github.phanerozoicc.user.domain.UserRepository
import com.github.phanerozoicc.user.domain.UserStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * 用户仓储实现
 */
@Repository
@Transactional
class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {

    override fun save(user: User): User {
        val entity = UserEntity.fromDomain(user)
        val savedEntity = userJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    @Transactional(readOnly = true)
    override fun findById(id: String): User? {
        return userJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    @Transactional(readOnly = true)
    override fun findByUsername(username: String): User? {
        return userJpaRepository.findByUsername(username)
            ?.toDomain()
    }

    @Transactional(readOnly = true)
    override fun findByEmail(email: String): User? {
        return userJpaRepository.findByEmail(email)
            ?.toDomain()
    }

    @Transactional(readOnly = true)
    override fun existsByUsername(username: String): Boolean {
        return userJpaRepository.existsByUsername(username)
    }

    @Transactional(readOnly = true)
    override fun existsByEmail(email: String): Boolean {
        return userJpaRepository.existsByEmail(email)
    }

    override fun delete(user: User) {
        userJpaRepository.deleteById(user.id.toString())
    }

    @Transactional(readOnly = true)
    override fun findAllActiveUsers(): List<User> {
        return userJpaRepository.findByStatus(UserStatus.ACTIVE)
            .map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<User> {
        return userJpaRepository.findAll(pageable)
            .map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    override fun count(): Long {
        return userJpaRepository.count()
    }
}