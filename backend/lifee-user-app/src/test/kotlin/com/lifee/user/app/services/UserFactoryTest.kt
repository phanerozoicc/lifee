package com.lifee.user.app.services

import com.lifee.user.domain.*
import com.lifee.user.domain.services.UserFactory
import com.lifee.user.domain.services.UserIdGenerator
import com.lifee.user.infrastructure.services.UserFactoryImpl
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

class UserFactoryTest {
    
    @Test
    fun `should create user with auto-generated ID`() {
        // Given
        val userIdGenerator = mock(UserIdGenerator::class.java)
        val expectedUserId = UserId.fromString("U00000123")
        `when`(userIdGenerator.generateNext()).thenReturn(expectedUserId)
        
        val userFactory = UserFactoryImpl(userIdGenerator)
        
        val email = Email.of("test@example.com")
        val password = Password.fromPlainText("Password123!")
        val firstName = "John"
        val lastName = "Doe"
        
        // When
        val user = userFactory.createUser(email, password, firstName, lastName)
        
        // Then
        assertNotNull(user)
        assertEquals(expectedUserId, user.getId())
        assertEquals(email, user.getEmail())
        assertEquals(firstName, user.getProfile().firstName)
        assertEquals(lastName, user.getProfile().lastName)
        
        verify(userIdGenerator, times(1)).generateNext()
    }
    
    @Test
    fun `should create user with specified ID`() {
        // Given
        val userIdGenerator = mock(UserIdGenerator::class.java)
        val userFactory = UserFactoryImpl(userIdGenerator)
        
        val specifiedUserId = UserId.fromString("U00000456")
        val email = Email.of("test2@example.com")
        val password = Password.fromPlainText("Password456!")
        val firstName = "Jane"
        val lastName = "Smith"
        
        // When
        val user = userFactory.createUserWithId(specifiedUserId, email, password, firstName, lastName)
        
        // Then
        assertNotNull(user)
        assertEquals(specifiedUserId, user.getId())
        assertEquals(email, user.getEmail())
        assertEquals(firstName, user.getProfile().firstName)
        assertEquals(lastName, user.getProfile().lastName)
        
        // Should not call generateNext when ID is specified
        verify(userIdGenerator, never()).generateNext()
    }
}