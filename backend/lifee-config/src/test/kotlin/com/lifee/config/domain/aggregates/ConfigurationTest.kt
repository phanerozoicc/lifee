package com.lifee.config.domain.aggregates

import com.lifee.config.domain.entities.ConfigItem
import com.lifee.config.domain.valueobjects.*
import com.lifee.config.domain.events.*
import com.lifee.config.domain.exceptions.InvalidConfigurationException
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.assertions.throwables.shouldThrow
import java.time.Instant
import java.util.UUID

/**
 * 配置聚合根测试
 */
class ConfigurationTest : BehaviorSpec({
    
    given("配置聚合根") {
        
        `when`("创建新配置") {
            val configId = ConfigId(UUID.randomUUID())
            val namespace = "test.namespace"
            val environment = Environment.DEVELOPMENT
            val userId = "user-123"
            
            val configuration = Configuration.create(
                id = configId,
                namespace = namespace,
                environment = environment,
                userId = userId
            )
            
            then("应该正确初始化配置") {
                configuration.getId() shouldBe configId
                configuration.getNamespace() shouldBe namespace
                configuration.getEnvironment() shouldBe environment
                configuration.getUserId() shouldBe userId
                configuration.getItems().size shouldBe 0
                configuration.getCreatedAt() shouldNotBe null
                configuration.getUpdatedAt() shouldNotBe null
            }
            
            then("应该发布配置创建事件") {
                val events = configuration.getUncommittedEvents()
                events.size shouldBe 1
                events[0] shouldBe ConfigurationCreatedEvent::class
            }
        }
        
        `when`("向配置添加配置项") {
            val configuration = createTestConfiguration()
            val configItem = createTestConfigItem()
            
            configuration.addItem(configItem)
            
            then("应该成功添加配置项") {
                configuration.getItems().size shouldBe 1
                configuration.getItems().values shouldContain configItem
            }
            
            then("应该发布配置项添加事件") {
                val events = configuration.getUncommittedEvents()
                // 第一个事件是创建事件，第二个是添加配置项事件
                events.size shouldBe 2
                events[1] shouldBe ConfigItemAddedEvent::class
            }
        }
        
        `when`("添加重复的配置项") {
            val configuration = createTestConfiguration()
            val configItem = createTestConfigItem()
            
            configuration.addItem(configItem)
            
            then("应该抛出异常") {
                shouldThrow<InvalidConfigurationException> {
                    configuration.addItem(configItem)
                }
            }
        }
        
        `when`("从配置中移除配置项") {
            val configuration = createTestConfiguration()
            val configItem = createTestConfigItem()
            
            configuration.addItem(configItem)
            configuration.removeItem(configItem.getKey())
            
            then("应该成功移除配置项") {
                configuration.getItems().size shouldBe 0
                configuration.getItems().values shouldNotContain configItem
            }
            
            then("应该发布配置项移除事件") {
                val events = configuration.getUncommittedEvents()
                // 创建、添加、移除三个事件
                events.size shouldBe 3
                events[2] shouldBe ConfigItemRemovedEvent::class
            }
        }
        
        `when`("移除不存在的配置项") {
            val configuration = createTestConfiguration()
            val nonExistentKey = ConfigKey("non.existent.key")
            
            then("应该抛出异常") {
                shouldThrow<InvalidConfigurationException> {
                    configuration.removeItem(nonExistentKey)
                }
            }
        }
        
        `when`("更新配置项") {
            val configuration = createTestConfiguration()
            val configItem = createTestConfigItem()
            val newValue = ConfigValue("updated-value", ConfigType.STRING)
            
            configuration.addItem(configItem)
            configuration.updateItem(configItem.getKey(), newValue)
            
            then("应该成功更新配置项") {
                val updatedItem = configuration.getItem(configItem.getKey())
                updatedItem!!.getValue() shouldBe newValue
            }
            
            then("应该发布配置项更新事件") {
                val events = configuration.getUncommittedEvents()
                // 创建、添加、更新三个事件
                events.size shouldBe 3
                events[2] shouldBe ConfigItemUpdatedEvent::class
            }
        }
        
        `when`("更新不存在的配置项") {
            val configuration = createTestConfiguration()
            val nonExistentKey = ConfigKey("non.existent.key")
            val value = ConfigValue("value", ConfigType.STRING)
            
            then("应该抛出异常") {
                shouldThrow<InvalidConfigurationException> {
                    configuration.updateItem(nonExistentKey, value)
                }
            }
        }
        
        `when`("获取配置项") {
            val configuration = createTestConfiguration()
            val configItem = createTestConfigItem()
            
            configuration.addItem(configItem)
            
            then("应该能获取已添加的配置项") {
                val retrievedItem = configuration.getItem(configItem.getKey())
                retrievedItem shouldBe configItem
            }
            
            then("获取不存在的配置项应该返回null") {
                val nonExistentKey = ConfigKey("non.existent.key")
                val retrievedItem = configuration.getItem(nonExistentKey)
                retrievedItem shouldBe null
            }
        }
        
        `when`("检查配置项是否存在") {
            val configuration = createTestConfiguration()
            val configItem = createTestConfigItem()
            
            configuration.addItem(configItem)
            
            then("应该正确检查配置项存在性") {
                configuration.hasItem(configItem.getKey()) shouldBe true
                
                val nonExistentKey = ConfigKey("non.existent.key")
                configuration.hasItem(nonExistentKey) shouldBe false
            }
        }
        
        `when`("清空配置") {
            val configuration = createTestConfiguration()
            val item1 = createTestConfigItem()
            val item2 = ConfigItem(
                key = ConfigKey("test.key2"),
                value = ConfigValue("value2", ConfigType.STRING),
                description = "测试配置项2"
            )
            
            configuration.addItem(item1)
            configuration.addItem(item2)
            configuration.clear()
            
            then("应该清空所有配置项") {
                configuration.getItems().size shouldBe 0
            }
            
            then("应该发布清空事件") {
                val events = configuration.getUncommittedEvents()
                // 创建、添加、添加、清空四个事件
                events.size shouldBe 4
                events[3] shouldBe ConfigurationClearedEvent::class
            }
        }
        
        `when`("复制配置") {
            val sourceConfiguration = createTestConfiguration()
            val item1 = createTestConfigItem()
            val item2 = ConfigItem(
                key = ConfigKey("test.key2"),
                value = ConfigValue("value2", ConfigType.STRING),
                description = "测试配置项2"
            )
            
            sourceConfiguration.addItem(item1)
            sourceConfiguration.addItem(item2)
            
            val targetId = ConfigId(UUID.randomUUID())
            val targetEnvironment = Environment.PRODUCTION
            
            val copiedConfiguration = sourceConfiguration.copyTo(
                targetId = targetId,
                targetEnvironment = targetEnvironment
            )
            
            then("应该创建包含所有配置项的副本") {
                copiedConfiguration.getId() shouldBe targetId
                copiedConfiguration.getEnvironment() shouldBe targetEnvironment
                copiedConfiguration.getNamespace() shouldBe sourceConfiguration.getNamespace()
                copiedConfiguration.getItems().size shouldBe 2
            }
            
            then("应该发布配置复制事件") {
                val events = sourceConfiguration.getUncommittedEvents()
                // 创建、添加、添加、复制四个事件
                events.size shouldBe 4
                events[3] shouldBe ConfigurationCopiedEvent::class
            }
        }
        
        `when`("发布配置") {
            val configuration = createTestConfiguration()
            val configItem = createTestConfigItem()
            
            configuration.addItem(configItem)
            configuration.publish()
            
            then("应该标记为已发布") {
                configuration.isPublished() shouldBe true
            }
            
            then("应该发布配置发布事件") {
                val events = configuration.getUncommittedEvents()
                // 创建、添加、发布三个事件
                events.size shouldBe 3
                events[2] shouldBe ConfigurationPublishedEvent::class
            }
        }
        
        `when`("获取配置项数量") {
            val configuration = createTestConfiguration()
            
            then("初始数量应该为0") {
                configuration.getItemCount() shouldBe 0
            }
            
            configuration.addItem(createTestConfigItem())
            
            then("添加配置项后数量应该正确") {
                configuration.getItemCount() shouldBe 1
            }
        }
        
        `when`("检查配置是否为空") {
            val configuration = createTestConfiguration()
            
            then("新配置应该为空") {
                configuration.isEmpty() shouldBe true
            }
            
            configuration.addItem(createTestConfigItem())
            
            then("有配置项的配置不应该为空") {
                configuration.isEmpty() shouldBe false
            }
        }
        
        `when`("按类型获取配置项") {
            val configuration = createTestConfiguration()
            val stringItem = ConfigItem(
                key = ConfigKey("string.key"),
                value = ConfigValue("string-value", ConfigType.STRING),
                description = "字符串配置"
            )
            val numberItem = ConfigItem(
                key = ConfigKey("number.key"),
                value = ConfigValue("123", ConfigType.NUMBER),
                description = "数字配置"
            )
            val booleanItem = ConfigItem(
                key = ConfigKey("boolean.key"),
                value = ConfigValue("true", ConfigType.BOOLEAN),
                description = "布尔配置"
            )
            
            configuration.addItem(stringItem)
            configuration.addItem(numberItem)
            configuration.addItem(booleanItem)
            
            then("应该能按类型筛选配置项") {
                val stringItems = configuration.getItemsByType(ConfigType.STRING)
                stringItems.size shouldBe 1
                stringItems shouldContain stringItem
                
                val numberItems = configuration.getItemsByType(ConfigType.NUMBER)
                numberItems.size shouldBe 1
                numberItems shouldContain numberItem
                
                val booleanItems = configuration.getItemsByType(ConfigType.BOOLEAN)
                booleanItems.size shouldBe 1
                booleanItems shouldContain booleanItem
            }
        }
    }
})

/**
 * 创建测试用的配置
 */
private fun createTestConfiguration(): Configuration {
    return Configuration.create(
        id = ConfigId(UUID.randomUUID()),
        namespace = "test.namespace",
        environment = Environment.DEVELOPMENT,
        userId = "user-123"
    )
}

/**
 * 创建测试用的配置项
 */
private fun createTestConfigItem(): ConfigItem {
    return ConfigItem(
        key = ConfigKey("test.key"),
        value = ConfigValue("test-value", ConfigType.STRING),
        description = "测试配置项"
    )
}