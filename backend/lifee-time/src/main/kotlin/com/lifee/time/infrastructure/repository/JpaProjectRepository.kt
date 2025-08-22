package com.lifee.time.infrastructure.repository

import com.lifee.time.domain.*
import com.lifee.time.domain.repository.ProjectRepository
import com.lifee.time.infrastructure.entity.*
import com.lifee.user.domain.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * JPA项目Repository接口
 */
interface JpaProjectRepositoryInterface : JpaRepository<ProjectEntity, String> {
    
    @Query("SELECT p FROM ProjectEntity p WHERE p.ownerId = :ownerId")
    fun findByOwnerId(@Param("ownerId") ownerId: String): List<ProjectEntity>
    
    @Query("SELECT p FROM ProjectEntity p WHERE p.teamId = :teamId")
    fun findByTeamId(@Param("teamId") teamId: String): List<ProjectEntity>
    
    @Query("""
        SELECT DISTINCT p FROM ProjectEntity p 
        LEFT JOIN p.members pm 
        WHERE p.ownerId = :userId OR pm.userId = :userId
    """)
    fun findAccessibleByUserId(@Param("userId") userId: String): List<ProjectEntity>
    
    @Query("""
        SELECT p FROM ProjectEntity p 
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    fun findByNameContainingIgnoreCase(@Param("name") name: String): List<ProjectEntity>
    
    @Query("SELECT p FROM ProjectEntity p WHERE p.isActive = true")
    fun findActiveProjects(): List<ProjectEntity>
    
    @Query("SELECT p FROM ProjectEntity p WHERE p.isArchived = true")
    fun findArchivedProjects(): List<ProjectEntity>
    
    @Query("""
        SELECT p FROM ProjectEntity p 
        WHERE LOWER(p.clientName) LIKE LOWER(CONCAT('%', :clientName, '%'))
    """)
    fun findByClientNameContainingIgnoreCase(@Param("clientName") clientName: String): List<ProjectEntity>
    
    @Query("SELECT COUNT(p) FROM ProjectEntity p WHERE p.ownerId = :ownerId")
    fun countByOwnerId(@Param("ownerId") ownerId: String): Long
    
    @Query("SELECT COUNT(p) FROM ProjectEntity p WHERE p.teamId = :teamId")
    fun countByTeamId(@Param("teamId") teamId: String): Long
    
    @Query("""
        SELECT COUNT(DISTINCT p) FROM ProjectEntity p 
        LEFT JOIN p.members pm 
        WHERE p.ownerId = :userId OR pm.userId = :userId
    """)
    fun countAccessibleByUserId(@Param("userId") userId: String): Long
    
    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END 
        FROM ProjectEntity p 
        WHERE p.name = :name AND p.ownerId = :ownerId
    """)
    fun existsByNameAndOwnerId(@Param("name") name: String, @Param("ownerId") ownerId: String): Boolean
}

/**
 * 项目Repository实现
 */
@Repository
class JpaProjectRepository(
    private val jpaRepository: JpaProjectRepositoryInterface,
    private val memberRepository: JpaProjectMemberRepositoryInterface
) : ProjectRepository {
    
    override fun save(project: Project): Project {
        val entity = project.toEntity()
        val savedEntity = jpaRepository.save(entity)
        
        // 保存项目成员
        memberRepository.deleteByProjectId(project.projectId.value)
        project.members.forEach { member ->
            memberRepository.save(member.toEntity(project.projectId.value))
        }
        
        return savedEntity.toDomain()
    }
    
    override fun findById(projectId: ProjectId): Project? {
        return jpaRepository.findById(projectId.value)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    override fun findByOwnerId(ownerId: UserId): List<Project> {
        return jpaRepository.findByOwnerId(ownerId.value)
            .map { it.toDomain() }
    }
    
    override fun findByTeamId(teamId: TeamId): List<Project> {
        return jpaRepository.findByTeamId(teamId.value)
            .map { it.toDomain() }
    }
    
    override fun findAccessibleByUserId(userId: UserId): List<Project> {
        return jpaRepository.findAccessibleByUserId(userId.value)
            .map { it.toDomain() }
    }
    
    override fun searchByName(name: String): List<Project> {
        return jpaRepository.findByNameContainingIgnoreCase(name)
            .map { it.toDomain() }
    }
    
    override fun findActiveProjects(): List<Project> {
        return jpaRepository.findActiveProjects()
            .map { it.toDomain() }
    }
    
    override fun findArchivedProjects(): List<Project> {
        return jpaRepository.findArchivedProjects()
            .map { it.toDomain() }
    }
    
    override fun findByClientName(clientName: String): List<Project> {
        return jpaRepository.findByClientNameContainingIgnoreCase(clientName)
            .map { it.toDomain() }
    }
    
    override fun countByOwnerId(ownerId: UserId): Long {
        return jpaRepository.countByOwnerId(ownerId.value)
    }
    
    override fun countByTeamId(teamId: TeamId): Long {
        return jpaRepository.countByTeamId(teamId.value)
    }
    
    override fun countAccessibleByUserId(userId: UserId): Long {
        return jpaRepository.countAccessibleByUserId(userId.value)
    }
    
    override fun delete(projectId: ProjectId) {
        memberRepository.deleteByProjectId(projectId.value)
        jpaRepository.deleteById(projectId.value)
    }
    
    override fun existsById(projectId: ProjectId): Boolean {
        return jpaRepository.existsById(projectId.value)
    }
    
    override fun existsByNameAndOwnerId(name: String, ownerId: UserId): Boolean {
        return jpaRepository.existsByNameAndOwnerId(name, ownerId.value)
    }
}

/**
 * JPA项目成员Repository接口
 */
interface JpaProjectMemberRepositoryInterface : JpaRepository<ProjectMemberEntity, Long> {
    
    @Query("SELECT pm FROM ProjectMemberEntity pm WHERE pm.projectId = :projectId")
    fun findByProjectId(@Param("projectId") projectId: String): List<ProjectMemberEntity>
    
    @Query("SELECT pm FROM ProjectMemberEntity pm WHERE pm.userId = :userId")
    fun findByUserId(@Param("userId") userId: String): List<ProjectMemberEntity>
    
    @Query("""
        SELECT pm FROM ProjectMemberEntity pm 
        WHERE pm.projectId = :projectId AND pm.userId = :userId
    """)
    fun findByProjectIdAndUserId(
        @Param("projectId") projectId: String,
        @Param("userId") userId: String
    ): ProjectMemberEntity?
    
    @Query("DELETE FROM ProjectMemberEntity pm WHERE pm.projectId = :projectId")
    fun deleteByProjectId(@Param("projectId") projectId: String)
    
    @Query("""
        DELETE FROM ProjectMemberEntity pm 
        WHERE pm.projectId = :projectId AND pm.userId = :userId
    """)
    fun deleteByProjectIdAndUserId(
        @Param("projectId") projectId: String,
        @Param("userId") userId: String
    )
}