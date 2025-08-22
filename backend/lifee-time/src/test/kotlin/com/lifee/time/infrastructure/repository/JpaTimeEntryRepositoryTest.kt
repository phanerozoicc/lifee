package com.lifee.time.infrastructure.repository

import com.lifee.time.domain.entity.TimeEntry
import com.lifee.time.domain.valueobject.*
import com.lifee.time.infrastructure.entity.JpaTimeEntry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.*

@DataJpaTest
@ActiveProfiles("test")
class JpaTimeEntryRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var jpaTimeEntryRepositoryInterface: JpaTimeEntryRepositoryInterface

    private lateinit var timeEntryRepository: JpaTimeEntryRepository
    private lateinit var userId: UserId
    private lateinit var projectId: ProjectId
    private lateinit var timeEntryId: TimeEntryId

    @BeforeEach
    fun setUp() {
        timeEntryRepository = JpaTimeEntryRepository(jpaTimeEntryRepositoryInterface)
        userId = UserId(UUID.randomUUID())
        projectId = ProjectId(UUID.randomUUID())
        timeEntryId = TimeEntryId(UUID.randomUUID())
    }

    @Test
    fun `should save and find time entry by id`() {
        // Given
        val timeEntry = createTimeEntry()
        
        // When
        timeEntryRepository.save(timeEntry)
        val foundTimeEntry = timeEntryRepository.findById(timeEntryId)
        
        // Then
        assertNotNull(foundTimeEntry)
        assertEquals(timeEntry.id, foundTimeEntry?.id)
        assertEquals(timeEntry.userId, foundTimeEntry?.userId)
        assertEquals(timeEntry.projectId, foundTimeEntry?.projectId)
        assertEquals(timeEntry.description, foundTimeEntry?.description)
    }
    
    @Test
    fun `should find time entries by user id`() {
        // Given
        val timeEntry1 = createTimeEntry()
        val timeEntry2 = createTimeEntry(TimeEntryId(UUID.randomUUID()))
        val otherUserTimeEntry = createTimeEntry(
            timeEntryId = TimeEntryId(UUID.randomUUID()),
            userId = UserId(UUID.randomUUID())
        )
        
        timeEntryRepository.save(timeEntry1)
        timeEntryRepository.save(timeEntry2)
        timeEntryRepository.save(otherUserTimeEntry)
        
        // When
        val userTimeEntries = timeEntryRepository.findByUserId(userId)
        
        // Then
        assertEquals(2, userTimeEntries.size)
        assertTrue(userTimeEntries.all { it.userId == userId })
    }
    
    @Test
    fun `should find time entries by project id`() {
        // Given
        val timeEntry1 = createTimeEntry()
        val timeEntry2 = createTimeEntry(TimeEntryId(UUID.randomUUID()))
        val otherProjectTimeEntry = createTimeEntry(
            timeEntryId = TimeEntryId(UUID.randomUUID()),
            projectId = ProjectId(UUID.randomUUID())
        )
        
        timeEntryRepository.save(timeEntry1)
        timeEntryRepository.save(timeEntry2)
        timeEntryRepository.save(otherProjectTimeEntry)
        
        // When
        val projectTimeEntries = timeEntryRepository.findByProjectId(projectId)
        
        // Then
        assertEquals(2, projectTimeEntries.size)
        assertTrue(projectTimeEntries.all { it.projectId == projectId })
    }
    
    @Test
    fun `should find time entries by user and project`() {
        // Given
        val timeEntry1 = createTimeEntry()
        val timeEntry2 = createTimeEntry(TimeEntryId(UUID.randomUUID()))
        val otherUserTimeEntry = createTimeEntry(
            timeEntryId = TimeEntryId(UUID.randomUUID()),
            userId = UserId(UUID.randomUUID())
        )
        val otherProjectTimeEntry = createTimeEntry(
            timeEntryId = TimeEntryId(UUID.randomUUID()),
            projectId = ProjectId(UUID.randomUUID())
        )
        
        timeEntryRepository.save(timeEntry1)
        timeEntryRepository.save(timeEntry2)
        timeEntryRepository.save(otherUserTimeEntry)
        timeEntryRepository.save(otherProjectTimeEntry)
        
        // When
        val userProjectTimeEntries = timeEntryRepository.findByUserIdAndProjectId(userId, projectId)
        
        // Then
        assertEquals(2, userProjectTimeEntries.size)
        assertTrue(userProjectTimeEntries.all { it.userId == userId && it.projectId == projectId })
    }
    
    @Test
    fun `should find time entries by date range`() {
        // Given
        val now = LocalDateTime.now()
        val timeEntry1 = createTimeEntry(
            startTime = now.minusHours(2),
            endTime = now.minusHours(1)
        )
        val timeEntry2 = createTimeEntry(
            timeEntryId = TimeEntryId(UUID.randomUUID()),
            startTime = now.minusDays(1),
            endTime = now.minusDays(1).plusHours(1)
        )
        val oldTimeEntry = createTimeEntry(
            timeEntryId = TimeEntryId(UUID.randomUUID()),
            startTime = now.minusDays(10),
            endTime = now.minusDays(10).plusHours(1)
        )
        
        timeEntryRepository.save(timeEntry1)
        timeEntryRepository.save(timeEntry2)
        timeEntryRepository.save(oldTimeEntry)
        
        // When
        val recentTimeEntries = timeEntryRepository.findByUserIdAndDateRange(
            userId = userId,
            startDate = now.minusDays(2),
            endDate = now
        )
        
        // Then
        assertEquals(2, recentTimeEntries.size)
        assertFalse(recentTimeEntries.any { it.id == oldTimeEntry.id })
    }
    
    @Test
    fun `should find running time entry`() {
        // Given
        val runningTimeEntry = createTimeEntry(endTime = null)
        val completedTimeEntry = createTimeEntry(
            timeEntryId = TimeEntryId(UUID.randomUUID()),
            endTime = LocalDateTime.now()
        )
        
        timeEntryRepository.save(runningTimeEntry)
        timeEntryRepository.save(completedTimeEntry)
        
        // When
        val foundRunningEntry = timeEntryRepository.findRunningTimeEntry(userId)
        
        // Then
        assertNotNull(foundRunningEntry)
        assertEquals(runningTimeEntry.id, foundRunningEntry?.id)
        assertNull(foundRunningEntry?.endTime)
    }
    
    @Test
    fun `should find time entries by tags`() {
        // Given
        val timeEntry1 = createTimeEntry(tags = listOf("development", "frontend"))
        val timeEntry2 = createTimeEntry(
            timeEntryId = TimeEntryId(UUID.randomUUID()),
            tags = listOf("development", "backend")
        )
        val timeEntry3 = createTimeEntry(
            timeEntryId = TimeEntryId(UUID.randomUUID()),
            tags = listOf("testing")
        )
        
        timeEntryRepository.save(timeEntry1)
        timeEntryRepository.save(timeEntry2)
        timeEntryRepository.save(timeEntry3)
        
        // When
        val developmentEntries = timeEntryRepository.findByUserIdAndTags(
            userId = userId,
            tags = listOf("development")
        )
        
        // Then
        assertEquals(2, developmentEntries.size)
        assertTrue(developmentEntries.all { it.tags.contains("development") })
    }
    
    @Test
    fun `should calculate total duration for user`() {
        // Given
        val now = LocalDateTime.now()
        val timeEntry1 = createTimeEntry(
            startTime = now.minusHours(3),
            endTime = now.minusHours(2)
        )
        val timeEntry2 = createTimeEntry(
            timeEntryId = TimeEntryId(UUID.randomUUID()),
            startTime = now.minusHours(2),
            endTime = now.minusHours(1)
        )
        
        timeEntryRepository.save(timeEntry1)
        timeEntryRepository.save(timeEntry2)
        
        // When
        val totalDuration = timeEntryRepository.calculateTotalDuration(
            userId = userId,
            startDate = now.minusDays(1),
            endDate = now
        )
        
        // Then
        assertEquals(Duration.ofHours(2), totalDuration)
    }
    
    @Test
    fun `should calculate total duration for project`() {
        // Given
        val now = LocalDateTime.now()
        val timeEntry1 = createTimeEntry(
            startTime = now.minusHours(3),
            endTime = now.minusHours(2)
        )
        val timeEntry2 = createTimeEntry(
            timeEntryId = TimeEntryId(UUID.randomUUID()),
            startTime = now.minusHours(2),
            endTime = now.minusHours(1)
        )
        val otherProjectEntry = createTimeEntry(
            timeEntryId = TimeEntryId(UUID.randomUUID()),
            projectId = ProjectId(UUID.randomUUID()),
            startTime = now.minusHours(1),
            endTime = now
        )
        
        timeEntryRepository.save(timeEntry1)
        timeEntryRepository.save(timeEntry2)
        timeEntryRepository.save(otherProjectEntry)
        
        // When
        val projectDuration = timeEntryRepository.calculateProjectTotalDuration(
            userId = userId,
            projectId = projectId,
            startDate = now.minusDays(1),
            endDate = now
        )
        
        // Then
        assertEquals(Duration.ofHours(2), projectDuration)
    }
    
    @Test
    fun `should delete time entry`() {
        // Given
        val timeEntry = createTimeEntry()
        timeEntryRepository.save(timeEntry)
        
        // When
        timeEntryRepository.delete(timeEntry)
        val deletedTimeEntry = timeEntryRepository.findById(timeEntryId)
        
        // Then
        assertNull(deletedTimeEntry)
    }
    
    @Test
    fun `should update time entry`() {
        // Given
        val timeEntry = createTimeEntry()
        timeEntryRepository.save(timeEntry)
        
        // When
        val updatedDescription = "Updated description"
        timeEntry.updateDescription(updatedDescription)
        timeEntryRepository.save(timeEntry)
        
        val foundTimeEntry = timeEntryRepository.findById(timeEntryId)
        
        // Then
        assertNotNull(foundTimeEntry)
        assertEquals(updatedDescription, foundTimeEntry?.description)
    }
    
    @Test
    fun `should find billable time entries`() {
        // Given
        val billableEntry = createTimeEntry(billable = true)
        val nonBillableEntry = createTimeEntry(
            timeEntryId = TimeEntryId(UUID.randomUUID()),
            billable = false
        )
        
        timeEntryRepository.save(billableEntry)
        timeEntryRepository.save(nonBillableEntry)
        
        // When
        val billableEntries = timeEntryRepository.findByUserIdAndBillable(
            userId = userId,
            billable = true
        )
        
        // Then
        assertEquals(1, billableEntries.size)
        assertTrue(billableEntries.all { it.billable })
    }
    
    private fun createTimeEntry(
        timeEntryId: TimeEntryId = this.timeEntryId,
        userId: UserId = this.userId,
        projectId: ProjectId = this.projectId,
        description: String = "Test time entry",
        startTime: LocalDateTime = LocalDateTime.now().minusHours(1),
        endTime: LocalDateTime? = LocalDateTime.now(),
        billable: Boolean = false,
        tags: List<String> = emptyList()
    ): TimeEntry {
        return TimeEntry.create(
            id = timeEntryId,
            userId = userId,
            projectId = projectId,
            description = description,
            startTime = startTime,
            taskId = null,
            billable = billable,
            tags = tags
        ).also { entry ->
            endTime?.let { entry.stop(it) }
        }
    }
}