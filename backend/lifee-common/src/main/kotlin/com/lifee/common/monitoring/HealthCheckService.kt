package com.lifee.common.monitoring

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.boot.actuator.health.Health
import org.springframework.boot.actuator.health.HealthIndicator
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import javax.sql.DataSource

/**
 * 健康检查服务
 * 提供系统各组件的健康状态检查
 */
@Service
class HealthCheckService(
    private val dataSource: DataSource,
    private val redisTemplate: RedisTemplate<String, Any>
) : HealthIndicator {
    
    private val logger = LoggerFactory.getLogger(HealthCheckService::class.java)
    
    companion object {
        private const val HEALTH_CHECK_KEY = "health:check"
        private const val DB_TIMEOUT_MS = 5000L
        private const val REDIS_TIMEOUT_MS = 3000L
    }
    
    /**
     * 系统整体健康检查
     */
    override fun health(): Health {
        return try {
            val healthStatus = performHealthCheck()
            
            if (healthStatus.isHealthy) {
                Health.up()
                    .withDetails(healthStatus.details)
                    .build()
            } else {
                Health.down()
                    .withDetails(healthStatus.details)
                    .build()
            }
        } catch (e: Exception) {
            logger.error("健康检查执行失败", e)
            Health.down()
                .withException(e)
                .build()
        }
    }
    
    /**
     * 执行详细的健康检查
     */
    suspend fun performHealthCheck(): SystemHealthStatus {
        logger.debug("开始执行系统健康检查")
        
        val startTime = System.currentTimeMillis()
        val checks = mutableMapOf<String, ComponentHealth>()
        
        try {
            // 数据库健康检查
            checks["database"] = checkDatabaseHealth()
            
            // Redis健康检查
            checks["redis"] = checkRedisHealth()
            
            // 内存健康检查
            checks["memory"] = checkMemoryHealth()
            
            // 磁盘空间检查
            checks["disk"] = checkDiskHealth()
            
            // 线程池检查
            checks["threads"] = checkThreadPoolHealth()
            
            val endTime = System.currentTimeMillis()
            val isHealthy = checks.values.all { it.status == HealthStatus.UP }
            
            val healthStatus = SystemHealthStatus(
                isHealthy = isHealthy,
                overallStatus = if (isHealthy) HealthStatus.UP else HealthStatus.DOWN,
                checks = checks,
                checkDurationMs = endTime - startTime,
                timestamp = Instant.now()
            )
            
            logger.debug("系统健康检查完成: status={}, duration={}ms", 
                healthStatus.overallStatus, healthStatus.checkDurationMs)
            
            return healthStatus
            
        } catch (e: Exception) {
            logger.error("健康检查执行异常", e)
            return SystemHealthStatus(
                isHealthy = false,
                overallStatus = HealthStatus.DOWN,
                checks = checks,
                checkDurationMs = System.currentTimeMillis() - startTime,
                timestamp = Instant.now(),
                error = e.message
            )
        }
    }
    
    /**
     * 数据库健康检查
     */
    private suspend fun checkDatabaseHealth(): ComponentHealth {
        return try {
            withContext(Dispatchers.IO) {
                val startTime = System.currentTimeMillis()
                
                dataSource.connection.use { connection ->
                    val isValid = connection.isValid((DB_TIMEOUT_MS / 1000).toInt())
                    val responseTime = System.currentTimeMillis() - startTime
                    
                    if (isValid) {
                        ComponentHealth(
                            status = HealthStatus.UP,
                            message = "数据库连接正常",
                            responseTimeMs = responseTime,
                            details = mapOf(
                                "url" to connection.metaData.url,
                                "driver" to connection.metaData.driverName,
                                "version" to connection.metaData.databaseProductVersion
                            )
                        )
                    } else {
                        ComponentHealth(
                            status = HealthStatus.DOWN,
                            message = "数据库连接无效",
                            responseTimeMs = responseTime
                        )
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("数据库健康检查失败", e)
            ComponentHealth(
                status = HealthStatus.DOWN,
                message = "数据库连接失败: ${e.message}",
                error = e.message
            )
        }
    }
    
    /**
     * Redis健康检查
     */
    private suspend fun checkRedisHealth(): ComponentHealth {
        return try {
            withContext(Dispatchers.IO) {
                val startTime = System.currentTimeMillis()
                
                // 执行ping命令
                val pong = redisTemplate.execute { connection ->
                    connection.ping()
                }
                
                val responseTime = System.currentTimeMillis() - startTime
                
                if ("PONG" == pong) {
                    // 测试读写操作
                    val testKey = "$HEALTH_CHECK_KEY:${System.currentTimeMillis()}"
                    val testValue = "health_check"
                    
                    redisTemplate.opsForValue().set(testKey, testValue)
                    val retrievedValue = redisTemplate.opsForValue().get(testKey)
                    redisTemplate.delete(testKey)
                    
                    if (testValue == retrievedValue) {
                        ComponentHealth(
                            status = HealthStatus.UP,
                            message = "Redis连接正常",
                            responseTimeMs = responseTime,
                            details = mapOf(
                                "ping_response" to pong,
                                "read_write_test" to "成功"
                            )
                        )
                    } else {
                        ComponentHealth(
                            status = HealthStatus.DOWN,
                            message = "Redis读写测试失败",
                            responseTimeMs = responseTime
                        )
                    }
                } else {
                    ComponentHealth(
                        status = HealthStatus.DOWN,
                        message = "Redis ping失败: $pong",
                        responseTimeMs = responseTime
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("Redis健康检查失败", e)
            ComponentHealth(
                status = HealthStatus.DOWN,
                message = "Redis连接失败: ${e.message}",
                error = e.message
            )
        }
    }
    
    /**
     * 内存健康检查
     */
    private fun checkMemoryHealth(): ComponentHealth {
        return try {
            val runtime = Runtime.getRuntime()
            val maxMemory = runtime.maxMemory()
            val totalMemory = runtime.totalMemory()
            val freeMemory = runtime.freeMemory()
            val usedMemory = totalMemory - freeMemory
            val memoryUsagePercent = (usedMemory.toDouble() / maxMemory * 100).toInt()
            
            val status = when {
                memoryUsagePercent < 80 -> HealthStatus.UP
                memoryUsagePercent < 90 -> HealthStatus.WARNING
                else -> HealthStatus.DOWN
            }
            
            ComponentHealth(
                status = status,
                message = "内存使用率: ${memoryUsagePercent}%",
                details = mapOf(
                    "max_memory_mb" to maxMemory / 1024 / 1024,
                    "total_memory_mb" to totalMemory / 1024 / 1024,
                    "used_memory_mb" to usedMemory / 1024 / 1024,
                    "free_memory_mb" to freeMemory / 1024 / 1024,
                    "usage_percent" to memoryUsagePercent
                )
            )
        } catch (e: Exception) {
            logger.error("内存健康检查失败", e)
            ComponentHealth(
                status = HealthStatus.DOWN,
                message = "内存检查失败: ${e.message}",
                error = e.message
            )
        }
    }
    
    /**
     * 磁盘空间检查
     */
    private fun checkDiskHealth(): ComponentHealth {
        return try {
            val file = java.io.File(".")
            val totalSpace = file.totalSpace
            val freeSpace = file.freeSpace
            val usedSpace = totalSpace - freeSpace
            val diskUsagePercent = (usedSpace.toDouble() / totalSpace * 100).toInt()
            
            val status = when {
                diskUsagePercent < 80 -> HealthStatus.UP
                diskUsagePercent < 90 -> HealthStatus.WARNING
                else -> HealthStatus.DOWN
            }
            
            ComponentHealth(
                status = status,
                message = "磁盘使用率: ${diskUsagePercent}%",
                details = mapOf(
                    "total_space_gb" to totalSpace / 1024 / 1024 / 1024,
                    "free_space_gb" to freeSpace / 1024 / 1024 / 1024,
                    "used_space_gb" to usedSpace / 1024 / 1024 / 1024,
                    "usage_percent" to diskUsagePercent
                )
            )
        } catch (e: Exception) {
            logger.error("磁盘健康检查失败", e)
            ComponentHealth(
                status = HealthStatus.DOWN,
                message = "磁盘检查失败: ${e.message}",
                error = e.message
            )
        }
    }
    
    /**
     * 线程池健康检查
     */
    private fun checkThreadPoolHealth(): ComponentHealth {
        return try {
            val threadMXBean = java.lang.management.ManagementFactory.getThreadMXBean()
            val threadCount = threadMXBean.threadCount
            val peakThreadCount = threadMXBean.peakThreadCount
            val daemonThreadCount = threadMXBean.daemonThreadCount
            
            val status = when {
                threadCount < 200 -> HealthStatus.UP
                threadCount < 500 -> HealthStatus.WARNING
                else -> HealthStatus.DOWN
            }
            
            ComponentHealth(
                status = status,
                message = "线程数: $threadCount",
                details = mapOf(
                    "thread_count" to threadCount,
                    "peak_thread_count" to peakThreadCount,
                    "daemon_thread_count" to daemonThreadCount
                )
            )
        } catch (e: Exception) {
            logger.error("线程池健康检查失败", e)
            ComponentHealth(
                status = HealthStatus.DOWN,
                message = "线程池检查失败: ${e.message}",
                error = e.message
            )
        }
    }
    
    /**
     * 获取系统健康摘要
     */
    suspend fun getHealthSummary(): HealthSummary {
        val healthStatus = performHealthCheck()
        
        return HealthSummary(
            status = healthStatus.overallStatus,
            uptime = getSystemUptime(),
            version = getApplicationVersion(),
            environment = getEnvironment(),
            timestamp = Instant.now(),
            componentCount = healthStatus.checks.size,
            healthyComponents = healthStatus.checks.values.count { it.status == HealthStatus.UP },
            warningComponents = healthStatus.checks.values.count { it.status == HealthStatus.WARNING },
            downComponents = healthStatus.checks.values.count { it.status == HealthStatus.DOWN }
        )
    }
    
    /**
     * 获取系统运行时间
     */
    private fun getSystemUptime(): Long {
        val runtimeMXBean = java.lang.management.ManagementFactory.getRuntimeMXBean()
        return runtimeMXBean.uptime
    }
    
    /**
     * 获取应用版本
     */
    private fun getApplicationVersion(): String {
        return this::class.java.`package`?.implementationVersion ?: "unknown"
    }
    
    /**
     * 获取运行环境
     */
    private fun getEnvironment(): String {
        return System.getProperty("spring.profiles.active") ?: "default"
    }
}

/**
 * 健康状态枚举
 */
enum class HealthStatus {
    UP,      // 健康
    WARNING, // 警告
    DOWN     // 不健康
}

/**
 * 组件健康状态
 */
data class ComponentHealth(
    val status: HealthStatus,
    val message: String,
    val responseTimeMs: Long = 0,
    val details: Map<String, Any> = emptyMap(),
    val error: String? = null
)

/**
 * 系统健康状态
 */
data class SystemHealthStatus(
    val isHealthy: Boolean,
    val overallStatus: HealthStatus,
    val checks: Map<String, ComponentHealth>,
    val checkDurationMs: Long,
    val timestamp: Instant,
    val error: String? = null
) {
    val details: Map<String, Any>
        get() = mapOf(
            "overall_status" to overallStatus.name,
            "check_duration_ms" to checkDurationMs,
            "timestamp" to timestamp.toString(),
            "components" to checks.mapValues { (_, health) ->
                mapOf(
                    "status" to health.status.name,
                    "message" to health.message,
                    "response_time_ms" to health.responseTimeMs,
                    "details" to health.details
                )
            }
        ) + if (error != null) mapOf("error" to error) else emptyMap()
}

/**
 * 健康摘要
 */
data class HealthSummary(
    val status: HealthStatus,
    val uptime: Long,
    val version: String,
    val environment: String,
    val timestamp: Instant,
    val componentCount: Int,
    val healthyComponents: Int,
    val warningComponents: Int,
    val downComponents: Int
)