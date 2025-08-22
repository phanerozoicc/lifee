package com.lifee.time.infrastructure.event

import com.lifee.time.domain.event.*
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory

/**
 * 时间追踪事件处理器
 */
@Component
@Transactional
class TimeTrackingEventHandler {
    
    private val logger = LoggerFactory.getLogger(TimeTrackingEventHandler::class.java)
    
    /**
     * 处理时间条目开始事件
     */
    @EventListener
    fun handleTimeEntryStarted(event: TimeEntryStarted) {
        logger.info("时间条目开始: 用户={}, 项目={}, 任务={}", 
            event.userId, event.projectId, event.taskId)
        
        // 可以在这里添加业务逻辑，如：
        // - 发送通知
        // - 更新统计数据
        // - 记录审计日志
        // - 触发其他业务流程
    }
    
    /**
     * 处理时间条目停止事件
     */
    @EventListener
    fun handleTimeEntryStopped(event: TimeEntryStopped) {
        logger.info("时间条目停止: ID={}, 持续时间={}, 用户={}", 
            event.timeEntryId, event.duration, event.userId)
        
        // 可以在这里添加业务逻辑，如：
        // - 计算项目进度
        // - 更新任务状态
        // - 生成时间报告
        // - 计算收入
    }
    
    /**
     * 处理时间条目暂停事件
     */
    @EventListener
    fun handleTimeEntryPaused(event: TimeEntryPaused) {
        logger.info("时间条目暂停: ID={}, 用户={}", 
            event.timeEntryId, event.userId)
    }
    
    /**
     * 处理时间条目恢复事件
     */
    @EventListener
    fun handleTimeEntryResumed(event: TimeEntryResumed) {
        logger.info("时间条目恢复: ID={}, 用户={}", 
            event.timeEntryId, event.userId)
    }
    
    /**
     * 处理时间条目更新事件
     */
    @EventListener
    fun handleTimeEntryUpdated(event: TimeEntryUpdated) {
        logger.info("时间条目更新: ID={}, 用户={}", 
            event.timeEntryId, event.userId)
    }
    
    /**
     * 处理时间条目删除事件
     */
    @EventListener
    fun handleTimeEntryDeleted(event: TimeEntryDeleted) {
        logger.info("时间条目删除: ID={}, 用户={}", 
            event.timeEntryId, event.userId)
        
        // 可以在这里添加业务逻辑，如：
        // - 更新项目统计
        // - 重新计算收入
        // - 记录删除日志
    }
    
    /**
     * 处理项目创建事件
     */
    @EventListener
    fun handleProjectCreated(event: ProjectCreated) {
        logger.info("项目创建: ID={}, 名称={}, 所有者={}", 
            event.projectId, event.name, event.ownerId)
        
        // 可以在这里添加业务逻辑，如：
        // - 发送欢迎邮件
        // - 创建默认任务
        // - 设置项目模板
        // - 初始化项目统计
    }
    
    /**
     * 处理项目更新事件
     */
    @EventListener
    fun handleProjectUpdated(event: ProjectUpdated) {
        logger.info("项目更新: ID={}, 用户={}", 
            event.projectId, event.userId)
    }
    
    /**
     * 处理项目归档事件
     */
    @EventListener
    fun handleProjectArchived(event: ProjectArchived) {
        logger.info("项目归档: ID={}, 用户={}", 
            event.projectId, event.userId)
        
        // 可以在这里添加业务逻辑，如：
        // - 停止所有运行中的时间条目
        // - 生成项目总结报告
        // - 通知项目成员
    }
    
    /**
     * 处理项目恢复事件
     */
    @EventListener
    fun handleProjectRestored(event: ProjectRestored) {
        logger.info("项目恢复: ID={}, 用户={}", 
            event.projectId, event.userId)
    }
    
    /**
     * 处理项目删除事件
     */
    @EventListener
    fun handleProjectDeleted(event: ProjectDeleted) {
        logger.info("项目删除: ID={}, 用户={}", 
            event.projectId, event.userId)
        
        // 可以在这里添加业务逻辑，如：
        // - 清理相关数据
        // - 通知项目成员
        // - 备份项目数据
    }
    
    /**
     * 处理项目成员添加事件
     */
    @EventListener
    fun handleProjectMemberAdded(event: ProjectMemberAdded) {
        logger.info("项目成员添加: 项目={}, 用户={}, 角色={}", 
            event.projectId, event.userId, event.role)
        
        // 可以在这里添加业务逻辑，如：
        // - 发送邀请邮件
        // - 设置权限
        // - 创建欢迎任务
    }
    
    /**
     * 处理项目成员移除事件
     */
    @EventListener
    fun handleProjectMemberRemoved(event: ProjectMemberRemoved) {
        logger.info("项目成员移除: 项目={}, 用户={}", 
            event.projectId, event.userId)
        
        // 可以在这里添加业务逻辑，如：
        // - 停止用户的时间追踪
        // - 重新分配任务
        // - 发送告别邮件
    }
    
    /**
     * 处理任务创建事件
     */
    @EventListener
    fun handleTaskCreated(event: TaskCreated) {
        logger.info("任务创建: ID={}, 名称={}, 项目={}, 创建者={}", 
            event.taskId, event.name, event.projectId, event.creatorId)
        
        // 可以在这里添加业务逻辑，如：
        // - 发送任务通知
        // - 更新项目进度
        // - 设置任务提醒
    }
    
    /**
     * 处理任务更新事件
     */
    @EventListener
    fun handleTaskUpdated(event: TaskUpdated) {
        logger.info("任务更新: ID={}, 用户={}", 
            event.taskId, event.userId)
    }
    
    /**
     * 处理任务分配事件
     */
    @EventListener
    fun handleTaskAssigned(event: TaskAssigned) {
        logger.info("任务分配: ID={}, 分配给={}, 分配者={}", 
            event.taskId, event.assigneeId, event.assignerId)
        
        // 可以在这里添加业务逻辑，如：
        // - 发送分配通知
        // - 更新工作负载
        // - 设置截止日期提醒
    }
    
    /**
     * 处理任务取消分配事件
     */
    @EventListener
    fun handleTaskUnassigned(event: TaskUnassigned) {
        logger.info("任务取消分配: ID={}, 原分配者={}, 操作者={}", 
            event.taskId, event.previousAssigneeId, event.userId)
    }
    
    /**
     * 处理任务开始事件
     */
    @EventListener
    fun handleTaskStarted(event: TaskStarted) {
        logger.info("任务开始: ID={}, 用户={}", 
            event.taskId, event.userId)
        
        // 可以在这里添加业务逻辑，如：
        // - 开始时间追踪
        // - 更新任务状态
        // - 发送开始通知
    }
    
    /**
     * 处理任务完成事件
     */
    @EventListener
    fun handleTaskCompleted(event: TaskCompleted) {
        logger.info("任务完成: ID={}, 用户={}", 
            event.taskId, event.userId)
        
        // 可以在这里添加业务逻辑，如：
        // - 停止时间追踪
        // - 更新项目进度
        // - 发送完成通知
        // - 计算任务统计
    }
    
    /**
     * 处理任务取消事件
     */
    @EventListener
    fun handleTaskCancelled(event: TaskCancelled) {
        logger.info("任务取消: ID={}, 用户={}", 
            event.taskId, event.userId)
    }
    
    /**
     * 处理任务删除事件
     */
    @EventListener
    fun handleTaskDeleted(event: TaskDeleted) {
        logger.info("任务删除: ID={}, 用户={}", 
            event.taskId, event.userId)
        
        // 可以在这里添加业务逻辑，如：
        // - 停止相关时间追踪
        // - 更新项目统计
        // - 清理相关数据
    }
    
    /**
     * 处理团队创建事件
     */
    @EventListener
    fun handleTeamCreated(event: TeamCreated) {
        logger.info("团队创建: ID={}, 名称={}, 所有者={}", 
            event.teamId, event.name, event.ownerId)
        
        // 可以在这里添加业务逻辑，如：
        // - 初始化团队设置
        // - 创建默认角色
        // - 发送欢迎消息
    }
    
    /**
     * 处理团队更新事件
     */
    @EventListener
    fun handleTeamUpdated(event: TeamUpdated) {
        logger.info("团队更新: ID={}, 用户={}", 
            event.teamId, event.userId)
    }
    
    /**
     * 处理团队成员添加事件
     */
    @EventListener
    fun handleTeamMemberAdded(event: TeamMemberAdded) {
        logger.info("团队成员添加: 团队={}, 用户={}, 角色={}", 
            event.teamId, event.userId, event.role)
        
        // 可以在这里添加业务逻辑，如：
        // - 发送邀请邮件
        // - 设置权限
        // - 更新团队统计
    }
    
    /**
     * 处理团队成员移除事件
     */
    @EventListener
    fun handleTeamMemberRemoved(event: TeamMemberRemoved) {
        logger.info("团队成员移除: 团队={}, 用户={}", 
            event.teamId, event.userId)
        
        // 可以在这里添加业务逻辑，如：
        // - 重新分配任务
        // - 更新权限
        // - 发送告别消息
    }
    
    /**
     * 处理团队停用事件
     */
    @EventListener
    fun handleTeamDeactivated(event: TeamDeactivated) {
        logger.info("团队停用: ID={}, 用户={}", 
            event.teamId, event.userId)
        
        // 可以在这里添加业务逻辑，如：
        // - 停止团队项目
        // - 通知团队成员
        // - 归档团队数据
    }
    
    /**
     * 处理团队激活事件
     */
    @EventListener
    fun handleTeamActivated(event: TeamActivated) {
        logger.info("团队激活: ID={}, 用户={}", 
            event.teamId, event.userId)
    }
    
    /**
     * 处理团队删除事件
     */
    @EventListener
    fun handleTeamDeleted(event: TeamDeleted) {
        logger.info("团队删除: ID={}, 用户={}", 
            event.teamId, event.userId)
        
        // 可以在这里添加业务逻辑，如：
        // - 清理团队数据
        // - 通知成员
        // - 备份重要信息
    }
}