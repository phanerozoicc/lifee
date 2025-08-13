package com.github.phanerozoicc.base.exception

/**
 * 业务规则异常
 * 当违反业务规则时抛出此异常
 */
class BusinessRuleException(
    message: String,
    private val ruleCode: String? = null,
    cause: Throwable? = null
) : DomainException(message, cause) {
    
    companion object {
        /**
         * 创建业务规则异常
         */
        fun of(message: String, ruleCode: String? = null): BusinessRuleException {
            return BusinessRuleException(message, ruleCode)
        }
        
        /**
         * 当条件为真时抛出异常
         */
        fun throwIf(condition: Boolean, message: String, ruleCode: String? = null) {
            if (condition) {
                throw BusinessRuleException(message, ruleCode)
            }
        }
        
        /**
         * 当条件为假时抛出异常
         */
        fun throwUnless(condition: Boolean, message: String, ruleCode: String? = null) {
            if (!condition) {
                throw BusinessRuleException(message, ruleCode)
            }
        }
    }
}