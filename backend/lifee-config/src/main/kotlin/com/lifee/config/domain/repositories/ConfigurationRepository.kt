package com.lifee.config.domain.repositories

import com.lifee.config.domain.aggregates.Configuration
import com.lifee.config.domain.valueobjects.ConfigId
import com.lifee.config.domain.valueobjects.Environment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * 配置仓储接口
 */
interface ConfigurationRepository {
    
    /**
     * 保存配置
     */
    suspend fun save(configuration: Configuration): Configuration
    
    /**
     * 根据ID查找配置
     */
    suspend fun findById(id: ConfigId): Configuration?
    
    /**
     * 根据命名空间和环境查找配置
     */
    suspend fun findByNamespaceAndEnvironment(namespace: String, environment: Environment): Configuration?
    
    /**
     * 根据命名空间查找所有环境的配置
     */
    suspend fun findByNamespace(namespace: String): List<Configuration>
    
    /**
     * 根据环境查找所有配置
     */
    suspend fun findByEnvironment(environment: Environment): List<Configuration>
    
    /**
     * 分页查询配置
     */
    suspend fun findAll(pageable: Pageable): Page<Configuration>
    
    /**
     * 根据命名空间分页查询配置
     */
    suspend fun findByNamespace(namespace: String, pageable: Pageable): Page<Configuration>
    
    /**
     * 根据环境分页查询配置
     */
    suspend fun findByEnvironment(environment: Environment, pageable: Pageable): Page<Configuration>
    
    /**
     * 删除配置
     */
    suspend fun delete(configuration: Configuration)
    
    /**
     * 根据ID删除配置
     */
    suspend fun deleteById(id: ConfigId)
    
    /**
     * 根据命名空间和环境删除配置
     */
    suspend fun deleteByNamespaceAndEnvironment(namespace: String, environment: Environment)
    
    /**
     * 检查配置是否存在
     */
    suspend fun existsById(id: ConfigId): Boolean
    
    /**
     * 检查命名空间和环境的配置是否存在
     */
    suspend fun existsByNamespaceAndEnvironment(namespace: String, environment: Environment): Boolean
    
    /**
     * 统计配置数量
     */
    suspend fun count(): Long
    
    /**
     * 根据命名空间统计配置数量
     */
    suspend fun countByNamespace(namespace: String): Long
    
    /**
     * 根据环境统计配置数量
     */
    suspend fun countByEnvironment(environment: Environment): Long
    
    /**
     * 获取所有命名空间
     */
    suspend fun findAllNamespaces(): List<String>
    
    /**
     * 获取指定环境的所有命名空间
     */
    suspend fun findNamespacesByEnvironment(environment: Environment): List<String>
}