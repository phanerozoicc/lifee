package com.lifee.recommendation.domain.exceptions

import com.lifee.common.exceptions.DomainException

/**
 * 无效推荐异常
 */
class InvalidRecommendationException(
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause) {
    
    companion object {
        fun itemCountExceeded(maxCount: Int): InvalidRecommendationException {
            return InvalidRecommendationException("推荐项数量不能超过${maxCount}个")
        }
        
        fun lowQualityScore(minScore: Double): InvalidRecommendationException {
            return InvalidRecommendationException("推荐项质量分数不能低于${minScore}")
        }
        
        fun invalidReason(reason: String): InvalidRecommendationException {
            return InvalidRecommendationException("推荐原因无效: $reason")
        }
        
        fun duplicateItem(contentId: String): InvalidRecommendationException {
            return InvalidRecommendationException("推荐项已存在: $contentId")
        }
        
        fun invalidGenerationLimit(limit: Int, maxLimit: Int): InvalidRecommendationException {
            return InvalidRecommendationException("推荐生成数量($limit)不能超过最大限制($maxLimit)")
        }
        
        fun invalidScoreRange(score: Double, minScore: Double, maxScore: Double): InvalidRecommendationException {
            return InvalidRecommendationException("推荐分数($score)必须在${minScore}到${maxScore}之间")
        }
        
        fun excessiveScoreChange(change: Double, maxChange: Double): InvalidRecommendationException {
            return InvalidRecommendationException("分数变化幅度($change)超过最大允许值($maxChange)")
        }
        
        fun batchSizeExceeded(size: Int, maxSize: Int): InvalidRecommendationException {
            return InvalidRecommendationException("批量操作数量($size)不能超过最大限制($maxSize)")
        }
        
        fun mixedTypesExceeded(maxTypes: Int): InvalidRecommendationException {
            return InvalidRecommendationException("推荐中已包含${maxTypes}种不同类型，不能再添加新类型")
        }
        
        fun invalidContentId(contentId: String): InvalidRecommendationException {
            return InvalidRecommendationException("内容ID格式无效: $contentId")
        }
        
        fun reasonTooShort(minLength: Int): InvalidRecommendationException {
            return InvalidRecommendationException("高质量推荐项必须提供至少${minLength}个字符的推荐原因")
        }
        
        fun reasonTooLong(maxLength: Int): InvalidRecommendationException {
            return InvalidRecommendationException("推荐原因不能超过${maxLength}个字符")
        }
        
        fun sensitiveContent(): InvalidRecommendationException {
            return InvalidRecommendationException("推荐原因包含敏感内容")
        }
        
        fun excessiveRepeatedChars(): InvalidRecommendationException {
            return InvalidRecommendationException("推荐原因包含过多重复字符")
        }
        
        fun invalidReasonContent(): InvalidRecommendationException {
            return InvalidRecommendationException("推荐原因缺少有效的推荐信息")
        }
    }
}