package com.lifee.time.domain

import com.lifee.common.domain.EventSourcedAggregateRoot
import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.events.*
import com.lifee.user.domain.UserId
import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Instant

/**
 * 团队聚合根
 */
class Team private constructor(
    val teamId: TeamId,
    var name: String,
    var description: String?,
    val ownerId: UserId,
    private val _members: MutableMap<UserId, TeamMember> = mutableMapOf(),
    var isActive: Boolean = true,
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
) : EventSourcedAggregateRoot() {
    
    @get:JsonIgnore
    val members: Map<UserId, TeamMember> get() = _members.toMap()
    
    @get:JsonIgnore
    val activeMembers: Map<UserId, TeamMember> get() = _members.filter { it.value.isActive }
    
    @get:JsonIgnore
    val memberCount: Int get() = activeMembers.size
    
    companion object {
        /**
         * 创建新团队
         */
        fun create(
            name: String,
            description: String?,
            ownerId: UserId
        ): Team {
            val teamId = TeamId.generate()
            val team = Team(
                teamId = teamId,
                name = name,
                description = description,
                ownerId = ownerId
            )
            
            // 添加所有者为团队成员
            val ownerMember = TeamMember.create(ownerId, TeamRole.OWNER)
            team._members[ownerId] = ownerMember
            
            team.addEvent(
                TeamCreatedEvent(
                    teamId = teamId,
                    name = name,
                    description = description,
                    ownerId = ownerId
                )
            )
            
            return team
        }
        
        /**
         * 从事件重建团队
         */
        fun reconstruct(events: List<DomainEvent>): Team {
            require(events.isNotEmpty()) { "事件列表不能为空" }
            
            val firstEvent = events.first() as TeamCreatedEvent
            val team = Team(
                teamId = firstEvent.teamId,
                name = firstEvent.name,
                description = firstEvent.description,
                ownerId = firstEvent.ownerId,
                createdAt = firstEvent.occurredAt
            )
            
            events.forEach { event ->
                team.applyEvent(event)
            }
            
            return team
        }
    }
    
    /**
     * 更新团队信息
     */
    fun updateInfo(name: String, description: String?) {
        require(name.isNotBlank()) { "团队名称不能为空" }
        
        this.name = name
        this.description = description
        this.updatedAt = Instant.now()
        
        addEvent(
            TeamUpdatedEvent(
                teamId = teamId,
                name = name,
                description = description
            )
        )
    }
    
    /**
     * 添加团队成员
     */
    fun addMember(userId: UserId, role: TeamRole, invitedBy: UserId) {
        require(isActive) { "团队已被停用" }
        require(!_members.containsKey(userId)) { "用户已是团队成员" }
        require(canManageMembers(invitedBy)) { "没有权限添加成员" }
        
        val member = TeamMember.create(userId, role)
        _members[userId] = member
        this.updatedAt = Instant.now()
        
        addEvent(
            TeamMemberAddedEvent(
                teamId = teamId,
                userId = userId,
                role = role,
                invitedBy = invitedBy
            )
        )
    }
    
    /**
     * 移除团队成员
     */
    fun removeMember(userId: UserId, removedBy: UserId) {
        require(_members.containsKey(userId)) { "用户不是团队成员" }
        require(userId != ownerId) { "不能移除团队所有者" }
        require(canRemoveMembers(removedBy)) { "没有权限移除成员" }
        
        _members.remove(userId)
        this.updatedAt = Instant.now()
        
        addEvent(
            TeamMemberRemovedEvent(
                teamId = teamId,
                userId = userId,
                removedBy = removedBy
            )
        )
    }
    
    /**
     * 更新成员角色
     */
    fun updateMemberRole(userId: UserId, newRole: TeamRole, updatedBy: UserId) {
        require(_members.containsKey(userId)) { "用户不是团队成员" }
        require(userId != ownerId) { "不能修改团队所有者角色" }
        require(canModifyMemberRoles(updatedBy)) { "没有权限修改成员角色" }
        
        val member = _members[userId]!!
        val oldRole = member.role
        _members[userId] = member.withRole(newRole)
        this.updatedAt = Instant.now()
        
        addEvent(
            TeamMemberRoleUpdatedEvent(
                teamId = teamId,
                userId = userId,
                oldRole = oldRole,
                newRole = newRole,
                updatedBy = updatedBy
            )
        )
    }
    
    /**
     * 停用团队
     */
    fun deactivate(deactivatedBy: UserId) {
        require(isActive) { "团队已被停用" }
        require(deactivatedBy == ownerId) { "只有团队所有者可以停用团队" }
        
        this.isActive = false
        this.updatedAt = Instant.now()
        
        addEvent(
            TeamDeactivatedEvent(
                teamId = teamId,
                deactivatedBy = deactivatedBy
            )
        )
    }
    
    /**
     * 激活团队
     */
    fun activate(activatedBy: UserId) {
        require(!isActive) { "团队已是激活状态" }
        require(activatedBy == ownerId) { "只有团队所有者可以激活团队" }
        
        this.isActive = true
        this.updatedAt = Instant.now()
        
        addEvent(
            TeamActivatedEvent(
                teamId = teamId,
                activatedBy = activatedBy
            )
        )
    }
    
    /**
     * 检查用户是否是团队成员
     */
    fun isMember(userId: UserId): Boolean {
        return _members.containsKey(userId) && _members[userId]?.isActive == true
    }
    
    /**
     * 获取用户在团队中的角色
     */
    fun getMemberRole(userId: UserId): TeamRole? {
        return _members[userId]?.role
    }
    
    /**
     * 检查用户是否可以管理团队成员
     */
    fun canManageMembers(userId: UserId): Boolean {
        return _members[userId]?.canManageMembers() == true
    }
    
    /**
     * 检查用户是否可以管理项目
     */
    fun canManageProjects(userId: UserId): Boolean {
        return _members[userId]?.canManageProjects() == true
    }
    
    /**
     * 检查用户是否可以查看团队报告
     */
    fun canViewReports(userId: UserId): Boolean {
        return _members[userId]?.canViewReports() == true
    }
    
    /**
     * 检查用户是否可以移除成员
     */
    fun canRemoveMembers(userId: UserId): Boolean {
        return _members[userId]?.canRemoveMembers() == true
    }
    
    /**
     * 检查用户是否可以修改成员角色
     */
    fun canModifyMemberRoles(userId: UserId): Boolean {
        return _members[userId]?.canModifyMemberRoles() == true
    }
    
    override fun applyEvent(event: DomainEvent) {
        when (event) {
            is TeamCreatedEvent -> {
                val ownerMember = TeamMember.create(event.ownerId, TeamRole.OWNER, event.occurredAt)
                _members[event.ownerId] = ownerMember
            }
            is TeamUpdatedEvent -> {
                // 信息已在方法中更新
            }
            is TeamMemberAddedEvent -> {
                val member = TeamMember.create(event.userId, event.role, event.occurredAt)
                _members[event.userId] = member
            }
            is TeamMemberRemovedEvent -> {
                _members.remove(event.userId)
            }
            is TeamMemberRoleUpdatedEvent -> {
                val member = _members[event.userId]
                if (member != null) {
                    _members[event.userId] = member.withRole(event.newRole)
                }
            }
            is TeamDeactivatedEvent -> {
                this.isActive = false
            }
            is TeamActivatedEvent -> {
                this.isActive = true
            }
        }
    }
    
    override fun serializeState(): String {
        // 实现状态序列化逻辑
        return ""
    }
    
    override fun deserializeState(state: String) {
        // 实现状态反序列化逻辑
    }
}