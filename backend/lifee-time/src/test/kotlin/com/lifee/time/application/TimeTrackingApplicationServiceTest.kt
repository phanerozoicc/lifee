package com.lifee.time.application

import com.lifee.time.domain.entity.*
import com.lifee.time.domain.repository.*
import com.lifee.time.domain.service.*
import com.lifee.time.domain.valueobject.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.*
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime
import java.util.*

class TimeTrackingApplicationServiceTest {

    private lateinit var timeEntryRepository: TimeEntryRepository
    private lateinit var projectRepository: ProjectRepository
    private lateinit var taskRepository: TaskRepository
    private lateinit var teamRepository: TeamRepository
    private lateinit var timeTrackingDomainService: TimeTrackingDomainService
    private lateinit var projectDomainService: ProjectDomainService
    private lateinit var taskDomainService: TaskDomainService
    private lateinit var teamDomainService: TeamDomainService
    private lateinit var reportDomainService: ReportDomainService
    private lateinit var statisticsDomainService: StatisticsDomainService
    private lateinit var eventPublisher: ApplicationEventPublisher
    private lateinit var timeTrackingApplicationService: TimeTrackingApplicationService

    @BeforeEach
    fun setUp() {
        timeEntryRepository = mock()
        projectRepository = mock()
        taskRepository = mock()
        teamRepository = mock()
        timeTrackingDomainService = mock()
        projectDomainService = mock()
        taskDomainService = mock()
        teamDomainService = mock()
        reportDomainService = mock()
        statisticsDomainService = mock()
        eventPublisher = mock()
        
        timeTrackingApplicationService = TimeTrackingApplicationService(
            timeEntryRepository = timeEntryRepository,
            projectRepository = projectRepository,
            taskRepository = taskRepository,
            teamRepository = teamRepository,
            timeTrackingDomainService = timeTrackingDomainService,
            projectDomainService = projectDomainService,
            taskDomainService = taskDomainService,
            teamDomainService = teamDomainService,
            reportDomainService = reportDomainService,
            statisticsDomainService = statisticsDomainService,
            eventPublisher = eventPublisher
        )
    }

    @Test
    fun `should start time tracking successfully`() {
        // Given
        val userId = UserId(UUID.randomUUID())
        val projectId = ProjectId(UUID.randomUUID())
        val description = "Working on feature"
        val project = createTestProject(projectId, userId)
        val timeEntry = createTestTimeEntry(userId, projectId, description)
        
        whenever(projectRepository.findById(projectId)).thenReturn(project)
        whenever(timeEntryRepository.findRunningByUserId(userId)).thenReturn(emptyList())
        whenever(timeTrackingDomainService.canStartTimeEntry(userId, projectId)).thenReturn(true)
        whenever(timeEntryRepository.save(any<TimeEntry>())).thenReturn(timeEntry)
        
        // When
        val result = timeTrackingApplicationService.startTimeTracking(
            userId = userId,
            projectId = projectId,
            description = description
        )
        
        // Then
        assertEquals(timeEntry.id, result)
        verify(timeEntryRepository).save(any<TimeEntry>())
        verify(eventPublisher).publishEvent(any())
    }
    
    @Test
    fun `should throw exception when starting time tracking without project access`() {
        // Given
        val userId = UserId(UUID.randomUUID())
        val projectId = ProjectId(UUID.randomUUID())
        val description = "Working on feature"
        val project = createTestProject(projectId, UserId(UUID.randomUUID())) // Different owner
        
        whenever(projectRepository.findById(projectId)).thenReturn(project)
        whenever(timeTrackingDomainService.canStartTimeEntry(userId, projectId)).thenReturn(false)
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            timeTrackingApplicationService.startTimeTracking(
                userId = userId,
                projectId = projectId,
                description = description
            )
        }
    }
    
    @Test
    fun `should stop time tracking successfully`() {
        // Given
        val userId = UserId(UUID.randomUUID())
        val timeEntryId = TimeEntryId(UUID.randomUUID())
        val timeEntry = createTestTimeEntry(userId, ProjectId(UUID.randomUUID()), "Test")
        val endTime = LocalDateTime.now()
        
        whenever(timeEntryRepository.findById(timeEntryId)).thenReturn(timeEntry)
        whenever(timeTrackingDomainService.canModifyTimeEntry(userId, timeEntry)).thenReturn(true)
        whenever(timeEntryRepository.save(timeEntry)).thenReturn(timeEntry)
        
        // When
        timeTrackingApplicationService.stopTimeTracking(userId, timeEntryId, endTime)
        
        // Then
        verify(timeEntryRepository).save(timeEntry)
        verify(eventPublisher).publishEvent(any())
        assertEquals(endTime, timeEntry.endTime)
    }
    
    @Test
    fun `should pause time tracking successfully`() {
        // Given
        val userId = UserId(UUID.randomUUID())
        val timeEntryId = TimeEntryId(UUID.randomUUID())
        val timeEntry = createTestTimeEntry(userId, ProjectId(UUID.randomUUID()), "Test")
        val pauseTime = LocalDateTime.now()
        
        whenever(timeEntryRepository.findById(timeEntryId)).thenReturn(timeEntry)
        whenever(timeTrackingDomainService.canModifyTimeEntry(userId, timeEntry)).thenReturn(true)
        whenever(timeEntryRepository.save(timeEntry)).thenReturn(timeEntry)
        
        // When
        timeTrackingApplicationService.pauseTimeTracking(userId, timeEntryId, pauseTime)
        
        // Then
        verify(timeEntryRepository).save(timeEntry)
        verify(eventPublisher).publishEvent(any())
        assertTrue(timeEntry.isPaused())
    }
    
    @Test
    fun `should resume time tracking successfully`() {
        // Given
        val userId = UserId(UUID.randomUUID())
        val timeEntryId = TimeEntryId(UUID.randomUUID())
        val timeEntry = createTestTimeEntry(userId, ProjectId(UUID.randomUUID()), "Test")
        timeEntry.pause(LocalDateTime.now().minusMinutes(30))
        val resumeTime = LocalDateTime.now()
        
        whenever(timeEntryRepository.findById(timeEntryId)).thenReturn(timeEntry)
        whenever(timeTrackingDomainService.canModifyTimeEntry(userId, timeEntry)).thenReturn(true)
        whenever(timeEntryRepository.save(timeEntry)).thenReturn(timeEntry)
        
        // When
        timeTrackingApplicationService.resumeTimeTracking(userId, timeEntryId, resumeTime)
        
        // Then
        verify(timeEntryRepository).save(timeEntry)
        verify(eventPublisher).publishEvent(any())
        assertFalse(timeEntry.isPaused())
    }
    
    @Test
    fun `should update time entry description successfully`() {
        // Given
        val userId = UserId(UUID.randomUUID())
        val timeEntryId = TimeEntryId(UUID.randomUUID())
        val timeEntry = createTestTimeEntry(userId, ProjectId(UUID.randomUUID()), "Old description")
        val newDescription = "New description"
        
        whenever(timeEntryRepository.findById(timeEntryId)).thenReturn(timeEntry)
        whenever(timeTrackingDomainService.canModifyTimeEntry(userId, timeEntry)).thenReturn(true)
        whenever(timeEntryRepository.save(timeEntry)).thenReturn(timeEntry)
        
        // When
        timeTrackingApplicationService.updateDescription(userId, timeEntryId, newDescription)
        
        // Then
        verify(timeEntryRepository).save(timeEntry)
        verify(eventPublisher).publishEvent(any())
        assertEquals(newDescription, timeEntry.description)
    }
    
    @Test
    fun `should delete time entry successfully`() {
        // Given
        val userId = UserId(UUID.randomUUID())
        val timeEntryId = TimeEntryId(UUID.randomUUID())
        val timeEntry = createTestTimeEntry(userId, ProjectId(UUID.randomUUID()), "Test")
        
        whenever(timeEntryRepository.findById(timeEntryId)).thenReturn(timeEntry)
        whenever(timeTrackingDomainService.canDeleteTimeEntry(userId, timeEntry)).thenReturn(true)
        
        // When
        timeTrackingApplicationService.deleteTimeEntry(userId, timeEntryId)
        
        // Then
        verify(timeEntryRepository).delete(timeEntry)
        verify(eventPublisher).publishEvent(any())
    }
    
    @Test
    fun `should create project successfully`() {
        // Given
        val userId = UserId(UUID.randomUUID())
        val name = "New Project"
        val description = "Project description"
        val color = "#FF5733"
        val project = createTestProject(ProjectId(UUID.randomUUID()), userId)
        
        whenever(projectDomainService.canCreateProject(userId)).thenReturn(true)
        whenever(projectRepository.save(any<Project>())).thenReturn(project)
        
        // When
        val result = timeTrackingApplicationService.createProject(
            userId = userId,
            name = name,
            description = description,
            color = color
        )
        
        // Then
        assertEquals(project.id, result)
        verify(projectRepository).save(any<Project>())
        verify(eventPublisher).publishEvent(any())
    }
    
    @Test
    fun `should update project successfully`() {
        // Given
        val userId = UserId(UUID.randomUUID())
        val projectId = ProjectId(UUID.randomUUID())
        val project = createTestProject(projectId, userId)
        val newName = "Updated Project"
        val newDescription = "Updated description"
        
        whenever(projectRepository.findById(projectId)).thenReturn(project)
        whenever(projectDomainService.canManageProject(userId, project)).thenReturn(true)
        whenever(projectRepository.save(project)).thenReturn(project)
        
        // When
        timeTrackingApplicationService.updateProject(
            userId = userId,
            projectId = projectId,
            name = newName,
            description = newDescription
        )
        
        // Then
        verify(projectRepository).save(project)
        verify(eventPublisher).publishEvent(any())
        assertEquals(newName, project.name)
        assertEquals(newDescription, project.description)
    }
    
    @Test
    fun `should add project member successfully`() {
        // Given
        val userId = UserId(UUID.randomUUID())
        val projectId = ProjectId(UUID.randomUUID())
        val memberId = UserId(UUID.randomUUID())
        val project = createTestProject(projectId, userId)
        val role = ProjectRole.MEMBER
        
        whenever(projectRepository.findById(projectId)).thenReturn(project)
        whenever(projectDomainService.canManageProject(userId, project)).thenReturn(true)
        whenever(projectDomainService.canAddMember(project, memberId)).thenReturn(true)
        whenever(projectRepository.save(project)).thenReturn(project)
        
        // When
        timeTrackingApplicationService.addProjectMember(
            userId = userId,
            projectId = projectId,
            memberId = memberId,
            role = role
        )
        
        // Then
        verify(projectRepository).save(project)
        verify(eventPublisher).publishEvent(any())
        assertTrue(project.hasAccess(memberId))
    }
    
    @Test
    fun `should get user time entries successfully`() {
        // Given
        val userId = UserId(UUID.randomUUID())
        val timeRange = TimeRange(
            LocalDateTime.now().minusDays(7),
            LocalDateTime.now()
        )
        val timeEntries = listOf(
            createTestTimeEntry(userId, ProjectId(UUID.randomUUID()), "Entry 1"),
            createTestTimeEntry(userId, ProjectId(UUID.randomUUID()), "Entry 2")
        )
        
        whenever(timeEntryRepository.findByUserIdAndTimeRange(userId, timeRange)).thenReturn(timeEntries)
        
        // When
        val result = timeTrackingApplicationService.getUserTimeEntries(userId, timeRange)
        
        // Then
        assertEquals(timeEntries, result)
        verify(timeEntryRepository).findByUserIdAndTimeRange(userId, timeRange)
    }
    
    @Test
    fun `should calculate total duration successfully`() {
        // Given
        val userId = UserId(UUID.randomUUID())
        val timeRange = TimeRange(
            LocalDateTime.now().minusDays(7),
            LocalDateTime.now()
        )
        val expectedDuration = Duration.ofHours(40)
        
        whenever(timeEntryRepository.calculateTotalDuration(userId, timeRange)).thenReturn(expectedDuration)
        
        // When
        val result = timeTrackingApplicationService.calculateTotalDuration(userId, timeRange)
        
        // Then
        assertEquals(expectedDuration, result)
        verify(timeEntryRepository).calculateTotalDuration(userId, timeRange)
    }
    
    @Test
    fun `should throw exception when user cannot modify time entry`() {
        // Given
        val userId = UserId(UUID.randomUUID())
        val timeEntryId = TimeEntryId(UUID.randomUUID())
        val timeEntry = createTestTimeEntry(UserId(UUID.randomUUID()), ProjectId(UUID.randomUUID()), "Test")
        
        whenever(timeEntryRepository.findById(timeEntryId)).thenReturn(timeEntry)
        whenever(timeTrackingDomainService.canModifyTimeEntry(userId, timeEntry)).thenReturn(false)
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            timeTrackingApplicationService.updateDescription(userId, timeEntryId, "New description")
        }
    }
    
    @Test
    fun `should throw exception when project not found`() {
        // Given
        val userId = UserId(UUID.randomUUID())
        val projectId = ProjectId(UUID.randomUUID())
        
        whenever(projectRepository.findById(projectId)).thenReturn(null)
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            timeTrackingApplicationService.startTimeTracking(
                userId = userId,
                projectId = projectId,
                description = "Test"
            )
        }
    }
    
    @Test
    fun `should throw exception when time entry not found`() {
        // Given
        val userId = UserId(UUID.randomUUID())
        val timeEntryId = TimeEntryId(UUID.randomUUID())
        
        whenever(timeEntryRepository.findById(timeEntryId)).thenReturn(null)
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            timeTrackingApplicationService.stopTimeTracking(userId, timeEntryId, LocalDateTime.now())
        }
    }
    
    private fun createTestTimeEntry(
        userId: UserId,
        projectId: ProjectId,
        description: String
    ): TimeEntry {
        return TimeEntry(
            id = TimeEntryId(UUID.randomUUID()),
            userId = userId,
            projectId = projectId,
            description = description,
            startTime = LocalDateTime.now().minusHours(1)
        )
    }
    
    private fun createTestProject(
        projectId: ProjectId,
        ownerId: UserId
    ): Project {
        return Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description",
            ownerId = ownerId,
            color = "#FF5733"
        )
    }
}