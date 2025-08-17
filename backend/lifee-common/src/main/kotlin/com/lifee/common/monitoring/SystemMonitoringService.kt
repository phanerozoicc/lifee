package com.lifee.common.monitoring

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.lang.management.ManagementFactory
import java.time.Instant
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * 系统监控服务
 * 负责收集、存储和分析系统性能指标
 */
@Service
class SystemMonitoringService {
    
    private val logger = LoggerFactory.getLogger(SystemMonitoringService::class.java)
    
    // 监控数据存储
    private val metricsHistory = ConcurrentHashMap<String, MutableList<MetricPoint>>()
    private val alertRules = ConcurrentHashMap<String, AlertRule>()
    private val activeAlerts = ConcurrentHashMap<String, Alert>()
    
    // 计数器
    private val requestCounter = AtomicLong(0)
    private val errorCounter = AtomicLong(0)
    private val responseTimeSum = AtomicLong(0)
    private val responseTimeCount = AtomicLong(0)
    
    // 监控任务
    private var monitoringJob: Job? = null
    private val monitoringScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    companion object {
        private const val MAX_HISTORY_SIZE = 1000
        private const val MONITORING_INTERVAL_MS = 30000L // 30秒
        private const val ALERT_CHECK_INTERVAL_MS = 60000L // 1分钟
    }
    
    @PostConstruct
    fun initialize() {
        logger.info("初始化系统监控服务")
        
        // 设置默认告警规则
        setupDefaultAlertRules()
        
        // 启动监控任务
        startMonitoring()
        
        logger.info("系统监控服务初始化完成")
    }
    
    @PreDestroy
    fun shutdown() {
        logger.info("关闭系统监控服务")
        monitoringJob?.cancel()
        monitoringScope.cancel()
    }
    
    /**
     * 启动监控任务
     */
    private fun startMonitoring() {
        monitoringJob = monitoringScope.launch {
            while (isActive) {
                try {
                    collectSystemMetrics()
                    delay(MONITORING_INTERVAL_MS)
                } catch (e: Exception) {
                    logger.error("监控数据收集失败", e)
                    delay(MONITORING_INTERVAL_MS)
                }
            }
        }
    }
    
    /**
     * 收集系统指标
     */
    private suspend fun collectSystemMetrics() {
        val timestamp = Instant.now()
        
        // CPU使用率
        val cpuUsage = getCpuUsage()
        recordMetric("cpu_usage", cpuUsage, timestamp)
        
        // 内存使用率
        val memoryUsage = getMemoryUsage()
        recordMetric("memory_usage", memoryUsage, timestamp)
        
        // 堆内存使用率
        val heapUsage = getHeapUsage()
        recordMetric("heap_usage", heapUsage, timestamp)
        
        // 线程数
        val threadCount = getThreadCount().toDouble()
        recordMetric("thread_count", threadCount, timestamp)
        
        // GC次数和时间
        val gcMetrics = getGcMetrics()
        recordMetric("gc_count", gcMetrics.totalCollections.toDouble(), timestamp)
        recordMetric("gc_time", gcMetrics.totalTime.toDouble(), timestamp)
        
        // 请求指标
        val requestMetrics = getRequestMetrics()
        recordMetric("request_count", requestMetrics.totalRequests.toDouble(), timestamp)
        recordMetric("error_count", requestMetrics.totalErrors.toDouble(), timestamp)
        recordMetric("avg_response_time", requestMetrics.avgResponseTime, timestamp)
        
        // 检查告警
        checkAlerts()
    }
    
    /**
     * 获取CPU使用率
     */
    private fun getCpuUsage(): Double {
        val osBean = ManagementFactory.getOperatingSystemMXBean()
        return if (osBean is com.sun.management.OperatingSystemMXBean) {
            osBean.processCpuLoad * 100
        } else {
            0.0
        }
    }
    
    /**
     * 获取内存使用率
     */
    private fun getMemoryUsage(): Double {
        val memoryBean = ManagementFactory.getMemoryMXBean()
        val heapMemory = memoryBean.heapMemoryUsage
        return (heapMemory.used.toDouble() / heapMemory.max * 100)
    }
    
    /**
     * 获取堆内存使用率
     */
    private fun getHeapUsage(): Double {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        return (usedMemory.toDouble() / maxMemory * 100)
    }
    
    /**
     * 获取线程数
     */
    private fun getThreadCount(): Int {
        val threadBean = ManagementFactory.getThreadMXBean()
        return threadBean.threadCount
    }
    
    /**
     * 获取GC指标
     */
    private fun getGcMetrics(): GcMetrics {
        val gcBeans = ManagementFactory.getGarbageCollectorMXBeans()
        var totalCollections = 0L
        var totalTime = 0L
        
        gcBeans.forEach { gcBean ->
            totalCollections += gcBean.collectionCount
            totalTime += gcBean.collectionTime
        }
        
        return GcMetrics(totalCollections, totalTime)
    }
    
    /**
     * 获取请求指标
     */
    private fun getRequestMetrics(): RequestMetrics {
        val totalRequests = requestCounter.get()
        val totalErrors = errorCounter.get()
        val responseTimeCount = responseTimeCount.get()
        val avgResponseTime = if (responseTimeCount > 0) {
            responseTimeSum.get().toDouble() / responseTimeCount
        } else {
            0.0
        }
        
        return RequestMetrics(totalRequests, totalErrors, avgResponseTime)
    }
    
    /**
     * 记录指标
     */
    private fun recordMetric(name: String, value: Double, timestamp: Instant) {
        val metricPoint = MetricPoint(value, timestamp)
        
        metricsHistory.computeIfAbsent(name) { mutableListOf() }.apply {
            add(metricPoint)
            // 保持历史记录大小限制
            if (size > MAX_HISTORY_SIZE) {
                removeAt(0)
            }
        }
    }
    
    /**
     * 记录请求
     */
    fun recordRequest(responseTimeMs: Long, isError: Boolean = false) {
        requestCounter.incrementAndGet()
        responseTimeSum.addAndGet(responseTimeMs)
        responseTimeCount.incrementAndGet()
        
        if (isError) {
            errorCounter.incrementAndGet()
        }
    }
    
    /**
     * 获取指标历史
     */
    fun getMetricHistory(metricName: String, limit: Int = 100): List<MetricPoint> {
        return metricsHistory[metricName]?.takeLast(limit) ?: emptyList()
    }
    
    /**
     * 获取最新指标值
     */
    fun getLatestMetric(metricName: String): MetricPoint? {
        return metricsHistory[metricName]?.lastOrNull()
    }
    
    /**
     * 获取所有指标名称
     */
    fun getAllMetricNames(): Set<String> {
        return metricsHistory.keys.toSet()
    }
    
    /**
     * 获取系统概览
     */
    fun getSystemOverview(): SystemOverview {
        val now = Instant.now()
        
        return SystemOverview(
            timestamp = now,
            cpuUsage = getLatestMetric("cpu_usage")?.value ?: 0.0,
            memoryUsage = getLatestMetric("memory_usage")?.value ?: 0.0,
            heapUsage = getLatestMetric("heap_usage")?.value ?: 0.0,
            threadCount = getLatestMetric("thread_count")?.value?.toInt() ?: 0,
            totalRequests = requestCounter.get(),
            totalErrors = errorCounter.get(),
            avgResponseTime = if (responseTimeCount.get() > 0) {
                responseTimeSum.get().toDouble() / responseTimeCount.get()
            } else 0.0,
            activeAlerts = activeAlerts.values.toList()
        )
    }
    
    /**
     * 设置默认告警规则
     */
    private fun setupDefaultAlertRules() {
        // CPU使用率告警
        addAlertRule(AlertRule(
            id = "cpu_high",
            metricName = "cpu_usage",
            threshold = 80.0,
            operator = AlertOperator.GREATER_THAN,
            severity = AlertSeverity.WARNING,
            message = "CPU使用率过高"
        ))
        
        addAlertRule(AlertRule(
            id = "cpu_critical",
            metricName = "cpu_usage",
            threshold = 90.0,
            operator = AlertOperator.GREATER_THAN,
            severity = AlertSeverity.CRITICAL,
            message = "CPU使用率严重过高"
        ))
        
        // 内存使用率告警
        addAlertRule(AlertRule(
            id = "memory_high",
            metricName = "memory_usage",
            threshold = 85.0,
            operator = AlertOperator.GREATER_THAN,
            severity = AlertSeverity.WARNING,
            message = "内存使用率过高"
        ))
        
        addAlertRule(AlertRule(
            id = "memory_critical",
            metricName = "memory_usage",
            threshold = 95.0,
            operator = AlertOperator.GREATER_THAN,
            severity = AlertSeverity.CRITICAL,
            message = "内存使用率严重过高"
        ))
        
        // 错误率告警
        addAlertRule(AlertRule(
            id = "error_rate_high",
            metricName = "error_rate",
            threshold = 5.0,
            operator = AlertOperator.GREATER_THAN,
            severity = AlertSeverity.WARNING,
            message = "错误率过高"
        ))
    }
    
    /**
     * 添加告警规则
     */
    fun addAlertRule(rule: AlertRule) {
        alertRules[rule.id] = rule
        logger.info("添加告警规则: {}", rule)
    }
    
    /**
     * 移除告警规则
     */
    fun removeAlertRule(ruleId: String) {
        alertRules.remove(ruleId)
        activeAlerts.remove(ruleId)
        logger.info("移除告警规则: {}", ruleId)
    }
    
    /**
     * 检查告警
     */
    @Scheduled(fixedDelay = ALERT_CHECK_INTERVAL_MS)
    private fun checkAlerts() {
        alertRules.values.forEach { rule ->
            try {
                val latestMetric = getLatestMetric(rule.metricName)
                if (latestMetric != null) {
                    val shouldAlert = when (rule.operator) {
                        AlertOperator.GREATER_THAN -> latestMetric.value > rule.threshold
                        AlertOperator.LESS_THAN -> latestMetric.value < rule.threshold
                        AlertOperator.EQUALS -> latestMetric.value == rule.threshold
                    }
                    
                    if (shouldAlert) {
                        triggerAlert(rule, latestMetric.value)
                    } else {
                        resolveAlert(rule.id)
                    }
                }
            } catch (e: Exception) {
                logger.error("检查告警规则失败: {}", rule.id, e)
            }
        }
    }
    
    /**
     * 触发告警
     */
    private fun triggerAlert(rule: AlertRule, currentValue: Double) {
        val existingAlert = activeAlerts[rule.id]
        
        if (existingAlert == null) {
            val alert = Alert(
                id = rule.id,
                rule = rule,
                currentValue = currentValue,
                triggeredAt = Instant.now(),
                status = AlertStatus.ACTIVE
            )
            
            activeAlerts[rule.id] = alert
            logger.warn("触发告警: {} - {} (当前值: {})", rule.id, rule.message, currentValue)
            
            // TODO: 发送告警通知（邮件、短信、Webhook等）
            sendAlertNotification(alert)
        } else {
            // 更新现有告警的当前值
            existingAlert.currentValue = currentValue
            existingAlert.lastUpdated = Instant.now()
        }
    }
    
    /**
     * 解决告警
     */
    private fun resolveAlert(ruleId: String) {
        val alert = activeAlerts.remove(ruleId)
        if (alert != null) {
            alert.status = AlertStatus.RESOLVED
            alert.resolvedAt = Instant.now()
            
            logger.info("告警已解决: {} - {}", ruleId, alert.rule.message)
            
            // TODO: 发送告警解决通知
            sendAlertResolvedNotification(alert)
        }
    }
    
    /**
     * 发送告警通知
     */
    private fun sendAlertNotification(alert: Alert) {
        // TODO: 实现告警通知逻辑
        // 可以集成邮件、短信、Slack、钉钉等通知方式
        logger.info("发送告警通知: {}", alert)
    }
    
    /**
     * 发送告警解决通知
     */
    private fun sendAlertResolvedNotification(alert: Alert) {
        // TODO: 实现告警解决通知逻辑
        logger.info("发送告警解决通知: {}", alert)
    }
    
    /**
     * 获取活跃告警
     */
    fun getActiveAlerts(): List<Alert> {
        return activeAlerts.values.toList()
    }
    
    /**
     * 获取告警规则
     */
    fun getAlertRules(): List<AlertRule> {
        return alertRules.values.toList()
    }
    
    /**
     * 清理历史数据
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    fun cleanupHistoryData() {
        logger.info("开始清理监控历史数据")
        
        val cutoffTime = Instant.now().minusSeconds(24 * 60 * 60) // 保留24小时数据
        
        metricsHistory.values.forEach { history ->
            history.removeIf { it.timestamp.isBefore(cutoffTime) }
        }
        
        logger.info("监控历史数据清理完成")
    }
}

/**
 * 指标点
 */
data class MetricPoint(
    val value: Double,
    val timestamp: Instant
)

/**
 * GC指标
 */
data class GcMetrics(
    val totalCollections: Long,
    val totalTime: Long
)

/**
 * 请求指标
 */
data class RequestMetrics(
    val totalRequests: Long,
    val totalErrors: Long,
    val avgResponseTime: Double
)

/**
 * 系统概览
 */
data class SystemOverview(
    val timestamp: Instant,
    val cpuUsage: Double,
    val memoryUsage: Double,
    val heapUsage: Double,
    val threadCount: Int,
    val totalRequests: Long,
    val totalErrors: Long,
    val avgResponseTime: Double,
    val activeAlerts: List<Alert>
)

/**
 * 告警规则
 */
data class AlertRule(
    val id: String,
    val metricName: String,
    val threshold: Double,
    val operator: AlertOperator,
    val severity: AlertSeverity,
    val message: String,
    val enabled: Boolean = true
)

/**
 * 告警操作符
 */
enum class AlertOperator {
    GREATER_THAN,
    LESS_THAN,
    EQUALS
}

/**
 * 告警严重程度
 */
enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

/**
 * 告警
 */
data class Alert(
    val id: String,
    val rule: AlertRule,
    var currentValue: Double,
    val triggeredAt: Instant,
    var lastUpdated: Instant = triggeredAt,
    var status: AlertStatus,
    var resolvedAt: Instant? = null
)

/**
 * 告警状态
 */
enum class AlertStatus {
    ACTIVE,
    RESOLVED
}