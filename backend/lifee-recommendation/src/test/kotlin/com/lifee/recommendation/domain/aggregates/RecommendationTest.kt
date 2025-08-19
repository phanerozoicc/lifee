package com.lifee.recommendation.domain.aggregates

import com.lifee.recommendation.domain.entities.RecommendationItem
import com.lifee.recommendation.domain.valueobjects.*
import com.lifee.recommendation.domain.events.*
import com.lifee.recommendation.domain.exceptions.InvalidRecommendationException
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.assertions.throwables.shouldThrow
import java.time.Instant
import java.util.UUID

/**
 * 推荐聚合根测试
 */
class RecommendationTest : BehaviorSpec({
    
    given("推荐聚合根") {
        
        `when`("创建新推荐") {
            val recommendationId = RecommendationId(UUID.randomUUID())
            val userId = "user-123"
            val recommendationType = RecommendationType.CONTENT_BASED
            
            val recommendation = Recommendation.create(
                recommendationId = recommendationId,
                userId = userId,
                type = recommendationType
            )
            
            then("应该正确初始化推荐") {
                recommendation.getId() shouldBe recommendationId
                recommendation.getUserId() shouldBe userId
                recommendation.getType() shouldBe recommendationType
                recommendation.getItems().size shouldBe 0
                recommendation.getCreatedAt() shouldNotBe null
                recommendation.getUpdatedAt() shouldNotBe null
            }
            
            then("应该发布推荐创建事件") {
                val events = recommendation.getUncommittedEvents()
                events.size shouldBe 1
                events[0] shouldBe RecommendationCreatedEvent::class
            }
        }
        
        `when`("向推荐添加项目") {
            val recommendation = createTestRecommendation()
            val item = createTestRecommendationItem()
            
            recommendation.addItem(item)
            
            then("应该成功添加项目") {
                recommendation.getItems().size shouldBe 1
                recommendation.getItems() shouldContain item
            }
            
            then("应该发布项目添加事件") {
                val events = recommendation.getUncommittedEvents()
                // 第一个事件是创建事件，第二个是添加项目事件
                events.size shouldBe 2
                events[1] shouldBe RecommendationItemAddedEvent::class
            }
        }
        
        `when`("添加重复的推荐项目") {
            val recommendation = createTestRecommendation()
            val item = createTestRecommendationItem()
            
            recommendation.addItem(item)
            
            then("应该抛出异常") {
                shouldThrow<InvalidRecommendationException> {
                    recommendation.addItem(item)
                }
            }
        }
        
        `when`("从推荐中移除项目") {
            val recommendation = createTestRecommendation()
            val item = createTestRecommendationItem()
            
            recommendation.addItem(item)
            recommendation.removeItem(item.getContentId())
            
            then("应该成功移除项目") {
                recommendation.getItems().size shouldBe 0
                recommendation.getItems() shouldNotContain item
            }
            
            then("应该发布项目移除事件") {
                val events = recommendation.getUncommittedEvents()
                // 创建、添加、移除三个事件
                events.size shouldBe 3
                events[2] shouldBe RecommendationItemRemovedEvent::class
            }
        }
        
        `when`("移除不存在的项目") {
            val recommendation = createTestRecommendation()
            val nonExistentContentId = ContentId("non-existent")
            
            then("应该抛出异常") {
                shouldThrow<InvalidRecommendationException> {
                    recommendation.removeItem(nonExistentContentId)
                }
            }
        }
        
        `when`("更新推荐分数") {
            val recommendation = createTestRecommendation()
            val item = createTestRecommendationItem()
            val newScore = RecommendationScore(0.95)
            
            recommendation.addItem(item)
            recommendation.updateScore(item.getContentId(), newScore)
            
            then("应该成功更新分数") {
                val updatedItem = recommendation.getItems().first { it.getContentId() == item.getContentId() }
                updatedItem.getScore() shouldBe newScore
            }
            
            then("应该发布分数更新事件") {
                val events = recommendation.getUncommittedEvents()
                // 创建、添加、更新分数三个事件
                events.size shouldBe 3
                events[2] shouldBe RecommendationScoreUpdatedEvent::class
            }
        }
        
        `when`("清空推荐") {
            val recommendation = createTestRecommendation()
            val item1 = createTestRecommendationItem()
            val item2 = RecommendationItem(
                contentId = ContentId("content-2"),
                score = RecommendationScore(0.8),
                reason = "测试原因2"
            )
            
            recommendation.addItem(item1)
            recommendation.addItem(item2)
            recommendation.clear()
            
            then("应该清空所有项目") {
                recommendation.getItems().size shouldBe 0
            }
            
            then("应该发布清空事件") {
                val events = recommendation.getUncommittedEvents()
                // 创建、添加、添加、清空四个事件
                events.size shouldBe 4
                events[3] shouldBe RecommendationClearedEvent::class
            }
        }
        
        `when`("获取推荐项目数量") {
            val recommendation = createTestRecommendation()
            
            then("初始数量应该为0") {
                recommendation.getItemCount() shouldBe 0
            }
            
            recommendation.addItem(createTestRecommendationItem())
            
            then("添加项目后数量应该正确") {
                recommendation.getItemCount() shouldBe 1
            }
        }
        
        `when`("检查推荐是否为空") {
            val recommendation = createTestRecommendation()
            
            then("新推荐应该为空") {
                recommendation.isEmpty() shouldBe true
            }
            
            recommendation.addItem(createTestRecommendationItem())
            
            then("有项目的推荐不应该为空") {
                recommendation.isEmpty() shouldBe false
            }
        }
        
        `when`("获取最高分推荐项目") {
            val recommendation = createTestRecommendation()
            val item1 = RecommendationItem(
                contentId = ContentId("content-1"),
                score = RecommendationScore(0.7),
                reason = "测试原因1"
            )
            val item2 = RecommendationItem(
                contentId = ContentId("content-2"),
                score = RecommendationScore(0.9),
                reason = "测试原因2"
            )
            val item3 = RecommendationItem(
                contentId = ContentId("content-3"),
                score = RecommendationScore(0.8),
                reason = "测试原因3"
            )
            
            recommendation.addItem(item1)
            recommendation.addItem(item2)
            recommendation.addItem(item3)
            
            then("应该返回最高分的项目") {
                val topItem = recommendation.getTopItem()
                topItem shouldBe item2
                topItem!!.getScore().value shouldBe 0.9
            }
        }
        
        `when`("获取空推荐的最高分项目") {
            val recommendation = createTestRecommendation()
            
            then("应该返回null") {
                recommendation.getTopItem() shouldBe null
            }
        }
        
        `when`("获取前N个推荐项目") {
            val recommendation = createTestRecommendation()
            val items = listOf(
                RecommendationItem(ContentId("content-1"), RecommendationScore(0.9), "原因1"),
                RecommendationItem(ContentId("content-2"), RecommendationScore(0.8), "原因2"),
                RecommendationItem(ContentId("content-3"), RecommendationScore(0.7), "原因3"),
                RecommendationItem(ContentId("content-4"), RecommendationScore(0.6), "原因4")
            )
            
            items.forEach { recommendation.addItem(it) }
            
            then("应该返回按分数排序的前N个项目") {
                val top3 = recommendation.getTopItems(3)
                top3.size shouldBe 3
                top3[0].getScore().value shouldBe 0.9
                top3[1].getScore().value shouldBe 0.8
                top3[2].getScore().value shouldBe 0.7
            }
        }
    }
})

/**
 * 创建测试用的推荐
 */
private fun createTestRecommendation(): Recommendation {
    return Recommendation.create(
        recommendationId = RecommendationId(UUID.randomUUID()),
        userId = "user-123",
        type = RecommendationType.CONTENT_BASED
    )
}

/**
 * 创建测试用的推荐项目
 */
private fun createTestRecommendationItem(): RecommendationItem {
    return RecommendationItem(
        contentId = ContentId("content-123"),
        score = RecommendationScore(0.85),
        reason = "基于用户历史行为的内容推荐"
    )
}