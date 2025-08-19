package com.lifee.user.infrastructure.repositories

import com.lifee.common.eventsourcing.ConcurrencyException
import com.lifee.common.eventsourcing.EventStore
import com.lifee.common.eventsourcing.SnapshotService
import com.lifee.common.domain.AggregateSnapshot
import com.lifee.user.domain.*
import com.lifee.user.domain.events.UserRegisteredEvent
import com.lifee.user.infrastructure.entities.UserEntity
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

/**
 * UserRepositoryImpl测试
 */
class UserRepositoryImplTest : BehaviorSpec({
    
    given("用户仓储实现") {
        val jpaUserRepository = mockk<JpaUserRepository>()
        val eventStore = mockk<EventStore>()
        val snapshotService = mockk<SnapshotService>()
        
        val repository = UserRepositoryImpl(
            jpaUserRepository = jpaUserRepository,
            eventStore = eventStore,
            snapshotService = snapshotService
        )
        
        beforeEach {
            clearAllMocks()
        }
        
        `when`("保存新用户") {
            val userId = UserId.generate()
            val email = Email("test@example.com")
            val password = Password("Password123!")
            val user = User.create(
                id = userId,
                email = email,
                password = password,
                firstName = "John",
                lastName = "Doe"
            )
            
            val userEntity = mockk<UserEntity>()
            val savedEntity = mockk<UserEntity>()
            val savedUser = mockk<User>()
            
            // Mock设置
            mockkObject(UserEntity.Companion)
            every { UserEntity.fromDomain(user) } returns userEntity
            every { jpaUserRepository.save(userEntity) } returns savedEntity
            every { savedEntity.toDomain() } returns savedUser
            every { user.hasUncommittedEvents() } returns true
            every { user.getUncommittedEvents() } returns listOf(
                UserRegisteredEvent(
                    userId = userId,
                    email = email,
                    firstName = "John",
                    lastName = "Doe",
                    registeredAt = Instant.now()
                )
            )
            every { user.getVersion() } returns 1L
            coEvery { eventStore.saveEvents(userId.value, any(), 0L) } just Runs
            every { user.markEventsAsCommitted() } just Runs
            coEvery { snapshotService.createSnapshotIfNeeded(userId.value, User::class) } just Runs
            
            then("应该成功保存用户") {
                val result = runBlocking {
                    repository.save(user)
                }
                
                result shouldBe savedUser
                
                // 验证调用
                verify { UserEntity.fromDomain(user) }
                verify { jpaUserRepository.save(userEntity) }
                verify { savedEntity.toDomain() }
                coVerify { eventStore.saveEvents(userId.value, any(), 0L) }
                verify { user.markEventsAsCommitted() }
                coVerify { snapshotService.createSnapshotIfNeeded(userId.value, User::class) }
            }
        }
        
        `when`("保存用户时发生并发冲突") {
            val userId = UserId.generate()
            val email = Email("test@example.com")
            val password = Password("Password123!")
            val user = User.create(
                id = userId,
                email = email,
                password = password,
                firstName = "John",
                lastName = "Doe"
            )
            
            val userEntity = mockk<UserEntity>()
            val latestUser = mockk<User>()
            
            mockkObject(UserEntity.Companion)
            every { UserEntity.fromDomain(user) } returns userEntity
            every { user.hasUncommittedEvents() } returns true
            every { user.getUncommittedEvents() } returns listOf(
                UserRegisteredEvent(
                    userId = userId,
                    email = email,
                    firstName = "John",
                    lastName = "Doe",
                    registeredAt = Instant.now()
                )
            )
            every { user.getVersion() } returns 1L
            every { user.getId() } returns userId
            
            // 第一次保存失败，第二次成功
            coEvery { eventStore.saveEvents(userId.value, any(), 0L) } throws ConcurrencyException(
                aggregateId = userId.value,
                expectedVersion = 0L,
                actualVersion = 1L
            ) andThen just(Runs)
            
            // Mock findById返回最新用户
            val jpaOptional = mockk<Optional<UserEntity>>()
            val latestEntity = mockk<UserEntity>()
            every { jpaUserRepository.findById(userId.value) } returns jpaOptional
            every { jpaOptional.orElse(null) } returns latestEntity
            every { latestEntity?.toDomain() } returns latestUser
            
            // Mock第二次保存成功
            val savedEntity = mockk<UserEntity>()
            val savedUser = mockk<User>()
            every { UserEntity.fromDomain(latestUser) } returns userEntity
            every { jpaUserRepository.save(userEntity) } returns savedEntity
            every { savedEntity.toDomain() } returns savedUser
            every { latestUser.hasUncommittedEvents() } returns false
            
            then("应该重试并成功保存") {
                val result = runBlocking {
                    repository.save(user)
                }
                
                result shouldBe savedUser
                
                // 验证重试逻辑
                coVerify(exactly = 2) { eventStore.saveEvents(userId.value, any(), 0L) }
                verify { jpaUserRepository.findById(userId.value) }
            }
        }
        
        `when`("保存用户时达到最大重试次数") {
            val userId = UserId.generate()
            val email = Email("test@example.com")
            val password = Password("Password123!")
            val user = User.create(
                id = userId,
                email = email,
                password = password,
                firstName = "John",
                lastName = "Doe"
            )
            
            val userEntity = mockk<UserEntity>()
            
            mockkObject(UserEntity.Companion)
            every { UserEntity.fromDomain(user) } returns userEntity
            every { user.hasUncommittedEvents() } returns true
            every { user.getUncommittedEvents() } returns listOf(
                UserRegisteredEvent(
                    userId = userId,
                    email = email,
                    firstName = "John",
                    lastName = "Doe",
                    registeredAt = Instant.now()
                )
            )
            every { user.getVersion() } returns 1L
            every { user.getId() } returns userId
            
            // 总是抛出并发异常
            coEvery { eventStore.saveEvents(userId.value, any(), 0L) } throws ConcurrencyException(
                aggregateId = userId.value,
                expectedVersion = 0L,
                actualVersion = 1L
            )
            
            // Mock findById返回用户
            val jpaOptional = mockk<Optional<UserEntity>>()
            val latestEntity = mockk<UserEntity>()
            val latestUser = mockk<User>()
            every { jpaUserRepository.findById(userId.value) } returns jpaOptional
            every { jpaOptional.orElse(null) } returns latestEntity
            every { latestEntity?.toDomain() } returns latestUser
            
            then("应该抛出并发异常") {
                shouldThrow<ConcurrencyException> {
                    runBlocking {
                        repository.save(user)
                    }
                }
                
                // 验证重试了3次
                coVerify(exactly = 3) { eventStore.saveEvents(userId.value, any(), 0L) }
            }
        }
        
        `when`("通过ID查找用户（从快照恢复）") {
            val userId = UserId.generate()
            val snapshot = mockk<AggregateSnapshot<Map<String, Any>>>()
            val events = listOf<com.lifee.common.domain.DomainEvent>()
            
            coEvery { eventStore.getLatestSnapshot(userId.value) } returns snapshot
            every { snapshot.version } returns 5L
            coEvery { eventStore.getEvents(userId.value) } returns events
            
            then("应该从快照恢复用户") {
                val result = runBlocking {
                    repository.findById(userId)
                }
                
                result shouldNotBe null
                
                // 验证调用
                coVerify { eventStore.getLatestSnapshot(userId.value) }
                coVerify { eventStore.getEvents(userId.value) }
            }
        }
        
        `when`("通过ID查找用户（从JPA加载）") {
            val userId = UserId.generate()
            val userEntity = mockk<UserEntity>()
            val user = mockk<User>()
            val jpaOptional = mockk<Optional<UserEntity>>()
            
            coEvery { eventStore.getLatestSnapshot(userId.value) } returns null
            every { jpaUserRepository.findById(userId.value) } returns jpaOptional
            every { jpaOptional.orElse(null) } returns userEntity
            every { userEntity?.toDomain() } returns user
            
            then("应该从JPA加载用户") {
                val result = runBlocking {
                    repository.findById(userId)
                }
                
                result shouldBe user
                
                // 验证调用
                coVerify { eventStore.getLatestSnapshot(userId.value) }
                verify { jpaUserRepository.findById(userId.value) }
            }
        }
        
        `when`("通过邮箱查找用户") {
            val email = Email("test@example.com")
            val userEntity = mockk<UserEntity>()
            val user = mockk<User>()
            
            every { jpaUserRepository.findByEmail(email.value) } returns userEntity
            every { userEntity.toDomain() } returns user
            
            then("应该返回用户") {
                val result = runBlocking {
                    repository.findByEmail(email)
                }
                
                result shouldBe user
                
                verify { jpaUserRepository.findByEmail(email.value) }
            }
        }
        
        `when`("检查邮箱是否存在") {
            val email = Email("test@example.com")
            
            every { jpaUserRepository.existsByEmail(email.value) } returns true
            
            then("应该返回true") {
                val result = runBlocking {
                    repository.existsByEmail(email)
                }
                
                result shouldBe true
                
                verify { jpaUserRepository.existsByEmail(email.value) }
            }
        }
        
        `when`("按状态查找用户") {
            val status = UserStatus.ACTIVE
            val limit = 10
            val offset = 0
            val userEntities = listOf(mockk<UserEntity>(), mockk<UserEntity>())
            val users = listOf(mockk<User>(), mockk<User>())
            
            val pageable = PageRequest.of(offset / limit, limit)
            every { jpaUserRepository.findByStatusOrderByCreatedAtDesc(status, pageable) } returns userEntities
            every { userEntities[0].toDomain() } returns users[0]
            every { userEntities[1].toDomain() } returns users[1]
            
            then("应该返回用户列表") {
                val result = runBlocking {
                    repository.findByStatus(status, limit, offset)
                }
                
                result shouldBe users
                
                verify { jpaUserRepository.findByStatusOrderByCreatedAtDesc(status, pageable) }
            }
        }
        
        `when`("查找待激活用户") {
            val olderThanHours = 24
            val cutoffTime = LocalDateTime.now().minusHours(olderThanHours.toLong())
            val userEntities = listOf(mockk<UserEntity>())
            val users = listOf(mockk<User>())
            
            every { jpaUserRepository.findPendingActivationUsers(any()) } returns userEntities
            every { userEntities[0].toDomain() } returns users[0]
            
            then("应该返回待激活用户列表") {
                val result = runBlocking {
                    repository.findPendingActivationUsers(olderThanHours)
                }
                
                result shouldBe users
                
                verify { jpaUserRepository.findPendingActivationUsers(any()) }
            }
        }
        
        `when`("按状态统计用户数量") {
            val status = UserStatus.ACTIVE
            val count = 100L
            
            every { jpaUserRepository.countByStatus(status) } returns count
            
            then("应该返回用户数量") {
                val result = runBlocking {
                    repository.countByStatus(status)
                }
                
                result shouldBe count
                
                verify { jpaUserRepository.countByStatus(status) }
            }
        }
        
        `when`("删除用户") {
            val userId = UserId.generate()
            val user = mockk<User>()
            
            every { user.getId() } returns userId
            every { jpaUserRepository.deleteById(userId.value) } just Runs
            
            then("应该成功删除") {
                runBlocking {
                    repository.delete(user)
                }
                
                verify { jpaUserRepository.deleteById(userId.value) }
            }
        }
        
        `when`("通过ID删除用户") {
            val userId = UserId.generate()
            
            every { jpaUserRepository.deleteById(userId.value) } just Runs
            
            then("应该成功删除") {
                runBlocking {
                    repository.deleteById(userId)
                }
                
                verify { jpaUserRepository.deleteById(userId.value) }
            }
        }
    }
})