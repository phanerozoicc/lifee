package com.lifee.user.infrastructure.services

import com.lifee.user.domain.services.UserIdGenerator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * 用户ID生成器集成测试
 * 验证Redis ID生成器在Spring容器中的工作情况
 */
@SpringBootTest(classes = [com.lifee.user.app.LifeeUserApplication::class])
@ActiveProfiles("test")
class UserIdGeneratorIntegrationTest {
    
    @Autowired
    private lateinit var userIdGenerator: UserIdGenerator
    
    @Test
    fun `应该能够注入UserIdGenerator实例`() {
        assertNotNull(userIdGenerator)
        assertTrue(userIdGenerator is RedisUserIdGenerator)
    }
    
    @Test
    fun `应该能够生成用户ID`() {
        // When
        val userId = userIdGenerator.generateNext()
        
        // Then
        assertNotNull(userId)
        assertTrue(userId.value.startsWith("U"))
        assertEquals(9, userId.value.length)
        assertTrue(userId.value.matches(Regex("^U[0-9]{8}$")))
    }
    
    @Test
    fun `应该能够获取当前序列号`() {
        // When
        val currentSequence = userIdGenerator.getCurrentSequence()
        
        // Then
        assertTrue(currentSequence >= 0)
    }
    
    @Test
    fun `生成的ID应该是递增的`() {
        // When
        val userId1 = userIdGenerator.generateNext()
        val userId2 = userIdGenerator.generateNext()
        
        // Then
        val sequence1 = userId1.value.substring(1).toLong()
        val sequence2 = userId2.value.substring(1).toLong()
        assertTrue(sequence2 > sequence1, "第二个ID应该大于第一个ID")
    }
}