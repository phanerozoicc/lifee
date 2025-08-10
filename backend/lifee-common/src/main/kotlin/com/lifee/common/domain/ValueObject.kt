package com.lifee.common.domain

/**
 * 值对象基类
 * 所有值对象都应该继承此类
 * 值对象是不可变的，通过值而不是标识符来区分
 */
abstract class ValueObject {
    
    /**
     * 获取用于相等性比较的属性
     * 子类应该重写此方法返回所有用于比较的属性
     * 
     * @return 用于相等性比较的属性列表
     */
    protected abstract fun getEqualityComponents(): List<Any?>
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        
        other as ValueObject
        return getEqualityComponents() == other.getEqualityComponents()
    }
    
    override fun hashCode(): Int {
        return getEqualityComponents().hashCode()
    }
    
    override fun toString(): String {
        val components = getEqualityComponents().joinToString(", ")
        return "${this::class.simpleName}($components)"
    }
}