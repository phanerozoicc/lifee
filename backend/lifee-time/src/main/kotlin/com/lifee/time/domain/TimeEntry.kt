package com.lifee.time.domain

import com.lifee.common.domain.EventSourcedAggregateRoot
import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import com.lifee.time.domain.events.*
import java.time.Instant

/**
 * 时间条目聚合根
 * 表示用户的一次时间追踪记录
 */
class TimeEntry private constructor(
    id: TimeEntryId
) : EventSourcedAggregateRoot<TimeEntryId>(id) {

    private var _userId: UserId? = null
    private var _projectId: ProjectId? = null
    private var _taskId: String? = null
    private var _description: String = ""
    private var _tags: MutableSet<String> = mutableSetOf()
    private var _timeRange: TimeRange? = null
    private var _status: TimeEntryStatus = TimeEntryStatus.STOPPED
    private var _billable: Boolean = false
    private var _hourlyRate: Double? = null
    private var _createdAt: Instant? = null
    private var _updatedAt: Instant? = null

    // 只读属性
    val userId: UserId get() = _userId ?: throw IllegalStateException("TimeEntry not initialized")
    val projectId: ProjectId? get() = _projectId
    val taskId: String? get() = _taskId
    val description: String get() = _description
    val tags: Set<String> get() = _tags.toSet()
    val timeRange: TimeRange? get() = _timeRange
    val status: TimeEntryStatus get() = _status
    val billable: Boolean get() = _billable
    val hourlyRate: Double? get() = _hourlyRate
    val createdAt: Instant get() = _createdAt ?: throw IllegalStateException("TimeEntry not initialized")
    val updatedAt: Instant get() = _updatedAt ?: throw IllegalStateException("TimeEntry not initialized")

    companion object {
        /**
         * 创建新的时间条目
         */
        fun create(
            userId: UserId,
            description: String = "",
            projectId: ProjectId? = null,
            taskId: String? = null,
            tags: Set<String> = emptySet(),
            billable: Boolean = false,
            hourlyRate: Double? = null
        ): TimeEntry {
            val timeEntryId = TimeEntryId.generate()
            val timeEntry = TimeEntry(timeEntryId)
            
            val now = Instant.now()
            timeEntry.applyEvent(
                TimeEntryCreatedEvent(
                    timeEntryId = timeEntryId,
                    userId = userId,
                    description = description,
                    projectId = projectId,
                    taskId = taskId,
                    tags = tags,
                    billable = billable,
                    hourlyRate = hourlyRate,
                    createdAt = now
                )
            )
            
            return timeEntry
        }

        /**
         * 从事件流重建时间条目
         */
        fun fromEvents(id: TimeEntryId, events: List<DomainEvent>): TimeEntry {
            val timeEntry = TimeEntry(id)
            events.forEach { event -> timeEntry.applyEvent(event) }
            return timeEntry
        }
    }

    /**
     * 开始时间追踪
     */
    fun start(startTime: Instant = Instant.now()): TimeEntry {
        require(_status != TimeEntryStatus.RUNNING) { "TimeEntry is already running" }
        require(_status != TimeEntryStatus.DELETED) { "Cannot start deleted TimeEntry" }

        applyEvent(
            TimeEntryStartedEvent(
                timeEntryId = id,
                userId = userId,
                startTime = startTime,
                occurredAt = Instant.now()
            )
        )
        
        return this
    }

    /**
     * 停止时间追踪
     */
    fun stop(endTime: Instant = Instant.now()): TimeEntry {
        require(_status.canStop()) { "Cannot stop TimeEntry in status: $_status" }
        require(_timeRange != null) { "TimeEntry has no time range" }
        require(_timeRange!!.startTime.isBefore(endTime) || _timeRange!!.startTime == endTime) { 
            "End time cannot be before start time" 
        }

        applyEvent(
            TimeEntryStoppedEvent(
                timeEntryId = id,
                userId = userId,
                endTime = endTime,
                duration = Duration.between(_timeRange!!.startTime, endTime),
                occurredAt = Instant.now()
            )
        )
        
        return this
    }

    /**
     * 暂停时间追踪
     */
    fun pause(pauseTime: Instant = Instant.now()): TimeEntry {
        require(_status.canPause()) { "Cannot pause TimeEntry in status: $_status" }

        applyEvent(
            TimeEntryPausedEvent(
                timeEntryId = id,
                userId = userId,
                pauseTime = pauseTime,
                occurredAt = Instant.now()
            )
        )
        
        return this
    }

    /**
     * 恢复时间追踪
     */
    fun resume(resumeTime: Instant = Instant.now()): TimeEntry {
        require(_status.canResume()) { "Cannot resume TimeEntry in status: $_status" }

        applyEvent(
            TimeEntryResumedEvent(
                timeEntryId = id,
                userId = userId,
                resumeTime = resumeTime,
                occurredAt = Instant.now()
            )
        )
        
        return this
    }

    /**
     * 更新描述
     */
    fun updateDescription(newDescription: String): TimeEntry {
        require(_status != TimeEntryStatus.DELETED) { "Cannot update deleted TimeEntry" }
        require(newDescription.length <= 500) { "Description too long: ${newDescription.length}" }

        if (_description != newDescription) {
            applyEvent(
                TimeEntryDescriptionUpdatedEvent(
                    timeEntryId = id,
                    userId = userId,
                    oldDescription = _description,
                    newDescription = newDescription,
                    occurredAt = Instant.now()
                )
            )
        }
        
        return this
    }

    /**
     * 更新项目
     */
    fun updateProject(newProjectId: ProjectId?): TimeEntry {
        require(_status != TimeEntryStatus.DELETED) { "Cannot update deleted TimeEntry" }

        if (_projectId != newProjectId) {
            applyEvent(
                TimeEntryProjectUpdatedEvent(
                    timeEntryId = id,
                    userId = userId,
                    oldProjectId = _projectId,
                    newProjectId = newProjectId,
                    occurredAt = Instant.now()
                )
            )
        }
        
        return this
    }

    /**
     * 更新标签
     */
    fun updateTags(newTags: Set<String>): TimeEntry {
        require(_status != TimeEntryStatus.DELETED) { "Cannot update deleted TimeEntry" }
        require(newTags.size <= 10) { "Too many tags: ${newTags.size}" }
        require(newTags.all { it.length <= 50 }) { "Tag too long" }

        if (_tags != newTags) {
            applyEvent(
                TimeEntryTagsUpdatedEvent(
                    timeEntryId = id,
                    userId = userId,
                    oldTags = _tags.toSet(),
                    newTags = newTags,
                    occurredAt = Instant.now()
                )
            )
        }
        
        return this
    }

    /**
     * 更新计费状态
     */
    fun updateBillable(billable: Boolean, hourlyRate: Double? = null): TimeEntry {
        require(_status != TimeEntryStatus.DELETED) { "Cannot update deleted TimeEntry" }
        if (billable && hourlyRate != null) {
            require(hourlyRate > 0) { "Hourly rate must be positive: $hourlyRate" }
        }

        if (_billable != billable || _hourlyRate != hourlyRate) {
            applyEvent(
                TimeEntryBillableUpdatedEvent(
                    timeEntryId = id,
                    userId = userId,
                    billable = billable,
                    hourlyRate = hourlyRate,
                    occurredAt = Instant.now()
                )
            )
        }
        
        return this
    }

    /**
     * 删除时间条目
     */
    fun delete(): TimeEntry {
        require(_status.canDelete()) { "Cannot delete TimeEntry in status: $_status" }

        applyEvent(
            TimeEntryDeletedEvent(
                timeEntryId = id,
                userId = userId,
                occurredAt = Instant.now()
            )
        )
        
        return this
    }

    /**
     * 获取总持续时间
     */
    fun getTotalDuration(): Duration? {
        return when (_status) {
            TimeEntryStatus.STOPPED -> _timeRange?.getDuration()
            TimeEntryStatus.RUNNING, TimeEntryStatus.PAUSED -> _timeRange?.getDurationUntilNow()
            TimeEntryStatus.DELETED -> null
        }
    }

    /**
     * 计算收入（如果可计费）
     */
    fun calculateRevenue(): Double? {
        if (!_billable || _hourlyRate == null) return null
        val duration = getTotalDuration() ?: return null
        return duration.toHours().toDouble() * _hourlyRate!!
    }

    /**
     * 应用领域事件
     */
    override fun applyEvent(event: DomainEvent) {
        when (event) {
            is TimeEntryCreatedEvent -> {
                _userId = event.userId
                _description = event.description
                _projectId = event.projectId
                _taskId = event.taskId
                _tags = event.tags.toMutableSet()
                _billable = event.billable
                _hourlyRate = event.hourlyRate
                _status = TimeEntryStatus.STOPPED
                _createdAt = event.createdAt
                _updatedAt = event.createdAt
            }
            is TimeEntryStartedEvent -> {
                _timeRange = TimeRange.ongoing(event.startTime)
                _status = TimeEntryStatus.RUNNING
                _updatedAt = event.occurredAt
            }
            is TimeEntryStoppedEvent -> {
                _timeRange = _timeRange?.end(event.endTime)
                _status = TimeEntryStatus.STOPPED
                _updatedAt = event.occurredAt
            }
            is TimeEntryPausedEvent -> {
                _status = TimeEntryStatus.PAUSED
                _updatedAt = event.occurredAt
            }
            is TimeEntryResumedEvent -> {
                _status = TimeEntryStatus.RUNNING
                _updatedAt = event.occurredAt
            }
            is TimeEntryDescriptionUpdatedEvent -> {
                _description = event.newDescription
                _updatedAt = event.occurredAt
            }
            is TimeEntryProjectUpdatedEvent -> {
                _projectId = event.newProjectId
                _updatedAt = event.occurredAt
            }
            is TimeEntryTagsUpdatedEvent -> {
                _tags = event.newTags.toMutableSet()
                _updatedAt = event.occurredAt
            }
            is TimeEntryBillableUpdatedEvent -> {
                _billable = event.billable
                _hourlyRate = event.hourlyRate
                _updatedAt = event.occurredAt
            }
            is TimeEntryDeletedEvent -> {
                _status = TimeEntryStatus.DELETED
                _updatedAt = event.occurredAt
            }
        }
        super.applyEvent(event)
    }

    /**
     * 序列化状态
     */
    override fun serializeState(): Map<String, Any?> {
        return mapOf(
            "userId" to _userId?.value,
            "projectId" to _projectId?.value,
            "taskId" to _taskId,
            "description" to _description,
            "tags" to _tags.toList(),
            "timeRange" to _timeRange?.let { mapOf(
                "startTime" to it.startTime.toString(),
                "endTime" to it.endTime?.toString()
            )},
            "status" to _status.name,
            "billable" to _billable,
            "hourlyRate" to _hourlyRate,
            "createdAt" to _createdAt?.toString(),
            "updatedAt" to _updatedAt?.toString()
        )
    }

    /**
     * 反序列化状态
     */
    override fun deserializeState(state: Map<String, Any?>) {
        _userId = (state["userId"] as? String)?.let { UserId.of(it) }
        _projectId = (state["projectId"] as? String)?.let { ProjectId.of(it) }
        _taskId = state["taskId"] as? String
        _description = state["description"] as? String ?: ""
        _tags = ((state["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()).toMutableSet()
        
        val timeRangeMap = state["timeRange"] as? Map<*, *>
        _timeRange = timeRangeMap?.let {
            val startTime = Instant.parse(it["startTime"] as String)
            val endTime = (it["endTime"] as? String)?.let { Instant.parse(it) }
            TimeRange(startTime, endTime)
        }
        
        _status = TimeEntryStatus.valueOf(state["status"] as? String ?: "STOPPED")
        _billable = state["billable"] as? Boolean ?: false
        _hourlyRate = state["hourlyRate"] as? Double
        _createdAt = (state["createdAt"] as? String)?.let { Instant.parse(it) }
        _updatedAt = (state["updatedAt"] as? String)?.let { Instant.parse(it) }
    }
}