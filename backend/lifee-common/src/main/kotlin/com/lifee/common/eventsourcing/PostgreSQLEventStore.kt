package com.lifee.common.eventsourcing

import com.fasterxml.jackson.databind.ObjectMapper
import com.lifee.common.domain.AggregateSnapshot
import com.lifee.common.domain.DomainEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.util.*

/**
 * PostgreSQL事件存储实现
 */
@Repository
class PostgreSQLEventStore(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper
) : EventStore {
    
    private val logger = LoggerFactory.getLogger(PostgreSQLEventStore::class.java)
    
    companion object {
        // 事件表SQL
        private const val INSERT_EVENT_SQL = """
            INSERT INTO event_store (
                event_id, aggregate_id, aggregate_type, event_type, event_data, 
                version, occurred_on, created_at
            ) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?)
        """
        
        private const val SELECT_EVENTS_SQL = """
            SELECT event_id, aggregate_id, aggregate_type, event_type, event_data, 
                   version, occurred_on, created_at
            FROM event_store 
            WHERE aggregate_id = ? AND version > ?
            ORDER BY version ASC
        """
        
        private const val SELECT_EVENTS_TO_VERSION_SQL = """
            SELECT event_id, aggregate_id, aggregate_type, event_type, event_data, 
                   version, occurred_on, created_at
            FROM event_store 
            WHERE aggregate_id = ? AND version <= ?
            ORDER BY version ASC
        """
        
        private const val SELECT_EVENTS_BY_TIME_SQL = """
            SELECT event_id, aggregate_id, aggregate_type, event_type, event_data, 
                   version, occurred_on, created_at
            FROM event_store 
            WHERE aggregate_id = ? AND occurred_on BETWEEN ? AND ?
            ORDER BY version ASC
        """
        
        private const val SELECT_CURRENT_VERSION_SQL = """
            SELECT COALESCE(MAX(version), 0) as current_version
            FROM event_store 
            WHERE aggregate_id = ?
        """
        
        private const val CHECK_AGGREGATE_EXISTS_SQL = """
            SELECT EXISTS(SELECT 1 FROM event_store WHERE aggregate_id = ?)
        """
        
        // 快照表SQL
        private const val INSERT_SNAPSHOT_SQL = """
            INSERT INTO aggregate_snapshots (
                snapshot_id, aggregate_id, aggregate_type, version, 
                snapshot_data, created_at
            ) VALUES (?, ?, ?, ?, ?::jsonb, ?)
            ON CONFLICT (aggregate_id, version) 
            DO UPDATE SET 
                snapshot_data = EXCLUDED.snapshot_data,
                created_at = EXCLUDED.created_at
        """
        
        private const val SELECT_LATEST_SNAPSHOT_SQL = """
            SELECT snapshot_id, aggregate_id, aggregate_type, version, 
                   snapshot_data, created_at
            FROM aggregate_snapshots 
            WHERE aggregate_id = ? AND (? IS NULL OR version <= ?)
            ORDER BY version DESC 
            LIMIT 1
        """
        
        private const val SELECT_ALL_AGGREGATE_IDS_SQL = """
            SELECT DISTINCT aggregate_id 
            FROM event_store 
            WHERE (? IS NULL OR aggregate_type = ?)
            ORDER BY aggregate_id
            LIMIT ? OFFSET ?
        """
        
        private const val DELETE_AGGREGATE_EVENTS_SQL = """
            DELETE FROM event_store WHERE aggregate_id = ?
        """
        
        private const val DELETE_AGGREGATE_SNAPSHOTS_SQL = """
            DELETE FROM aggregate_snapshots WHERE aggregate_id = ?
        """
    }
    
    @Transactional
    override suspend fun saveEvents(
        aggregateId: String,
        events: List<DomainEvent>,
        expectedVersion: Long
    ) = withContext(Dispatchers.IO) {
        try {
            // 检查并发冲突
            val currentVersion = getCurrentVersion(aggregateId)
            if (currentVersion != expectedVersion) {
                throw ConcurrencyException(aggregateId, expectedVersion, currentVersion)
            }
            
            // 批量插入事件
            events.forEach { event ->
                val eventData = objectMapper.writeValueAsString(event)
                val aggregateType = extractAggregateType(event)
                
                jdbcTemplate.update(
                    INSERT_EVENT_SQL,
                    event.eventId.toString(),
                    aggregateId,
                    aggregateType,
                    event.getEventType(),
                    eventData,
                    event.version,
                    Timestamp.from(event.occurredOn),
                    Timestamp.from(Instant.now())
                )
            }
            
            logger.debug("Saved {} events for aggregate {}", events.size, aggregateId)
            
        } catch (e: DataIntegrityViolationException) {
            logger.error("Data integrity violation when saving events for aggregate {}", aggregateId, e)
            throw EventStoreException("Failed to save events due to data integrity violation", e)
        } catch (e: Exception) {
            logger.error("Error saving events for aggregate {}", aggregateId, e)
            throw EventStoreException("Failed to save events", e)
        }
    }
    
    override suspend fun getEvents(
        aggregateId: String,
        fromVersion: Long
    ): List<DomainEvent> = withContext(Dispatchers.IO) {
        try {
            val events = jdbcTemplate.query(
                SELECT_EVENTS_SQL,
                EventRowMapper(),
                aggregateId,
                fromVersion
            )
            
            logger.debug("Retrieved {} events for aggregate {} from version {}", 
                events.size, aggregateId, fromVersion)
            
            events
        } catch (e: Exception) {
            logger.error("Error retrieving events for aggregate {}", aggregateId, e)
            throw EventStoreException("Failed to retrieve events", e)
        }
    }
    
    override suspend fun getEventsToVersion(
        aggregateId: String,
        toVersion: Long
    ): List<DomainEvent> = withContext(Dispatchers.IO) {
        try {
            jdbcTemplate.query(
                SELECT_EVENTS_TO_VERSION_SQL,
                EventRowMapper(),
                aggregateId,
                toVersion
            )
        } catch (e: Exception) {
            logger.error("Error retrieving events for aggregate {} to version {}", aggregateId, toVersion, e)
            throw EventStoreException("Failed to retrieve events", e)
        }
    }
    
    override suspend fun getEventsByTimeRange(
        aggregateId: String,
        fromTime: Instant,
        toTime: Instant
    ): List<DomainEvent> = withContext(Dispatchers.IO) {
        try {
            jdbcTemplate.query(
                SELECT_EVENTS_BY_TIME_SQL,
                EventRowMapper(),
                aggregateId,
                Timestamp.from(fromTime),
                Timestamp.from(toTime)
            )
        } catch (e: Exception) {
            logger.error("Error retrieving events for aggregate {} by time range", aggregateId, e)
            throw EventStoreException("Failed to retrieve events by time range", e)
        }
    }
    
    override suspend fun getCurrentVersion(aggregateId: String): Long = withContext(Dispatchers.IO) {
        try {
            jdbcTemplate.queryForObject(
                SELECT_CURRENT_VERSION_SQL,
                Long::class.java,
                aggregateId
            ) ?: 0L
        } catch (e: Exception) {
            logger.error("Error getting current version for aggregate {}", aggregateId, e)
            throw EventStoreException("Failed to get current version", e)
        }
    }
    
    override suspend fun aggregateExists(aggregateId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            jdbcTemplate.queryForObject(
                CHECK_AGGREGATE_EXISTS_SQL,
                Boolean::class.java,
                aggregateId
            ) ?: false
        } catch (e: Exception) {
            logger.error("Error checking if aggregate {} exists", aggregateId, e)
            throw EventStoreException("Failed to check aggregate existence", e)
        }
    }
    
    @Transactional
    override suspend fun saveSnapshot(snapshot: AggregateSnapshot<*>) = withContext(Dispatchers.IO) {
        try {
            val snapshotData = objectMapper.writeValueAsString(snapshot.snapshotData)
            
            jdbcTemplate.update(
                INSERT_SNAPSHOT_SQL,
                UUID.randomUUID().toString(),
                snapshot.aggregateId.toString(),
                snapshot.aggregateType,
                snapshot.version,
                snapshotData,
                Timestamp.from(snapshot.createdAt)
            )
            
            logger.debug("Saved snapshot for aggregate {} at version {}", 
                snapshot.aggregateId, snapshot.version)
            
        } catch (e: Exception) {
            logger.error("Error saving snapshot for aggregate {}", snapshot.aggregateId, e)
            throw EventStoreException("Failed to save snapshot", e)
        }
    }
    
    override suspend fun getLatestSnapshot(
        aggregateId: String,
        maxVersion: Long?
    ): AggregateSnapshot<*>? = withContext(Dispatchers.IO) {
        try {
            val snapshots = jdbcTemplate.query(
                SELECT_LATEST_SNAPSHOT_SQL,
                SnapshotRowMapper(),
                aggregateId,
                maxVersion,
                maxVersion
            )
            
            snapshots.firstOrNull()
        } catch (e: Exception) {
            logger.error("Error retrieving latest snapshot for aggregate {}", aggregateId, e)
            throw EventStoreException("Failed to retrieve snapshot", e)
        }
    }
    
    override suspend fun getAllSnapshots(): List<AggregateSnapshot<*>> = withContext(Dispatchers.IO) {
        try {
            jdbcTemplate.query(
                "SELECT * FROM aggregate_snapshots ORDER BY created_at DESC",
                SnapshotRowMapper()
            )
        } catch (e: Exception) {
            logger.error("Error retrieving all snapshots", e)
            throw EventStoreException("Failed to retrieve all snapshots", e)
        }
    }
    
    @Transactional
    override suspend fun deleteSnapshot(aggregateId: String, version: Long) = withContext(Dispatchers.IO) {
        try {
            val deletedRows = jdbcTemplate.update(
                "DELETE FROM aggregate_snapshots WHERE aggregate_id = ? AND version = ?",
                aggregateId,
                version
            )
            
            logger.debug("Deleted {} snapshot(s) for aggregate {} at version {}", 
                deletedRows, aggregateId, version)
                
        } catch (e: Exception) {
            logger.error("Error deleting snapshot for aggregate {} at version {}", aggregateId, version, e)
            throw EventStoreException("Failed to delete snapshot", e)
        }
    }
    
    override suspend fun getAllAggregateIds(
        aggregateType: String?,
        limit: Int,
        offset: Int
    ): List<String> = withContext(Dispatchers.IO) {
        try {
            jdbcTemplate.queryForList(
                SELECT_ALL_AGGREGATE_IDS_SQL,
                String::class.java,
                aggregateType,
                aggregateType,
                limit,
                offset
            )
        } catch (e: Exception) {
            logger.error("Error retrieving aggregate IDs", e)
            throw EventStoreException("Failed to retrieve aggregate IDs", e)
        }
    }
    
    @Transactional
    override suspend fun deleteAggregate(aggregateId: String) = withContext(Dispatchers.IO) {
        try {
            // 删除快照
            jdbcTemplate.update(DELETE_AGGREGATE_SNAPSHOTS_SQL, aggregateId)
            
            // 删除事件
            val deletedEvents = jdbcTemplate.update(DELETE_AGGREGATE_EVENTS_SQL, aggregateId)
            
            logger.warn("Deleted aggregate {} with {} events", aggregateId, deletedEvents)
            
        } catch (e: Exception) {
            logger.error("Error deleting aggregate {}", aggregateId, e)
            throw EventStoreException("Failed to delete aggregate", e)
        }
    }
    
    /**
     * 从事件中提取聚合根类型
     */
    private fun extractAggregateType(event: DomainEvent): String {
        // 可以从事件类名或其他方式推断聚合根类型
        return event::class.simpleName?.replace("Event", "") ?: "Unknown"
    }
    
    /**
     * 事件行映射器
     */
    private inner class EventRowMapper : RowMapper<DomainEvent> {
        override fun mapRow(rs: ResultSet, rowNum: Int): DomainEvent {
            val eventType = rs.getString("event_type")
            val eventData = rs.getString("event_data")
            
            // 这里需要根据事件类型反序列化为具体的事件类
            // 可以使用事件注册表来获取事件类
            return deserializeEvent(eventType, eventData)
        }
    }
    
    /**
     * 快照行映射器
     */
    private inner class SnapshotRowMapper : RowMapper<AggregateSnapshot<Map<String, Any>>> {
        override fun mapRow(rs: ResultSet, rowNum: Int): AggregateSnapshot<Map<String, Any>> {
            val snapshotData = objectMapper.readValue(
                rs.getString("snapshot_data"),
                Map::class.java
            ) as Map<String, Any>
            
            return AggregateSnapshot(
                aggregateId = rs.getString("aggregate_id"),
                aggregateType = rs.getString("aggregate_type"),
                version = rs.getLong("version"),
                snapshotData = snapshotData,
                createdAt = rs.getTimestamp("created_at").toInstant()
            )
        }
    }
    
    /**
     * 反序列化事件
     * 这里需要实现具体的事件反序列化逻辑
     */
    private fun deserializeEvent(eventType: String, eventData: String): DomainEvent {
        // TODO: 实现事件反序列化逻辑
        // 可以使用事件注册表来获取事件类，然后反序列化
        throw NotImplementedError("Event deserialization not implemented yet")
    }
}