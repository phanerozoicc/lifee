package com.lifee.time.infrastructure.repository

import com.lifee.time.domain.*
import com.lifee.time.domain.repository.TeamRepository
import com.lifee.time.infrastructure.entity.*
import com.lifee.user.domain.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * JPA团队Repository接口
 */
interface JpaTeamRepositoryInterface : JpaRepository<TeamEntity, String> {
    
    @Query("SELECT t FROM TeamEntity t WHERE t.ownerId = :ownerId")
    fun findByOwnerId(@Param("ownerId") ownerId: String): List<TeamEntity>
    
    @Query("""
        SELECT DISTINCT t FROM TeamEntity t 
        JOIN t.members tm 
        WHERE tm.userId = :memberId AND tm.isActive = true
    """)
    fun findByMemberId(@Param("memberId") memberId: String): List<TeamEntity>
    
    @Query("""
        SELECT t FROM TeamEntity t 
        WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    fun findByNameContainingIgnoreCase(@Param("name") name: String): List<TeamEntity>
    
    @Query("SELECT t FROM TeamEntity t WHERE t.isActive = true")
    fun findActiveTeams(): List<TeamEntity>
    
    @Query("SELECT t FROM TeamEntity t WHERE t.isActive = false")
    fun findInactiveTeams(): List<TeamEntity>
    
    @Query("""
        SELECT DISTINCT t FROM TeamEntity t 
        JOIN t.members tm 
        WHERE tm.userId = :memberId AND tm.role = :role AND tm.isActive = true
    """)
    fun findByMemberIdAndRole(
        @Param("memberId") memberId: String,
        @Param("role") role: TeamMemberRole
    ): List<TeamEntity>
    
    @Query("""
        SELECT DISTINCT t FROM TeamEntity t 
        JOIN t.members tm 
        WHERE (t.ownerId = :userId OR (tm.userId = :userId AND tm.role IN ('ADMIN', 'MANAGER') AND tm.isActive = true))
    """)
    fun findManageableByUserId(@Param("userId") userId: String): List<TeamEntity>
    
    @Query("SELECT COUNT(t) FROM TeamEntity t WHERE t.ownerId = :ownerId")
    fun countByOwnerId(@Param("ownerId") ownerId: String): Long
    
    @Query("""
        SELECT COUNT(DISTINCT t) FROM TeamEntity t 
        JOIN t.members tm 
        WHERE tm.userId = :memberId AND tm.isActive = true
    """)
    fun countByMemberId(@Param("memberId") memberId: String): Long
    
    @Query("SELECT COUNT(t) FROM TeamEntity t WHERE t.isActive = true")
    fun countActiveTeams(): Long
    
    @Query("""
        SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END 
        FROM TeamEntity t 
        WHERE t.name = :name AND t.ownerId = :ownerId
    """)
    fun existsByNameAndOwnerId(@Param("name") name: String, @Param("ownerId") ownerId: String): Boolean
    
    @Query("""
        SELECT CASE WHEN COUNT(tm) > 0 THEN true ELSE false END 
        FROM TeamEntity t 
        JOIN t.members tm 
        WHERE t.teamId = :teamId AND tm.userId = :userId AND tm.isActive = true
    """)
    fun isUserMember(@Param("teamId") teamId: String, @Param("userId") userId: String): Boolean
}

/**
 * JPA团队成员Repository接口
 */
interface JpaTeamMemberRepositoryInterface : JpaRepository<TeamMemberEntity, Long> {
    
    @Query("SELECT tm FROM TeamMemberEntity tm WHERE tm.teamId = :teamId")
    fun findByTeamId(@Param("teamId") teamId: String): List<TeamMemberEntity>
    
    @Query("SELECT tm FROM TeamMemberEntity tm WHERE tm.userId = :userId")
    fun findByUserId(@Param("userId") userId: String): List<TeamMemberEntity>
    
    @Query("""
        SELECT tm FROM TeamMemberEntity tm 
        WHERE tm.teamId = :teamId AND tm.userId = :userId
    """)
    fun findByTeamIdAndUserId(
        @Param("teamId") teamId: String,
        @Param("userId") userId: String
    ): TeamMemberEntity?
    
    @Query("DELETE FROM TeamMemberEntity tm WHERE tm.teamId = :teamId")
    fun deleteByTeamId(@Param("teamId") teamId: String)
    
    @Query("""
        DELETE FROM TeamMemberEntity tm 
        WHERE tm.teamId = :teamId AND tm.userId = :userId
    """)
    fun deleteByTeamIdAndUserId(
        @Param("teamId") teamId: String,
        @Param("userId") userId: String
    )
}

/**
 * 团队Repository实现
 */
@Repository
class JpaTeamRepository(
    private val jpaRepository: JpaTeamRepositoryInterface,
    private val memberRepository: JpaTeamMemberRepositoryInterface
) : TeamRepository {
    
    override fun save(team: Team): Team {
        val entity = team.toEntity()
        val savedEntity = jpaRepository.save(entity)
        
        // 保存团队成员
        memberRepository.deleteByTeamId(team.teamId.value)
        team.members.forEach { member ->
            memberRepository.save(member.toEntity(team.teamId.value))
        }
        
        return savedEntity.toDomain()
    }
    
    override fun findById(teamId: TeamId): Team? {
        return jpaRepository.findById(teamId.value)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    override fun findByOwnerId(ownerId: UserId): List<Team> {
        return jpaRepository.findByOwnerId(ownerId.value)
            .map { it.toDomain() }
    }
    
    override fun findByMemberId(memberId: UserId): List<Team> {
        return jpaRepository.findByMemberId(memberId.value)
            .map { it.toDomain() }
    }
    
    override fun findByNameContaining(name: String): List<Team> {
        return jpaRepository.findByNameContainingIgnoreCase(name)
            .map { it.toDomain() }
    }
    
    override fun findActiveTeams(): List<Team> {
        return jpaRepository.findActiveTeams()
            .map { it.toDomain() }
    }
    
    override fun findInactiveTeams(): List<Team> {
        return jpaRepository.findInactiveTeams()
            .map { it.toDomain() }
    }
    
    override fun findByMemberIdAndRole(memberId: UserId, role: TeamMemberRole): List<Team> {
        return jpaRepository.findByMemberIdAndRole(memberId.value, role)
            .map { it.toDomain() }
    }
    
    override fun findManageableByUserId(userId: UserId): List<Team> {
        return jpaRepository.findManageableByUserId(userId.value)
            .map { it.toDomain() }
    }
    
    override fun countByOwnerId(ownerId: UserId): Long {
        return jpaRepository.countByOwnerId(ownerId.value)
    }
    
    override fun countByMemberId(memberId: UserId): Long {
        return jpaRepository.countByMemberId(memberId.value)
    }
    
    override fun countActiveTeams(): Long {
        return jpaRepository.countActiveTeams()
    }
    
    override fun delete(teamId: TeamId) {
        memberRepository.deleteByTeamId(teamId.value)
        jpaRepository.deleteById(teamId.value)
    }
    
    override fun existsById(teamId: TeamId): Boolean {
        return jpaRepository.existsById(teamId.value)
    }
    
    override fun existsByNameAndOwnerId(name: String, ownerId: UserId): Boolean {
        return jpaRepository.existsByNameAndOwnerId(name, ownerId.value)
    }
    
    override fun isUserMember(teamId: TeamId, userId: UserId): Boolean {
        return jpaRepository.isUserMember(teamId.value, userId.value)
    }
}