package com.lifee.time.domain.entity

import com.lifee.time.domain.valueobject.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class ProjectTest {

    @Test
    fun `should create project successfully`() {
        // Given
        val projectId = ProjectId(UUID.randomUUID())
        val ownerId = UserId(UUID.randomUUID())
        val name = "Test Project"
        val description = "A test project"
        val color = "#FF5733"
        
        // When
        val project = Project(
            id = projectId,
            name = name,
            description = description,
            ownerId = ownerId,
            color = color
        )
        
        // Then
        assertEquals(projectId, project.id)
        assertEquals(name, project.name)
        assertEquals(description, project.description)
        assertEquals(ownerId, project.ownerId)
        assertEquals(color, project.color)
        assertTrue(project.isActive)
        assertNull(project.clientName)
        assertNull(project.hourlyRate)
        assertNull(project.teamId)
        assertTrue(project.members.isEmpty())
        assertNotNull(project.createdAt)
        assertNotNull(project.updatedAt)
    }
    
    @Test
    fun `should throw exception when project name is blank`() {
        // Given
        val projectId = ProjectId(UUID.randomUUID())
        val ownerId = UserId(UUID.randomUUID())
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            Project(
                id = projectId,
                name = "",
                description = "Description",
                ownerId = ownerId
            )
        }
        
        assertThrows<IllegalArgumentException> {
            Project(
                id = projectId,
                name = "   ",
                description = "Description",
                ownerId = ownerId
            )
        }
    }
    
    @Test
    fun `should update project details successfully`() {
        // Given
        val project = createTestProject()
        val newName = "Updated Project"
        val newDescription = "Updated description"
        val newColor = "#33FF57"
        val newClientName = "New Client"
        val newHourlyRate = BigDecimal("75.00")
        
        // When
        project.updateDetails(
            name = newName,
            description = newDescription,
            color = newColor,
            clientName = newClientName,
            hourlyRate = newHourlyRate
        )
        
        // Then
        assertEquals(newName, project.name)
        assertEquals(newDescription, project.description)
        assertEquals(newColor, project.color)
        assertEquals(newClientName, project.clientName)
        assertEquals(newHourlyRate, project.hourlyRate)
    }
    
    @Test
    fun `should add member successfully`() {
        // Given
        val project = createTestProject()
        val userId = UserId(UUID.randomUUID())
        val role = ProjectRole.MEMBER
        
        // When
        project.addMember(userId, role)
        
        // Then
        assertEquals(1, project.members.size)
        val member = project.members.first()
        assertEquals(userId, member.userId)
        assertEquals(role, member.role)
        assertEquals(project.id, member.projectId)
        assertNotNull(member.joinedAt)
    }
    
    @Test
    fun `should throw exception when adding duplicate member`() {
        // Given
        val project = createTestProject()
        val userId = UserId(UUID.randomUUID())
        project.addMember(userId, ProjectRole.MEMBER)
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            project.addMember(userId, ProjectRole.ADMIN)
        }
    }
    
    @Test
    fun `should remove member successfully`() {
        // Given
        val project = createTestProject()
        val userId = UserId(UUID.randomUUID())
        project.addMember(userId, ProjectRole.MEMBER)
        
        // When
        project.removeMember(userId)
        
        // Then
        assertTrue(project.members.isEmpty())
    }
    
    @Test
    fun `should throw exception when removing non-existent member`() {
        // Given
        val project = createTestProject()
        val userId = UserId(UUID.randomUUID())
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            project.removeMember(userId)
        }
    }
    
    @Test
    fun `should throw exception when removing project owner`() {
        // Given
        val project = createTestProject()
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            project.removeMember(project.ownerId)
        }
    }
    
    @Test
    fun `should update member role successfully`() {
        // Given
        val project = createTestProject()
        val userId = UserId(UUID.randomUUID())
        project.addMember(userId, ProjectRole.MEMBER)
        
        // When
        project.updateMemberRole(userId, ProjectRole.ADMIN)
        
        // Then
        val member = project.members.first { it.userId == userId }
        assertEquals(ProjectRole.ADMIN, member.role)
    }
    
    @Test
    fun `should throw exception when updating role of non-existent member`() {
        // Given
        val project = createTestProject()
        val userId = UserId(UUID.randomUUID())
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            project.updateMemberRole(userId, ProjectRole.ADMIN)
        }
    }
    
    @Test
    fun `should archive project successfully`() {
        // Given
        val project = createTestProject()
        
        // When
        project.archive()
        
        // Then
        assertFalse(project.isActive)
        assertNotNull(project.archivedAt)
    }
    
    @Test
    fun `should throw exception when archiving already archived project`() {
        // Given
        val project = createTestProject()
        project.archive()
        
        // When & Then
        assertThrows<IllegalStateException> {
            project.archive()
        }
    }
    
    @Test
    fun `should restore project successfully`() {
        // Given
        val project = createTestProject()
        project.archive()
        
        // When
        project.restore()
        
        // Then
        assertTrue(project.isActive)
        assertNull(project.archivedAt)
    }
    
    @Test
    fun `should throw exception when restoring active project`() {
        // Given
        val project = createTestProject()
        
        // When & Then
        assertThrows<IllegalStateException> {
            project.restore()
        }
    }
    
    @Test
    fun `should assign to team successfully`() {
        // Given
        val project = createTestProject()
        val teamId = TeamId(UUID.randomUUID())
        
        // When
        project.assignToTeam(teamId)
        
        // Then
        assertEquals(teamId, project.teamId)
    }
    
    @Test
    fun `should remove from team successfully`() {
        // Given
        val project = createTestProject()
        val teamId = TeamId(UUID.randomUUID())
        project.assignToTeam(teamId)
        
        // When
        project.removeFromTeam()
        
        // Then
        assertNull(project.teamId)
    }
    
    @Test
    fun `should check if user has access to project`() {
        // Given
        val project = createTestProject()
        val memberId = UserId(UUID.randomUUID())
        val nonMemberId = UserId(UUID.randomUUID())
        project.addMember(memberId, ProjectRole.MEMBER)
        
        // When & Then
        assertTrue(project.hasAccess(project.ownerId)) // Owner has access
        assertTrue(project.hasAccess(memberId)) // Member has access
        assertFalse(project.hasAccess(nonMemberId)) // Non-member has no access
    }
    
    @Test
    fun `should check if user can manage project`() {
        // Given
        val project = createTestProject()
        val adminId = UserId(UUID.randomUUID())
        val memberId = UserId(UUID.randomUUID())
        project.addMember(adminId, ProjectRole.ADMIN)
        project.addMember(memberId, ProjectRole.MEMBER)
        
        // When & Then
        assertTrue(project.canManage(project.ownerId)) // Owner can manage
        assertTrue(project.canManage(adminId)) // Admin can manage
        assertFalse(project.canManage(memberId)) // Member cannot manage
    }
    
    @Test
    fun `should get member by user id`() {
        // Given
        val project = createTestProject()
        val userId = UserId(UUID.randomUUID())
        project.addMember(userId, ProjectRole.MEMBER)
        
        // When
        val member = project.getMember(userId)
        
        // Then
        assertNotNull(member)
        assertEquals(userId, member!!.userId)
        assertEquals(ProjectRole.MEMBER, member.role)
    }
    
    @Test
    fun `should return null when getting non-existent member`() {
        // Given
        val project = createTestProject()
        val userId = UserId(UUID.randomUUID())
        
        // When
        val member = project.getMember(userId)
        
        // Then
        assertNull(member)
    }
    
    @Test
    fun `should validate color format`() {
        // Given
        val project = createTestProject()
        
        // When & Then - Valid colors
        assertDoesNotThrow {
            project.updateDetails(color = "#FF5733")
            project.updateDetails(color = "#123ABC")
            project.updateDetails(color = "#000000")
            project.updateDetails(color = "#FFFFFF")
        }
        
        // When & Then - Invalid colors
        assertThrows<IllegalArgumentException> {
            project.updateDetails(color = "FF5733") // Missing #
        }
        
        assertThrows<IllegalArgumentException> {
            project.updateDetails(color = "#FF57") // Too short
        }
        
        assertThrows<IllegalArgumentException> {
            project.updateDetails(color = "#FF5733G") // Too long
        }
        
        assertThrows<IllegalArgumentException> {
            project.updateDetails(color = "#GG5733") // Invalid hex
        }
    }
    
    @Test
    fun `should validate hourly rate`() {
        // Given
        val project = createTestProject()
        
        // When & Then - Valid rates
        assertDoesNotThrow {
            project.updateDetails(hourlyRate = BigDecimal("0.00"))
            project.updateDetails(hourlyRate = BigDecimal("50.00"))
            project.updateDetails(hourlyRate = BigDecimal("999.99"))
        }
        
        // When & Then - Invalid rates
        assertThrows<IllegalArgumentException> {
            project.updateDetails(hourlyRate = BigDecimal("-1.00")) // Negative
        }
        
        assertThrows<IllegalArgumentException> {
            project.updateDetails(hourlyRate = BigDecimal("1000.00")) // Too high
        }
    }
    
    @Test
    fun `should duplicate project successfully`() {
        // Given
        val originalProject = createTestProject()
        originalProject.updateDetails(
            clientName = "Test Client",
            hourlyRate = BigDecimal("50.00")
        )
        originalProject.addMember(UserId(UUID.randomUUID()), ProjectRole.MEMBER)
        
        val newProjectId = ProjectId(UUID.randomUUID())
        val newOwnerId = UserId(UUID.randomUUID())
        val newName = "Duplicated Project"
        
        // When
        val duplicatedProject = originalProject.duplicate(
            newId = newProjectId,
            newOwnerId = newOwnerId,
            newName = newName
        )
        
        // Then
        assertEquals(newProjectId, duplicatedProject.id)
        assertEquals(newOwnerId, duplicatedProject.ownerId)
        assertEquals(newName, duplicatedProject.name)
        assertEquals(originalProject.description, duplicatedProject.description)
        assertEquals(originalProject.color, duplicatedProject.color)
        assertEquals(originalProject.clientName, duplicatedProject.clientName)
        assertEquals(originalProject.hourlyRate, duplicatedProject.hourlyRate)
        assertTrue(duplicatedProject.isActive)
        assertTrue(duplicatedProject.members.isEmpty()) // Members are not duplicated
        assertNull(duplicatedProject.teamId) // Team assignment is not duplicated
    }
    
    private fun createTestProject(): Project {
        return Project(
            id = ProjectId(UUID.randomUUID()),
            name = "Test Project",
            description = "A test project",
            ownerId = UserId(UUID.randomUUID()),
            color = "#FF5733"
        )
    }
}