package com.lifee.recommendation.domain.exceptions

import com.lifee.common.exceptions.DomainException
import com.lifee.recommendation.domain.valueobjects.ContentId
import com.lifee.recommendation.domain.valueobjects.RecommendationId
import com.lifee.recommendation.domain.valueobjects.UserId

/**
 * 推荐未找到异常
 */
class RecommendationNotFoundException(
    recommendationId: RecommendationId
) : DomainException("推荐未找到: $recommendationId")

/**
 * 用户推荐未找到异常
 */
class UserRecommendationNotFoundException(
    userId: UserId
) : DomainException("用户推荐未找到: $userId")

/**
 * 推荐项已存在异常
 */
class RecommendationItemAlreadyExistsException(
    contentId: ContentId
) : DomainException("推荐项已存在: $contentId")

/**
 * 推荐项未找到异常
 */
class RecommendationItemNotFoundException(
    contentId: ContentId
) : DomainException("推荐项未找到: $contentId")

/**
 * 推荐容量超限异常
 */
class RecommendationCapacityExceededException(
    currentCount: Int,
    maxCount: Int
) : DomainException("推荐容量超限: 当前 $currentCount, 最大 $maxCount")

/**
 * 无效推荐分数异常
 */
class InvalidRecommendationScoreException(
    score: Double
) : DomainException("无效的推荐分数: $score, 必须在0.0到1.0之间")

/**
 * 推荐算法不支持异常
 */
class UnsupportedRecommendationAlgorithmException(
    algorithm: String
) : DomainException("不支持的推荐算法: $algorithm")

/**
 * 推荐数据不足异常
 */
class InsufficientRecommendationDataException(
    reason: String
) : DomainException("推荐数据不足: $reason")