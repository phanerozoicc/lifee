package com.github.phanerozoicc.base.eventsource

import com.github.phanerozoicc.base.event.Event
import com.github.phanerozoicc.base.exception.ConcurrencyDomainException
import com.github.phanerozoicc.base.utils.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.Instant

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
        events: List<Event>,
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
                    val eventData = JsonUtils.writeValueAsString(event)
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


    fun extractAggregateType(event: Event): String {
        return event::class.simpleName?.replace("Event", "")?: "unknown"
    }


}
