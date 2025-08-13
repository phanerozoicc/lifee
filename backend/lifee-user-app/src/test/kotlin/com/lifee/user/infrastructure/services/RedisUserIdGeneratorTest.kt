package com.lifee.user.infrastructure.services

import com.lifee.user.domain.UserId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.redisson.api.RAtomicLong
import org.redisson.api.RedissonClient

/**
 * Redis用户ID生成器测试
 */
class RedisUserIdGeneratorTest {
    
    private val mockRedissonClient = mock<RedissonClient>()
    private val mockAtomicLong = mock<RAtomicLong>()
    private val generator = RedisUserIdGenerator(mockRedissonClient)
    
    @Test
    fun `应该生成正确格式的用户ID`() {
        // Given
        whenever(mockRedissonClient.getAtomicLong("lifee:user:id:counter"))
            .thenReturn(mockAtomicLong)
        whenever(mockAtomicLong.incrementAndGet()).thenReturn(1L)
        
        // When
        val userId = generator.generateNext()
        
        // Then
        assertEquals("U00000001", userId.value)
    }
    
    @Test
    fun `应该生成递增的用户ID`() {
        // Given
        whenever(mockRedissonClient.getAtomicLong("lifee:user:id:counter"))
            .thenReturn(mockAtomicLong)
        whenever(mockAtomicLong.incrementAndGet())
            .thenReturn(1L)
            .thenReturn(2L)
            .thenReturn(3L)
        
        // When
        val userId1 = generator.generateNext()
        val userId2 = generator.generateNext()
        val userId3 = generator.generateNext()
        
        // Then
        assertEquals("U00000001", userId1.value)
        assertEquals("U00000002", userId2.value)
        assertEquals("U00000003", userId3.value)
    }
    
    @Test
    fun `应该处理大数字`() {
        // Given
        whenever(mockRedissonClient.getAtomicLong("lifee:user:id:counter"))
            .thenReturn(mockAtomicLong)
        whenever(mockAtomicLong.incrementAndGet()).thenReturn(99999999L)
        
        // When
        val userId = generator.generateNext()
        
        // Then
        assertEquals("U99999999", userId.value)
    }
    
    @Test
    fun `应该返回当前计数器值`() {
        // Given
        whenever(mockRedissonClient.getAtomicLong("lifee:user:id:counter"))
            .thenReturn(mockAtomicLong)
        whenever(mockAtomicLong.get()).thenReturn(42L)
        
        // When
        val currentSequence = generator.getCurrentSequence()
        
        // Then
        assertEquals(42L, currentSequence)
    }
    
    @Test
    fun `异常时应该返回0`() {
        // Given
        whenever(mockRedissonClient.getAtomicLong("lifee:user:id:counter"))
            .thenReturn(mockAtomicLong)
        whenever(mockAtomicLong.get()).thenThrow(RuntimeException("Redis连接失败"))
        
        // When
        val currentSequence = generator.getCurrentSequence()
        
        // Then
        assertEquals(0L, currentSequence)
    }
    
    @Test
    fun `验证ID格式正确性`() {
        // Given
        whenever(mockRedissonClient.getAtomicLong("lifee:user:id:counter"))
            .thenReturn(mockAtomicLong)
        whenever(mockAtomicLong.incrementAndGet()).thenReturn(123L)
        
        // When
        val userId = generator.generateNext()
        
        // Then
        assertTrue(userId.value.matches(Regex("^U[0-9]{8}$")))
        assertEquals("U00000123", userId.value)
    }
}