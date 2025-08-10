package com.lifee.config.app.adapters.persistence

import com.lifee.config.domain.aggregates.Configuration
import com.lifee.config.domain.entities.ConfigItem
import com.lifee.config.domain.valueobjects.*
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

/**
 * 配置JPA实体
 */
@Entity
@Table(
    name = "configurations",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["namespace", "environment"])
    ],
    indexes = [
        Index(name = "idx_config_namespace", columnList = "namespace"),
        Index(name = "idx_config_environment", columnList = "environment"),
        Index(name = "idx_config_namespace_env", columnList = "namespace,environment")
    ]
)
class ConfigurationJpaEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    var id: String = ""
    
    @Column(name = "namespace", nullable = false, length = 200)
    var namespace: String = ""
    
    @Column(name = "environment", nullable = false, length = 50)
    var environment: String = ""
    
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
    
    @OneToMany(
        mappedBy = "configuration",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var items: MutableSet<ConfigItemJpaEntity> = mutableSetOf()
    
    /**
     * 转换为领域对象
     */
    fun toDomain(): Configuration {
        val configId = ConfigId.of(id)
        val env = Environment.of(environment)
        
        val configuration = Configuration(
            id = configId,
            namespace = namespace,
            environment = env,
            createdAt = createdAt
        )
        
        // 添加配置项
        items.forEach { itemEntity ->
            val configKey = ConfigKey.of(itemEntity.key)
            val configValue = ConfigValue.of(itemEntity.value)
            val configType = ConfigType.valueOf(itemEntity.type)
            
            val configItem = ConfigItem(
                key = configKey,
                value = configValue,
                type = configType,
                environment = env,
                description = itemEntity.description,
                isEncrypted = itemEntity.isEncrypted,
                createdAt = itemEntity.createdAt,
                updatedAt = itemEntity.updatedAt
            )
            
            configuration.addExistingItem(configItem)
        }
        
        return configuration
    }
    
    companion object {
        /**
         * 从领域对象创建JPA实体
         */
        fun fromDomain(configuration: Configuration): ConfigurationJpaEntity {
            val entity = ConfigurationJpaEntity()
            entity.id = configuration.id.toString()
            entity.namespace = configuration.namespace
            entity.environment = configuration.environment.toString()
            entity.createdAt = configuration.createdAt
            entity.updatedAt = configuration.getUpdatedAt()
            
            // 转换配置项
            entity.items = configuration.getItems().values.map { item ->
                ConfigItemJpaEntity.fromDomain(item, entity)
            }.toMutableSet()
            
            return entity
        }
    }
}

/**
 * 配置项JPA实体
 */
@Entity
@Table(
    name = "config_items",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["configuration_id", "key"])
    ],
    indexes = [
        Index(name = "idx_config_item_key", columnList = "key"),
        Index(name = "idx_config_item_type", columnList = "type"),
        Index(name = "idx_config_item_encrypted", columnList = "is_encrypted")
    ]
)
class ConfigItemJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
    
    @Column(name = "key", nullable = false, length = 200)
    var key: String = ""
    
    @Column(name = "value", nullable = false, length = 5000)
    var value: String = ""
    
    @Column(name = "type", nullable = false, length = 20)
    var type: String = ""
    
    @Column(name = "environment", nullable = false, length = 50)
    var environment: String = ""
    
    @Column(name = "description", length = 1000)
    var description: String = ""
    
    @Column(name = "is_encrypted", nullable = false)
    var isEncrypted: Boolean = false
    
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuration_id", nullable = false)
    var configuration: ConfigurationJpaEntity? = null
    
    companion object {
        /**
         * 从领域对象创建JPA实体
         */
        fun fromDomain(
            item: ConfigItem,
            configurationEntity: ConfigurationJpaEntity
        ): ConfigItemJpaEntity {
            val entity = ConfigItemJpaEntity()
            entity.key = item.key.toString()
            entity.value = item.getValue().toString()
            entity.type = item.type.name
            entity.environment = item.environment.toString()
            entity.description = item.getDescription()
            entity.isEncrypted = item.isEncrypted()
            entity.createdAt = item.createdAt
            entity.updatedAt = item.getUpdatedAt()
            entity.configuration = configurationEntity
            
            return entity
        }
    }
}