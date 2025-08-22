package com.lifee.time.web

import com.lifee.time.application.TimeTrackingApplicationService
import com.lifee.time.domain.valueobject.*
import com.lifee.time.web.dto.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@WebMvcTest(ProjectController::class)
class ProjectControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var timeTrackingApplicationService: TimeTrackingApplicationService

    private lateinit var userId: UserId
    private lateinit var projectId: ProjectId
    private lateinit var teamId: TeamId

    @BeforeEach
    fun setUp() {
        userId = UserId(UUID.randomUUID())
        projectId = ProjectId(UUID.randomUUID())
        teamId = TeamId(UUID.randomUUID())
    }

    @Test
    fun `should create project successfully`() {
        // Given
        val request = CreateProjectRequest(
            name = "Test Project",
            description = "Test project description",
            color = "#FF5733",
            billable = true,
            hourlyRate = BigDecimal("50.00"),
            teamId = teamId.value,
            isPublic = false
        )
        
        whenever(
            timeTrackingApplicationService.createProject(
                userId = userId,
                name = request.name,
                description = request.description,
                color = request.color,
                billable = request.billable,
                hourlyRate = request.hourlyRate,
                teamId = teamId,
                isPublic = request.isPublic
            )
        ).thenReturn(projectId)
        
        // When & Then
        mockMvc.perform(
            post("/api/projects")
                .header("X-User-Id", userId.value.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.projectId").value(projectId.value.toString()))
    }
    
    @Test
    fun `should return bad request when create project with invalid data`() {
        // Given
        val request = CreateProjectRequest(
            name = "", // Invalid empty name
            description = "Test project description",
            color = "#FF5733",
            billable = true,
            hourlyRate = BigDecimal("50.00"),
            teamId = teamId.value,
            isPublic = false
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/projects")
                .header("X-User-Id", userId.value.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }
    
    @Test
    fun `should get project by id successfully`() {
        // Given
        val project = createProjectResponse()
        whenever(timeTrackingApplicationService.getProject(userId, projectId))
            .thenReturn(project)
        
        // When & Then
        mockMvc.perform(
            get("/api/projects/{projectId}", projectId.value)
                .header("X-User-Id", userId.value.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(project.id.toString()))
            .andExpect(jsonPath("$.name").value(project.name))
            .andExpect(jsonPath("$.description").value(project.description))
    }
    
    @Test
    fun `should update project successfully`() {
        // Given
        val request = UpdateProjectRequest(
            name = "Updated Project",
            description = "Updated description",
            color = "#33FF57",
            billable = false,
            hourlyRate = BigDecimal("60.00"),
            isPublic = true
        )
        
        // When & Then
        mockMvc.perform(
            put("/api/projects/{projectId}", projectId.value)
                .header("X-User-Id", userId.value.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
        
        verify(timeTrackingApplicationService).updateProject(
            userId = userId,
            projectId = projectId,
            name = request.name,
            description = request.description,
            color = request.color,
            billable = request.billable,
            hourlyRate = request.hourlyRate,
            isPublic = request.isPublic
        )
    }
    
    @Test
    fun `should archive project successfully`() {
        // When & Then
        mockMvc.perform(
            post("/api/projects/{projectId}/archive", projectId.value)
                .header("X-User-Id", userId.value.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
        
        verify(timeTrackingApplicationService).archiveProject(userId, projectId)
    }
    
    @Test
    fun `should restore project successfully`() {
        // When & Then
        mockMvc.perform(
            post("/api/projects/{projectId}/restore", projectId.value)
                .header("X-User-Id", userId.value.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
        
        verify(timeTrackingApplicationService).restoreProject(userId, projectId)
    }
    
    @Test
    fun `should delete project successfully`() {
        // When & Then
        mockMvc.perform(
            delete("/api/projects/{projectId}", projectId.value)
                .header("X-User-Id", userId.value.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
        
        verify(timeTrackingApplicationService).deleteProject(userId, projectId)
    }
    
    @Test
    fun `should duplicate project successfully`() {
        // Given
        val request = DuplicateProjectRequest(
            name = "Duplicated Project",
            includeMembers = true,
            includeTasks = false
        )
        
        val newProjectId = ProjectId(UUID.randomUUID())
        whenever(
            timeTrackingApplicationService.duplicateProject(
                userId = userId,
                projectId = projectId,
                newName = request.name,
                includeMembers = request.includeMembers,
                includeTasks = request.includeTasks
            )
        ).thenReturn(newProjectId)
        
        // When & Then
        mockMvc.perform(
            post("/api/projects/{projectId}/duplicate", projectId.value)
                .header("X-User-Id", userId.value.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.projectId").value(newProjectId.value.toString()))
    }
    
    @Test
    fun `should add project member successfully`() {
        // Given
        val memberId = UserId(UUID.randomUUID())
        val request = AddProjectMemberRequest(
            userId = memberId.value,
            role = "MEMBER",
            hourlyRate = BigDecimal("40.00")
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/projects/{projectId}/members", projectId.value)
                .header("X-User-Id", userId.value.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
        
        verify(timeTrackingApplicationService).addProjectMember(
            userId = userId,
            projectId = projectId,
            memberId = memberId,
            role = request.role,
            hourlyRate = request.hourlyRate
        )
    }
    
    @Test
    fun `should remove project member successfully`() {
        // Given
        val memberId = UserId(UUID.randomUUID())
        
        // When & Then
        mockMvc.perform(
            delete("/api/projects/{projectId}/members/{memberId}", projectId.value, memberId.value)
                .header("X-User-Id", userId.value.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
        
        verify(timeTrackingApplicationService).removeProjectMember(
            userId = userId,
            projectId = projectId,
            memberId = memberId
        )
    }
    
    @Test
    fun `should update project member role successfully`() {
        // Given
        val memberId = UserId(UUID.randomUUID())
        val request = UpdateProjectMemberRequest(
            role = "ADMIN",
            hourlyRate = BigDecimal("70.00")
        )
        
        // When & Then
        mockMvc.perform(
            put("/api/projects/{projectId}/members/{memberId}", projectId.value, memberId.value)
                .header("X-User-Id", userId.value.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
        
        verify(timeTrackingApplicationService).updateProjectMemberRole(
            userId = userId,
            projectId = projectId,
            memberId = memberId,
            role = request.role,
            hourlyRate = request.hourlyRate
        )
    }
    
    @Test
    fun `should get user projects successfully`() {
        // Given
        val projects = listOf(
            createProjectResponse(),
            createProjectResponse()
        )
        
        whenever(
            timeTrackingApplicationService.getUserProjects(
                userId = userId,
                includeArchived = false,
                page = 0,
                size = 20
            )
        ).thenReturn(projects)
        
        // When & Then
        mockMvc.perform(
            get("/api/projects")
                .header("X-User-Id", userId.value.toString())
                .param("includeArchived", "false")
                .param("page", "0")
                .param("size", "20")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }
    
    @Test
    fun `should search projects successfully`() {
        // Given
        val projects = listOf(createProjectResponse())
        
        whenever(
            timeTrackingApplicationService.searchProjects(
                userId = userId,
                query = "test",
                teamId = null,
                page = 0,
                size = 20
            )
        ).thenReturn(projects)
        
        // When & Then
        mockMvc.perform(
            get("/api/projects/search")
                .header("X-User-Id", userId.value.toString())
                .param("query", "test")
                .param("page", "0")
                .param("size", "20")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
    }
    
    @Test
    fun `should get project statistics successfully`() {
        // Given
        val statistics = ProjectStatisticsResponse(
            totalProjects = 10,
            activeProjects = 8,
            archivedProjects = 2,
            totalTimeTracked = Duration.ofHours(100),
            totalRevenue = BigDecimal("5000.00"),
            averageHourlyRate = BigDecimal("50.00")
        )
        
        whenever(timeTrackingApplicationService.getProjectStatistics(userId))
            .thenReturn(statistics)
        
        // When & Then
        mockMvc.perform(
            get("/api/projects/statistics")
                .header("X-User-Id", userId.value.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalProjects").value(10))
            .andExpect(jsonPath("$.activeProjects").value(8))
            .andExpect(jsonPath("$.archivedProjects").value(2))
    }
    
    @Test
    fun `should return not found when project does not exist`() {
        // Given
        whenever(timeTrackingApplicationService.getProject(userId, projectId))
            .thenThrow(IllegalArgumentException("Project not found"))
        
        // When & Then
        mockMvc.perform(
            get("/api/projects/{projectId}", projectId.value)
                .header("X-User-Id", userId.value.toString())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Project not found"))
    }
    
    @Test
    fun `should return unauthorized when user id header is missing`() {
        // Given
        val request = CreateProjectRequest(
            name = "Test Project",
            description = "Test project description",
            color = "#FF5733",
            billable = true,
            hourlyRate = BigDecimal("50.00"),
            teamId = teamId.value,
            isPublic = false
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }
    
    private fun createProjectResponse(): ProjectResponse {
        return ProjectResponse(
            id = UUID.randomUUID(),
            name = "Test Project",
            description = "Test project description",
            color = "#FF5733",
            billable = true,
            hourlyRate = BigDecimal("50.00"),
            ownerId = userId.value,
            ownerName = "Test User",
            teamId = teamId.value,
            teamName = "Test Team",
            isPublic = false,
            isArchived = false,
            memberCount = 5,
            taskCount = 10,
            totalTimeTracked = Duration.ofHours(40),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}