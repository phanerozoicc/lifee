package com.github.phanerozoicc.user.domain.model

/**
 * 用户ID值对象
 * 使用自增ID字符串格式（如 USER_000001）
 */
data class UserId(
    val value: String
) {
    companion object {
        private const val PREFIX = "U"
        private const val ID_FORMAT = "%08d"
        
        /**
         * 从字符串创建用户ID
         * @param value 用户ID字符串值
         * @return UserId实例
         * @throws IllegalArgumentException 如果格式不正确
         */
        fun of(value: String): UserId {
            require(value.isNotBlank()) { "用户ID不能为空" }
            require(value.startsWith(PREFIX)) { "用户ID必须以${PREFIX}开头" }
            require(value.length == PREFIX.length + 6) { "用户ID格式不正确，应为${PREFIX}XXXXXX" }
            return UserId(value)
        }
    }
    

    /**
     * 转换为字符串
     */
    override fun toString(): String = value
    
    init {
        require(value.isNotBlank()) { "用户ID不能为空" }
        require(value.startsWith(PREFIX)) { "用户ID必须以${PREFIX}开头" }
    }
}