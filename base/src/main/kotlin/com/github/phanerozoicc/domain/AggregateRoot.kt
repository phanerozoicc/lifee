package com.github.phanerozoicc.domain

import com.github.phanerozoicc.utils.SpringContextUtil
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.BeanFactoryUtils
import java.util.*

/**
 * 聚合根基类
 * 提供领域事件管理功能
 */
abstract class AggregateRoot<T>(
    val id: T
) {

    companion object {
        fun publisher(): DomainEventPublisher {
            return SpringContextUtil.getBean(DomainEventPublisher::class.java)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AggregateRoot<*>) return false
        return id == other.id
    }
    
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
    
    override fun toString(): String {
        return "${this::class.simpleName}(id=$id)"
    }
}