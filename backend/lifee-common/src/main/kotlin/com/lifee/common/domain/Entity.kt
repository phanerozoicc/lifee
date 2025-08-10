package com.lifee.common.domain

/**
 * 实体基类
 * 所有实体都应该继承此类
 * 实体具有唯一标识符，但不是聚合根
 * 
 * @param ID 实体标识符类型
 */
abstract class Entity<ID>(
    private val _id: ID
) {
    
    /**
     * 获取实体标识符
     * 
     * @return 实体ID
     */
    fun getId(): ID = _id
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Entity<*>) return false
        return _id == other._id
    }
    
    override fun hashCode(): Int {
        return _id?.hashCode() ?: 0
    }
    
    override fun toString(): String {
        return "${this::class.simpleName}(id=$_id)"
    }
}