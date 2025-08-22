package com.lifee.time.domain.entity

import com.lifee.time.domain.valueobject.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime
import java.util.*

class TimeEntryTest {

    @Test
    fun `should create time entry successfully`() {
        // Given
        val timeEntryId = TimeEntryId(UUID.randomUUID())
        val userId = UserId(UUID.randomUUID())
        val projectId = ProjectId(UUID.randomUUID())
        val description = "Working on feature"
        val startTime = LocalDateTime.now()
        
        // When
        val timeEntry = TimeEntry(
            id = timeEntryId,
            userId = userId,
            projectId = projectId,
            description = description,
            startTime = startTime
        )
        
        // Then
        assertEquals(timeEntryId, timeEntry.id)
        assertEquals(userId, timeEntry.userId)
        assertEquals(projectId, timeEntry.projectId)
        assertEquals(description, timeEntry.description)
        assertEquals(startTime, timeEntry.startTime)
        assertNull(timeEntry.endTime)
        assertTrue(timeEntry.isRunning())
        assertFalse(timeEntry.billable)
        assertTrue(timeEntry.tags.isEmpty())
    }
    
    @Test
    fun `should stop time entry successfully`() {
        // Given
        val timeEntry = createRunningTimeEntry()
        val endTime = LocalDateTime.now().plusHours(2)
        
        // When
        timeEntry.stop(endTime)
        
        // Then
        assertEquals(endTime, timeEntry.endTime)
        assertFalse(timeEntry.isRunning())
        assertEquals(Duration.between(timeEntry.startTime, endTime), timeEntry.calculateDuration())
    }
    
    @Test
    fun `should throw exception when stopping already stopped time entry`() {
        // Given
        val timeEntry = createStoppedTimeEntry()
        val endTime = LocalDateTime.now()
        
        // When & Then
        assertThrows<IllegalStateException> {
            timeEntry.stop(endTime)
        }
    }
    
    @Test
    fun `should throw exception when end time is before start time`() {
        // Given
        val timeEntry = createRunningTimeEntry()
        val endTime = timeEntry.startTime.minusHours(1)
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            timeEntry.stop(endTime)
        }
    }
    
    @Test
    fun `should pause time entry successfully`() {
        // Given
        val timeEntry = createRunningTimeEntry()
        val pauseTime = LocalDateTime.now().plusHours(1)
        
        // When
        timeEntry.pause(pauseTime)
        
        // Then
        assertTrue(timeEntry.isPaused())
        assertFalse(timeEntry.isRunning())
        assertEquals(1, timeEntry.timeSessions.size)
        val session = timeEntry.timeSessions.first()
        assertEquals(timeEntry.startTime, session.startTime)
        assertEquals(pauseTime, session.endTime)
    }
    
    @Test
    fun `should resume time entry successfully`() {
        // Given
        val timeEntry = createRunningTimeEntry()
        val pauseTime = LocalDateTime.now().plusHours(1)
        val resumeTime = pauseTime.plusMinutes(30)
        timeEntry.pause(pauseTime)
        
        // When
        timeEntry.resume(resumeTime)
        
        // Then
        assertFalse(timeEntry.isPaused())
        assertTrue(timeEntry.isRunning())
        assertEquals(resumeTime, timeEntry.startTime) // startTime should be updated to resume time
    }
    
    @Test
    fun `should throw exception when pausing already paused time entry`() {
        // Given
        val timeEntry = createRunningTimeEntry()
        timeEntry.pause(LocalDateTime.now().plusHours(1))
        
        // When & Then
        assertThrows<IllegalStateException> {
            timeEntry.pause(LocalDateTime.now().plusHours(2))
        }
    }
    
    @Test
    fun `should throw exception when resuming running time entry`() {
        // Given
        val timeEntry = createRunningTimeEntry()
        
        // When & Then
        assertThrows<IllegalStateException> {
            timeEntry.resume(LocalDateTime.now().plusHours(1))
        }
    }
    
    @Test
    fun `should update description successfully`() {
        // Given
        val timeEntry = createRunningTimeEntry()
        val newDescription = "Updated description"
        
        // When
        timeEntry.updateDescription(newDescription)
        
        // Then
        assertEquals(newDescription, timeEntry.description)
    }
    
    @Test
    fun `should throw exception when description is blank`() {
        // Given
        val timeEntry = createRunningTimeEntry()
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            timeEntry.updateDescription("")
        }
        
        assertThrows<IllegalArgumentException> {
            timeEntry.updateDescription("   ")
        }
    }
    
    @Test
    fun `should update project successfully`() {
        // Given
        val timeEntry = createRunningTimeEntry()
        val newProjectId = ProjectId(UUID.randomUUID())
        
        // When
        timeEntry.updateProject(newProjectId)
        
        // Then
        assertEquals(newProjectId, timeEntry.projectId)
    }
    
    @Test
    fun `should update task successfully`() {
        // Given
        val timeEntry = createRunningTimeEntry()
        val taskId = TaskId(UUID.randomUUID())
        
        // When
        timeEntry.updateTask(taskId)
        
        // Then
        assertEquals(taskId, timeEntry.taskId)
    }
    
    @Test
    fun `should add and remove tags successfully`() {
        // Given
        val timeEntry = createRunningTimeEntry()
        val tag1 = "development"
        val tag2 = "frontend"
        
        // When
        timeEntry.addTag(tag1)
        timeEntry.addTag(tag2)
        
        // Then
        assertTrue(timeEntry.tags.contains(tag1))
        assertTrue(timeEntry.tags.contains(tag2))
        assertEquals(2, timeEntry.tags.size)
        
        // When
        timeEntry.removeTag(tag1)
        
        // Then
        assertFalse(timeEntry.tags.contains(tag1))
        assertTrue(timeEntry.tags.contains(tag2))
        assertEquals(1, timeEntry.tags.size)
    }
    
    @Test
    fun `should update billable status successfully`() {
        // Given
        val timeEntry = createRunningTimeEntry()
        
        // When
        timeEntry.updateBillableStatus(true)
        
        // Then
        assertTrue(timeEntry.billable)
        
        // When
        timeEntry.updateBillableStatus(false)
        
        // Then
        assertFalse(timeEntry.billable)
    }
    
    @Test
    fun `should calculate total duration including sessions`() {
        // Given
        val timeEntry = createRunningTimeEntry()
        val pauseTime = timeEntry.startTime.plusHours(2)
        val resumeTime = pauseTime.plusMinutes(30)
        val endTime = resumeTime.plusHours(1)
        
        // When
        timeEntry.pause(pauseTime)
        timeEntry.resume(resumeTime)
        timeEntry.stop(endTime)
        
        // Then
        val expectedDuration = Duration.ofHours(3) // 2 hours + 1 hour (30 min break excluded)
        assertEquals(expectedDuration, timeEntry.calculateDuration())
    }
    
    @Test
    fun `should validate time entry constraints`() {
        // Given
        val timeEntryId = TimeEntryId(UUID.randomUUID())
        val userId = UserId(UUID.randomUUID())
        val projectId = ProjectId(UUID.randomUUID())
        val startTime = LocalDateTime.now()
        
        // When & Then - blank description
        assertThrows<IllegalArgumentException> {
            TimeEntry(
                id = timeEntryId,
                userId = userId,
                projectId = projectId,
                description = "",
                startTime = startTime
            )
        }
        
        // When & Then - future start time
        assertThrows<IllegalArgumentException> {
            TimeEntry(
                id = timeEntryId,
                userId = userId,
                projectId = projectId,
                description = "Valid description",
                startTime = LocalDateTime.now().plusDays(1)
            )
        }
    }
    
    @Test
    fun `should check if time entry is in time range`() {
        // Given
        val startTime = LocalDateTime.of(2024, 1, 15, 9, 0)
        val endTime = LocalDateTime.of(2024, 1, 15, 17, 0)
        val timeEntry = TimeEntry(
            id = TimeEntryId(UUID.randomUUID()),
            userId = UserId(UUID.randomUUID()),
            projectId = ProjectId(UUID.randomUUID()),
            description = "Test entry",
            startTime = startTime
        )
        timeEntry.stop(endTime)
        
        val timeRange1 = TimeRange(
            LocalDateTime.of(2024, 1, 15, 8, 0),
            LocalDateTime.of(2024, 1, 15, 18, 0)
        )
        val timeRange2 = TimeRange(
            LocalDateTime.of(2024, 1, 16, 8, 0),
            LocalDateTime.of(2024, 1, 16, 18, 0)
        )
        
        // When & Then
        assertTrue(timeEntry.isInTimeRange(timeRange1))
        assertFalse(timeEntry.isInTimeRange(timeRange2))
    }
    
    private fun createRunningTimeEntry(): TimeEntry {
        return TimeEntry(
            id = TimeEntryId(UUID.randomUUID()),
            userId = UserId(UUID.randomUUID()),
            projectId = ProjectId(UUID.randomUUID()),
            description = "Test time entry",
            startTime = LocalDateTime.now().minusHours(1)
        )
    }
    
    private fun createStoppedTimeEntry(): TimeEntry {
        val timeEntry = createRunningTimeEntry()
        timeEntry.stop(LocalDateTime.now())
        return timeEntry
    }
}