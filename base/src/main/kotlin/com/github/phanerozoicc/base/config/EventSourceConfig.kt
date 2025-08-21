package com.github.phanerozoicc.base.config

import com.github.phanerozoicc.base.eventsource.DefaultSnapshotService
import com.github.phanerozoicc.base.eventsource.EventReplayService
import com.github.phanerozoicc.base.eventsource.EventStore
import com.github.phanerozoicc.base.eventsource.PostgreSQLEventStore
import com.github.phanerozoicc.base.eventsource.SnapshotProperties
import com.github.phanerozoicc.base.eventsource.SnapshotScheduler
import com.github.phanerozoicc.base.eventsource.SnapshotService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@EnableConfigurationProperties(SnapshotProperties::class)
@Configuration
class EventSourceConfig {

    @Bean
    fun eventStore(jdbcTemplate: JdbcTemplate): EventStore {
        return PostgreSQLEventStore(jdbcTemplate)
    }


    @ConditionalOnProperty(name = ["lifee.event-sourcing.snapshot.enabled"], havingValue = "true", matchIfMissing = true)
    @Bean
    fun eventReplayService(eventStore: EventStore): EventReplayService {
        return EventReplayService(eventStore)
    }

    @ConditionalOnProperty(name = ["lifee.event-sourcing.snapshot.enabled"], havingValue = "true", matchIfMissing = true)
    @Bean
    fun snapshotService(eventStore: EventStore, snapshotProperties: SnapshotProperties,
                        eventReplayService: EventReplayService): SnapshotService {
        return DefaultSnapshotService(eventStore, snapshotProperties, eventReplayService)
    }


    @ConditionalOnProperty(name = ["lifee.event-sourcing.snapshot.enabled"], havingValue = "true", matchIfMissing = true)
    @Bean
    fun snapshotScheduler(snapshotService: SnapshotService,
                          snapshotProperties: SnapshotProperties): SnapshotScheduler {
        return SnapshotScheduler(snapshotService, snapshotProperties)
    }

}