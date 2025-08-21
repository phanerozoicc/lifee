package com.github.phanerozoicc.base.eventsource

import com.fasterxml.jackson.core.type.TypeReference
import com.github.phanerozoicc.base.event.DomainEvent
import com.github.phanerozoicc.base.exception.ConcurrencyDomainException
import com.github.phanerozoicc.base.utils.JsonUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Repository
class PostgreSQLEventStore(
    private val jdbcTemplate: JdbcTemplate
): EventStore {

    companion object: KLogging()

    /**
     * Get the current version of an aggregate
     */
    override suspend fun getCurrentVersion(aggregateId: String): Long = withContext(Dispatchers.IO) {
            try {
                val sql = "select max(version, 0) from event_store where aggregate_id = ?"
                jdbcTemplate.queryForObject(sql, Long::class.java, aggregateId)?: 0L
            } catch (e: Exception) {
                logger.error("error getting current version for aggregate {}", aggregateId, e)
                throw EventStoreException("error getting current version for aggregate $aggregateId", e)
            }
        }


    override suspend fun saveEvents(
        aggregateId: String,
        events: List<DomainEvent>,
        expectedVersion: Long
    )  = withContext(Dispatchers.IO) {
        try {
            // 检查并发冲突
            val currentVersion = getCurrentVersion(aggregateId)
            if (currentVersion!=expectedVersion) {
                throw ConcurrencyDomainException(aggregateId, expectedVersion, currentVersion)
            }
            // 保存事件
            val sql = "insert into event_store(event_id, aggregate_id, aggregate_type," +
                    " event_type, event_data, version, occurred_on, created_at) " +
                    "values (?, ?, ?, ?, ?::jsonb, ?, ?, ?)"

            for (event in events) {
                val eventData = JsonUtil.writeValueAsString(event)
                val aggregateType = extractAggregateType(event)
                jdbcTemplate.update(sql, event.eventId, event.aggregateId, aggregateType,
                    event.eventType, eventData, event.version, Timestamp.from(event.occurredOn),
                    Timestamp.from(Instant.now()))
            }
            logger.debug("saved {} events for aggregate {}", events.size, aggregateId)
        } catch (e: DataIntegrityViolationException) {
            logger.error("data integrity violation when saving events for aggregate {}", aggregateId, e)
            throw EventStoreException("data integrity violation when saving events for aggregate $aggregateId", e)
        } catch (e: Exception) {
            logger.error("error saving events for aggregate {}", aggregateId, e)
            throw EventStoreException("failed to save events for aggregate $aggregateId", e)
        }
    }


    fun extractAggregateType(event: DomainEvent): String {
        return event::class.simpleName?.replace("Event", "")?: "unknown"
    }


    override suspend fun getLastSnapshot(
        aggregateId: String,
        maxVersion: Long?
    ): AggregateSnapshot<*>? = withContext(Dispatchers.IO) {
        try {
            val sql = "select snapshot_id, aggregate_id, aggregate_type, version, snapshot_data, created_at" +
                    " from aggregate_snapshots" +
                    " where aggregate_id = ? and version <= ? order by version desc" +
                    " limit 1"
            val snapshot = jdbcTemplate.query(sql, SnapshotRowMapper(), aggregateId, maxVersion)
            snapshot.firstOrNull()
        } catch (e: Exception) {
            logger.error("error retrieving latest snapshot for aggregate {}", aggregateId, e)
            throw EventStoreException("error retrieving latest snapshot for aggregate $aggregateId", e)
        }
    }

    override suspend fun saveSnapshot(snapshot: AggregateSnapshot<Map<String, Any>>) = withContext(Dispatchers.IO) {
        try {
            val sql = "insert into aggregate_snapshots(snapshot_id, aggregate_id, aggregate_type," +
                    " version, snapshot_data, created_at) values (?, ?, ?, ?, ?::jsonb, ?)"

            val snapShotDataJsonStr = JsonUtil.writeValueAsString(snapshot.snapshotData)
            jdbcTemplate.update(sql, UUID.randomUUID().toString(), snapshot.aggregateId, snapshot.aggregateType,
                snapshot.version, snapShotDataJsonStr, Timestamp.from(snapshot.createdAt))
            logger.debug("saved snapshot for aggregate type:{} version:{}", snapshot.aggregateType, snapshot.version)
        }catch (e: Exception) {
            logger.error("error saving snapshot for aggregate {}", snapshot.aggregateId, e)
            throw EventStoreException("error saving snapshot for aggregate ${snapshot.aggregateId}", e)
        }
    }



    override suspend fun getAllSnapshots(aggregateId: String): List<AggregateSnapshot<*>> = withContext(Dispatchers.IO) {
        try {
            val sql = "select *" +
                    " from aggregate_snapshots" +
                    " where aggregate_id = ? order by version asc"
            return@withContext jdbcTemplate.query(sql, SnapshotRowMapper(), aggregateId)
        } catch (e: Exception) {
            logger.error("error retrieving all snapshots for aggregate {}", aggregateId, e)
            throw EventStoreException("error retrieving all snapshots for aggregate $aggregateId", e)
        }
    }

    override suspend fun deleteSnapshot(aggregateId: String, version: Long) = withContext(Dispatchers.IO) {
        try {
            val sql = "delete from aggregate_snapshots where aggregate_id = ? and version = ?"
            jdbcTemplate.update(sql, aggregateId, version)
            logger.debug("deleted snapshot for aggregate {} version {}", aggregateId, version)
        } catch (e: Exception) {
            logger.error("error deleting snapshot for aggregate {}", aggregateId, e)
            throw EventStoreException("error deleting snapshot for aggregate $aggregateId", e)
        }
    }
}


private class SnapshotRowMapper: RowMapper<AggregateSnapshot<Map<String, Any>>> {
    override fun mapRow(
        rs: ResultSet,
        rowNum: Int
    ): AggregateSnapshot<Map<String, Any>> {
        val snapshotData= JsonUtil.readValue(rs.getString("snapshot_data"), object: TypeReference<Map<String, Any>?>(){})
        return AggregateSnapshot(
            aggregateId = rs.getString("aggregate_id"),
            aggregateType = rs.getString("aggregate_type"),
            version = rs.getLong("version"),
            snapshotData = snapshotData!!,
            createdAt = rs.getTimestamp("created_at").toInstant()
        )
    }
}
