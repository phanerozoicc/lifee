package com.lifee.time.web

import com.lifee.time.application.TimeTrackingApplicationService
import com.lifee.time.domain.*
import com.lifee.time.web.dto.*
import com.lifee.user.domain.UserId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 团队管理控制器
 */
@RestController
@RequestMapping("/api/teams")
class TeamController(
    private val timeTrackingService: TimeTrackingApplicationService
) {
    
    /**
     * 创建团队
     */
    @PostMapping
    fun createTeam(
        @RequestBody request: CreateTeamRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TeamResponse> {
        val team = timeTrackingService.createTeam(
            name = request.name,
            description = request.description,
            ownerId = UserId(userId)
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(team.toResponse())
    }
    
    /**
     * 获取团队详情
     */
    @GetMapping("/{teamId}")
    fun getTeam(
        @PathVariable teamId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TeamResponse> {
        val team = timeTrackingService.getTeam(
            teamId = TeamId(teamId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(team.toResponse())
    }
    
    /**
     * 更新团队
     */
    @PutMapping("/{teamId}")
    fun updateTeam(
        @PathVariable teamId: String,
        @RequestBody request: UpdateTeamRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TeamResponse> {
        val team = timeTrackingService.updateTeam(
            teamId = TeamId(teamId),
            name = request.name,
            description = request.description,
            userId = UserId(userId)
        )
        return ResponseEntity.ok(team.toResponse())
    }
    
    /**
     * 添加团队成员
     */
    @PostMapping("/{teamId}/members")
    fun addTeamMember(
        @PathVariable teamId: String,
        @RequestBody request: AddTeamMemberRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TeamResponse> {
        val team = timeTrackingService.addTeamMember(
            teamId = TeamId(teamId),
            memberId = UserId(request.userId),
            role = TeamMemberRole.valueOf(request.role),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(team.toResponse())
    }
    
    /**
     * 移除团队成员
     */
    @DeleteMapping("/{teamId}/members/{memberId}")
    fun removeTeamMember(
        @PathVariable teamId: String,
        @PathVariable memberId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TeamResponse> {
        val team = timeTrackingService.removeTeamMember(
            teamId = TeamId(teamId),
            memberId = UserId(memberId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(team.toResponse())
    }
    
    /**
     * 更新团队成员角色
     */
    @PutMapping("/{teamId}/members/{memberId}/role")
    fun updateTeamMemberRole(
        @PathVariable teamId: String,
        @PathVariable memberId: String,
        @RequestBody request: UpdateTeamMemberRoleRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TeamResponse> {
        val team = timeTrackingService.updateTeamMemberRole(
            teamId = TeamId(teamId),
            memberId = UserId(memberId),
            role = TeamMemberRole.valueOf(request.role),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(team.toResponse())
    }
    
    /**
     * 离开团队
     */
    @PostMapping("/{teamId}/leave")
    fun leaveTeam(
        @PathVariable teamId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<Void> {
        timeTrackingService.leaveTeam(
            teamId = TeamId(teamId),
            userId = UserId(userId)
        )
        return ResponseEntity.noContent().build()
    }
    
    /**
     * 转移团队所有权
     */
    @PostMapping("/{teamId}/transfer-ownership")
    fun transferTeamOwnership(
        @PathVariable teamId: String,
        @RequestBody request: TransferTeamOwnershipRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TeamResponse> {
        val team = timeTrackingService.transferTeamOwnership(
            teamId = TeamId(teamId),
            newOwnerId = UserId(request.newOwnerId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(team.toResponse())
    }
    
    /**
     * 停用团队
     */
    @PostMapping("/{teamId}/deactivate")
    fun deactivateTeam(
        @PathVariable teamId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TeamResponse> {
        val team = timeTrackingService.deactivateTeam(
            teamId = TeamId(teamId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(team.toResponse())
    }
    
    /**
     * 激活团队
     */
    @PostMapping("/{teamId}/activate")
    fun activateTeam(
        @PathVariable teamId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TeamResponse> {
        val team = timeTrackingService.activateTeam(
            teamId = TeamId(teamId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(team.toResponse())
    }
    
    /**
     * 删除团队
     */
    @DeleteMapping("/{teamId}")
    fun deleteTeam(
        @PathVariable teamId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<Void> {
        timeTrackingService.deleteTeam(
            teamId = TeamId(teamId),
            userId = UserId(userId)
        )
        return ResponseEntity.noContent().build()
    }
    
    /**
     * 获取用户团队列表
     */
    @GetMapping
    fun getUserTeams(
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) active: Boolean?
    ): ResponseEntity<List<TeamResponse>> {
        val teams = timeTrackingService.getUserTeams(
            userId = UserId(userId),
            activeOnly = active ?: true
        )
        return ResponseEntity.ok(teams.map { it.toResponse() })
    }
    
    /**
     * 获取用户拥有的团队
     */
    @GetMapping("/owned")
    fun getOwnedTeams(
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<List<TeamResponse>> {
        val teams = timeTrackingService.getOwnedTeams(
            userId = UserId(userId)
        )
        return ResponseEntity.ok(teams.map { it.toResponse() })
    }
    
    /**
     * 获取用户可管理的团队
     */
    @GetMapping("/manageable")
    fun getManageableTeams(
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<List<TeamResponse>> {
        val teams = timeTrackingService.getManageableTeams(
            userId = UserId(userId)
        )
        return ResponseEntity.ok(teams.map { it.toResponse() })
    }
    
    /**
     * 搜索团队
     */
    @GetMapping("/search")
    fun searchTeams(
        @RequestParam query: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<List<TeamResponse>> {
        val teams = timeTrackingService.searchTeams(
            query = query,
            userId = UserId(userId)
        )
        return ResponseEntity.ok(teams.map { it.toResponse() })
    }
    
    /**
     * 获取团队统计
     */
    @GetMapping("/{teamId}/statistics")
    fun getTeamStatistics(
        @PathVariable teamId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TeamStatisticsResponse> {
        val statistics = timeTrackingService.getTeamStatistics(
            teamId = TeamId(teamId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(statistics.toResponse())
    }
    
    /**
     * 批量添加团队成员
     */
    @PostMapping("/{teamId}/members/batch")
    fun batchAddTeamMembers(
        @PathVariable teamId: String,
        @RequestBody request: BatchAddTeamMembersRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TeamResponse> {
        val team = timeTrackingService.batchAddTeamMembers(
            teamId = TeamId(teamId),
            memberRequests = request.members.map { 
                Pair(UserId(it.userId), TeamMemberRole.valueOf(it.role))
            },
            userId = UserId(userId)
        )
        return ResponseEntity.ok(team.toResponse())
    }
    
    /**
     * 批量移除团队成员
     */
    @DeleteMapping("/{teamId}/members/batch")
    fun batchRemoveTeamMembers(
        @PathVariable teamId: String,
        @RequestBody request: BatchRemoveTeamMembersRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TeamResponse> {
        val team = timeTrackingService.batchRemoveTeamMembers(
            teamId = TeamId(teamId),
            memberIds = request.userIds.map { UserId(it) },
            userId = UserId(userId)
        )
        return ResponseEntity.ok(team.toResponse())
    }
}

/**
 * 扩展函数：领域对象转响应DTO
 */
fun Team.toResponse(): TeamResponse {
    return TeamResponse(
        teamId = teamId.value,
        name = name,
        description = description,
        ownerId = ownerId.value,
        isActive = isActive,
        members = members.map { it.toResponse() },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun TeamMember.toResponse(): TeamMemberResponse {
    return TeamMemberResponse(
        userId = userId.value,
        role = role.name,
        isActive = isActive,
        joinedAt = joinedAt,
        updatedAt = updatedAt
    )
}

// 假设有TeamStatistics类
data class TeamStatistics(
    val totalMembers: Long,
    val activeMembers: Long,
    val totalProjects: Long,
    val totalTimeSpent: Long,
    val totalRevenue: java.math.BigDecimal?
)

fun TeamStatistics.toResponse(): TeamStatisticsResponse {
    return TeamStatisticsResponse(
        totalMembers = totalMembers,
        activeMembers = activeMembers,
        totalProjects = totalProjects,
        totalTimeSpent = totalTimeSpent,
        totalRevenue = totalRevenue
    )
}