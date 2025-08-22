package com.lifee.time.web

import com.lifee.time.application.TimeTrackingApplicationService
import com.lifee.time.domain.*
import com.lifee.time.web.dto.*
import com.lifee.user.domain.UserId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 项目管理控制器
 */
@RestController
@RequestMapping("/api/projects")
class ProjectController(
    private val timeTrackingService: TimeTrackingApplicationService
) {
    
    /**
     * 创建项目
     */
    @PostMapping
    fun createProject(
        @RequestBody request: CreateProjectRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ProjectResponse> {
        val project = timeTrackingService.createProject(
            name = request.name,
            description = request.description,
            ownerId = UserId(userId),
            teamId = request.teamId?.let { TeamId(it) },
            clientName = request.clientName,
            hourlyRate = request.hourlyRate,
            estimatedHours = request.estimatedHours?.let { Duration.ofHours(it) },
            color = request.color
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(project.toResponse())
    }
    
    /**
     * 获取项目详情
     */
    @GetMapping("/{projectId}")
    fun getProject(
        @PathVariable projectId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ProjectResponse> {
        val project = timeTrackingService.getProject(
            projectId = ProjectId(projectId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(project.toResponse())
    }
    
    /**
     * 更新项目
     */
    @PutMapping("/{projectId}")
    fun updateProject(
        @PathVariable projectId: String,
        @RequestBody request: UpdateProjectRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ProjectResponse> {
        val project = timeTrackingService.updateProject(
            projectId = ProjectId(projectId),
            name = request.name,
            description = request.description,
            clientName = request.clientName,
            hourlyRate = request.hourlyRate,
            estimatedHours = request.estimatedHours?.let { Duration.ofHours(it) },
            color = request.color,
            userId = UserId(userId)
        )
        return ResponseEntity.ok(project.toResponse())
    }
    
    /**
     * 归档项目
     */
    @PostMapping("/{projectId}/archive")
    fun archiveProject(
        @PathVariable projectId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ProjectResponse> {
        val project = timeTrackingService.archiveProject(
            projectId = ProjectId(projectId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(project.toResponse())
    }
    
    /**
     * 恢复项目
     */
    @PostMapping("/{projectId}/restore")
    fun restoreProject(
        @PathVariable projectId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ProjectResponse> {
        val project = timeTrackingService.restoreProject(
            projectId = ProjectId(projectId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(project.toResponse())
    }
    
    /**
     * 删除项目
     */
    @DeleteMapping("/{projectId}")
    fun deleteProject(
        @PathVariable projectId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<Void> {
        timeTrackingService.deleteProject(
            projectId = ProjectId(projectId),
            userId = UserId(userId)
        )
        return ResponseEntity.noContent().build()
    }
    
    /**
     * 复制项目
     */
    @PostMapping("/{projectId}/duplicate")
    fun duplicateProject(
        @PathVariable projectId: String,
        @RequestParam newName: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ProjectResponse> {
        val project = timeTrackingService.duplicateProject(
            projectId = ProjectId(projectId),
            newName = newName,
            userId = UserId(userId)
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(project.toResponse())
    }
    
    /**
     * 添加项目成员
     */
    @PostMapping("/{projectId}/members")
    fun addProjectMember(
        @PathVariable projectId: String,
        @RequestBody request: AddProjectMemberRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ProjectResponse> {
        val project = timeTrackingService.addProjectMember(
            projectId = ProjectId(projectId),
            memberId = UserId(request.userId),
            role = ProjectMemberRole.valueOf(request.role),
            hourlyRate = request.hourlyRate,
            userId = UserId(userId)
        )
        return ResponseEntity.ok(project.toResponse())
    }
    
    /**
     * 移除项目成员
     */
    @DeleteMapping("/{projectId}/members/{memberId}")
    fun removeProjectMember(
        @PathVariable projectId: String,
        @PathVariable memberId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ProjectResponse> {
        val project = timeTrackingService.removeProjectMember(
            projectId = ProjectId(projectId),
            memberId = UserId(memberId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(project.toResponse())
    }
    
    /**
     * 更新项目成员角色
     */
    @PutMapping("/{projectId}/members/{memberId}/role")
    fun updateProjectMemberRole(
        @PathVariable projectId: String,
        @PathVariable memberId: String,
        @RequestBody request: UpdateProjectMemberRoleRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ProjectResponse> {
        val project = timeTrackingService.updateProjectMemberRole(
            projectId = ProjectId(projectId),
            memberId = UserId(memberId),
            role = ProjectMemberRole.valueOf(request.role),
            hourlyRate = request.hourlyRate,
            userId = UserId(userId)
        )
        return ResponseEntity.ok(project.toResponse())
    }
    
    /**
     * 获取用户项目列表
     */
    @GetMapping
    fun getUserProjects(
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) archived: Boolean?,
        @RequestParam(required = false) teamId: String?
    ): ResponseEntity<List<ProjectResponse>> {
        val projects = timeTrackingService.getUserProjects(
            userId = UserId(userId),
            includeArchived = archived ?: false,
            teamId = teamId?.let { TeamId(it) }
        )
        return ResponseEntity.ok(projects.map { it.toResponse() })
    }
    
    /**
     * 搜索项目
     */
    @GetMapping("/search")
    fun searchProjects(
        @RequestParam query: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<List<ProjectResponse>> {
        val projects = timeTrackingService.searchProjects(
            query = query,
            userId = UserId(userId)
        )
        return ResponseEntity.ok(projects.map { it.toResponse() })
    }
    
    /**
     * 获取项目统计
     */
    @GetMapping("/{projectId}/statistics")
    fun getProjectStatistics(
        @PathVariable projectId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ProjectStatisticsResponse> {
        val statistics = timeTrackingService.getProjectStatistics(
            projectId = ProjectId(projectId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(statistics.toResponse())
    }
}

/**
 * 扩展函数：领域对象转响应DTO
 */
fun Project.toResponse(): ProjectResponse {
    return ProjectResponse(
        projectId = projectId.value,
        name = name,
        description = description,
        ownerId = ownerId.value,
        teamId = teamId?.value,
        clientName = clientName,
        hourlyRate = hourlyRate,
        estimatedHours = estimatedHours?.toHours(),
        color = color,
        isActive = isActive,
        isArchived = isArchived,
        members = members.map { it.toResponse() },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ProjectMember.toResponse(): ProjectMemberResponse {
    return ProjectMemberResponse(
        userId = userId.value,
        role = role.name,
        hourlyRate = hourlyRate,
        isActive = isActive,
        joinedAt = joinedAt,
        updatedAt = updatedAt
    )
}

// 假设有ProjectStatistics类
data class ProjectStatistics(
    val totalProjects: Long,
    val activeProjects: Long,
    val archivedProjects: Long,
    val totalTimeSpent: Long,
    val totalRevenue: java.math.BigDecimal?
)

fun ProjectStatistics.toResponse(): ProjectStatisticsResponse {
    return ProjectStatisticsResponse(
        totalProjects = totalProjects,
        activeProjects = activeProjects,
        archivedProjects = archivedProjects,
        totalTimeSpent = totalTimeSpent,
        totalRevenue = totalRevenue
    )
}