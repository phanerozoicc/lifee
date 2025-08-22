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
import java.time.LocalDateTime
import java.util.*

@WebMvcTest(TimeTrackingController::class)
class TimeTrackingControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var timeTrackingApplicationService: TimeTrackingApplicationService

    private lateinit var userId: UserId
    private lateinit var projectId: ProjectId
    private lateinit var timeEntryId: TimeEntryId

    @BeforeEach
    fun setUp() {
        userId = UserId(UUID.randomUUID())
        projectId = ProjectId(UUID.randomUUID())
        timeEntryId = TimeEntryId(UUID.randomUUID())
    }

    @Test
    fun `should start time tracking successfully`() {
        // Given
        val request = StartTimeTrackingRequest(
            projectId = projectId.value,
            description = "Working on feature",
            taskId = null,
            tags = emptyList(),
            billable = false
        )
        
        whenever(
            timeTrackingApplicationService.startTimeTracking(
                userId = userId,
                projectId = projectId,
                description = request.description,
                taskId = null,
                tags = request.tags,
                billable = request.billable
            )
        ).thenReturn(timeEntryId)
        
        // When & Then
        mockMvc.perform(
            post("/api/time-tracking/start")
                .header("X-User-Id", userId.value.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.timeEntryId").value(timeEntryId.value.toString()))
    }
    
    @Test
    fun `should return bad request when start time tracking with invalid data`() {
        // Given
        val request = StartTimeTrackingRequest(
            projectId = projectId.value,
            description = "", // Invalid empty description
            taskId = null,
            tags = emptyList(),
            billable = false
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/time-tracking/start")
                .header("X-User-Id", userId.value.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }
    
    @Test
    fun `should stop time tracking successfully`() {
        // Given
        val request = StopTimeTrackingRequest(
            timeEntryId = timeEntryId.value,
            endTime = LocalDateTime.now()
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/time-tracking/stop")
                .header("X-User-Id", userId.value.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
        
        verify(timeTrackingApplicationService).stopTimeTracking(
            userId = userId,
            timeEntryId = timeEntryId,
            endTime = request.endTime
        )
    }
    
    @Test
    fun `should pause time tracking successfully`() {
        // Given
        val request = PauseTimeTrackingRequest(
            timeEntryId = timeEntryId.value,
            pauseTime = LocalDateTime.now()
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/time-tracking/pause")
                .header("X-User-Id", userId.value.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
        
        verify(timeTrackingApplicationService).pauseTimeTracking(
            userId = userId,
            timeEntryId = timeEntryId,
            pauseTime = request.pauseTime
        )
    }
    
    @Test
    fun `should resume time tracking successfully`() {
        // Given
        val request = ResumeTimeTrackingRequest(
            timeEntryId = timeEntryId.value,
            resumeTime = LocalDateTime.now()
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/time-tracking/resume")
                .header("X-User-Id", userId.value.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
        
        verify(timeTrackingApplicationService).resumeTimeTracking(
            userId = userId,
            timeEntryId = timeEntryId,
            resumeTime = request.resumeTime
        )
    }
    
    @Test
    fun `should update description successfully`() {
        // Given
        val request = UpdateDescriptionRequest(
            timeEntryId = timeEntryId.value,
            description = "Updated description"
        )
        
        // When & Then
        mockMvc.perform(
            put("/api/time-tracking/description")
                .header("X-User-Id", userId.value.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
        
        verify(timeTrackingApplicationService).updateDescription(
            userId = userId,
            timeEntryId = timeEntryId,
            description = request.description
        )
    }
    
    @Test
    fun `should update project successfully`() {
        // Given
        val newProjectId = ProjectId(UUID.randomUUID())
        val request = UpdateProjectRequest(
            timeEntryId = timeEntryId.value,
            projectId = newProjectId.value
        )
        
        // When & Then
        mockMvc.perform(
            put("/api/time-tracking/project")
                .header("X-User-Id", userId.value.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
        
        verify(timeTrackingApplicationService).updateProject(
            userId = userId,
            timeEntryId = timeEntryId,
            projectId = newProjectId
        )
    }
    
    @Test
    fun `should update tags successfully`() {
        // Given
        val request = UpdateTagsRequest(
            timeEntryId = timeEntryId.value,
            tags = listOf("development", "frontend")
        )
        
        // When & Then
        mockMvc.perform(
            put("/api/time-tracking/tags")
                .header("X-User-Id", userId.value.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
        
        verify(timeTrackingApplicationService).updateTags(
            userId = userId,
            timeEntryId = timeEntryId,
            tags = request.tags
        )
    }
    
    @Test
    fun `should update billable status successfully`() {
        // Given
        val request = UpdateBillableStatusRequest(
            timeEntryId = timeEntryId.value,
            billable = true
        )
        
        // When & Then
        mockMvc.perform(
            put("/api/time-tracking/billable")
                .header("X-User-Id", userId.value.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
        
        verify(timeTrackingApplicationService).updateBillableStatus(
            userId = userId,
            timeEntryId = timeEntryId,
            billable = request.billable
        )
    }
    
    @Test
    fun `should delete time entry successfully`() {
        // When & Then
        mockMvc.perform(
            delete("/api/time-tracking/{timeEntryId}", timeEntryId.value)
                .header("X-User-Id", userId.value.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
        
        verify(timeTrackingApplicationService).deleteTimeEntry(
            userId = userId,
            timeEntryId = timeEntryId
        )
    }
    
    @Test
    fun `should get current running entry successfully`() {
        // Given
        val runningEntry = createTimeEntryResponse()
        whenever(timeTrackingApplicationService.getCurrentRunningEntry(userId))
            .thenReturn(runningEntry)
        
        // When & Then
        mockMvc.perform(
            get("/api/time-tracking/current")
                .header("X-User-Id", userId.value.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(runningEntry.id.toString()))
            .andExpect(jsonPath("$.description").value(runningEntry.description))
    }
    
    @Test
    fun `should get time entries successfully`() {
        // Given
        val startDate = LocalDateTime.now().minusDays(7)
        val endDate = LocalDateTime.now()
        val timeEntries = listOf(
            createTimeEntryResponse(),
            createTimeEntryResponse()
        )
        
        whenever(
            timeTrackingApplicationService.getTimeEntries(
                userId = userId,
                projectId = null,
                startDate = startDate,
                endDate = endDate,
                page = 0,
                size = 20
            )
        ).thenReturn(timeEntries)
        
        // When & Then
        mockMvc.perform(
            get("/api/time-tracking/entries")
                .header("X-User-Id", userId.value.toString())
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("page", "0")
                .param("size", "20")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }
    
    @Test
    fun `should calculate total duration successfully`() {
        // Given
        val startDate = LocalDateTime.now().minusDays(7)
        val endDate = LocalDateTime.now()
        val totalDuration = Duration.ofHours(40)
        
        whenever(
            timeTrackingApplicationService.calculateTotalDuration(
                userId = userId,
                projectId = null,
                startDate = startDate,
                endDate = endDate
            )
        ).thenReturn(totalDuration)
        
        // When & Then
        mockMvc.perform(
            get("/api/time-tracking/duration")
                .header("X-User-Id", userId.value.toString())
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalSeconds").value(totalDuration.seconds))
    }
    
    @Test
    fun `should calculate project total duration successfully`() {
        // Given
        val startDate = LocalDateTime.now().minusDays(7)
        val endDate = LocalDateTime.now()
        val totalDuration = Duration.ofHours(20)
        
        whenever(
            timeTrackingApplicationService.calculateTotalDuration(
                userId = userId,
                projectId = projectId,
                startDate = startDate,
                endDate = endDate
            )
        ).thenReturn(totalDuration)
        
        // When & Then
        mockMvc.perform(
            get("/api/time-tracking/projects/{projectId}/duration", projectId.value)
                .header("X-User-Id", userId.value.toString())
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalSeconds").value(totalDuration.seconds))
    }
    
    @Test
    fun `should return unauthorized when user id header is missing`() {
        // Given
        val request = StartTimeTrackingRequest(
            projectId = projectId.value,
            description = "Working on feature",
            taskId = null,
            tags = emptyList(),
            billable = false
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/time-tracking/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }
    
    @Test
    fun `should handle application service exceptions`() {
        // Given
        val request = StartTimeTrackingRequest(
            projectId = projectId.value,
            description = "Working on feature",
            taskId = null,
            tags = emptyList(),
            billable = false
        )
        
        whenever(
            timeTrackingApplicationService.startTimeTracking(
                userId = userId,
                projectId = projectId,
                description = request.description,
                taskId = null,
                tags = request.tags,
                billable = request.billable
            )
        ).thenThrow(IllegalArgumentException("Project not found"))
        
        // When & Then
        mockMvc.perform(
            post("/api/time-tracking/start")
                .header("X-User-Id", userId.value.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Project not found"))
    }
    
    private fun createTimeEntryResponse(): TimeEntryResponse {
        return TimeEntryResponse(
            id = UUID.randomUUID(),
            userId = userId.value,
            projectId = projectId.value,
            projectName = "Test Project",
            taskId = null,
            taskName = null,
            description = "Test time entry",
            startTime = LocalDateTime.now().minusHours(2),
            endTime = LocalDateTime.now(),
            duration = Duration.ofHours(2),
            billable = false,
            tags = emptyList(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}