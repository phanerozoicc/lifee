package com.lifee.common.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties

/**
 * Kafka消费者配置
 */
@Configuration
@EnableKafka
class KafkaConsumerConfig {
    
    @Value("\${spring.kafka.bootstrap-servers:localhost:9092}")
    private lateinit var bootstrapServers: String
    
    @Value("\${spring.kafka.consumer.group-id:lifee-event-group}")
    private lateinit var groupId: String
    
    @Value("\${spring.kafka.consumer.auto-offset-reset:earliest}")
    private lateinit var autoOffsetReset: String
    
    @Value("\${spring.kafka.consumer.enable-auto-commit:true}")
    private var enableAutoCommit: Boolean = true
    
    @Value("\${spring.kafka.consumer.auto-commit-interval:1000}")
    private var autoCommitInterval: Int = 1000
    
    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val configProps = mutableMapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to autoOffsetReset,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to enableAutoCommit,
            ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG to autoCommitInterval,
            // 设置会话超时和心跳间隔
            ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG to 30000,
            ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG to 10000,
            // 设置最大拉取记录数
            ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 100,
            // 设置拉取超时时间
            ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG to 300000
        )
        
        return DefaultKafkaConsumerFactory(configProps)
    }
    
    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory()
        
        // 设置并发级别
        factory.setConcurrency(3)
        
        // 设置确认模式
        factory.containerProperties.ackMode = ContainerProperties.AckMode.BATCH
        
        // 设置错误处理
        factory.setCommonErrorHandler(org.springframework.kafka.listener.DefaultErrorHandler())
        
        return factory
    }
}