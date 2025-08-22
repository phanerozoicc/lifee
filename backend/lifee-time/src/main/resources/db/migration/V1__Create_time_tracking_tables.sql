-- 时间追踪模块数据库迁移脚本
-- 创建时间追踪相关的数据库表

-- 创建团队表
CREATE TABLE teams (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id VARCHAR(36) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建团队成员表
CREATE TABLE team_members (
    id VARCHAR(36) PRIMARY KEY,
    team_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    UNIQUE KEY unique_team_member (team_id, user_id)
);

-- 创建项目表
CREATE TABLE projects (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    client_name VARCHAR(255),
    hourly_rate DECIMAL(10,2),
    budget DECIMAL(12,2),
    color VARCHAR(7),
    is_billable BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    owner_id VARCHAR(36) NOT NULL,
    team_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL
);

-- 创建项目成员表
CREATE TABLE project_members (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(50) NOT NULL,
    hourly_rate DECIMAL(10,2),
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    UNIQUE KEY unique_project_member (project_id, user_id)
);

-- 创建任务表
CREATE TABLE tasks (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    project_id VARCHAR(36) NOT NULL,
    assignee_id VARCHAR(36),
    creator_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    estimated_duration BIGINT,
    actual_duration BIGINT DEFAULT 0,
    due_date DATE,
    tags TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- 创建时间条目表
CREATE TABLE time_entries (
    id VARCHAR(36) PRIMARY KEY,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration BIGINT DEFAULT 0,
    is_billable BOOLEAN NOT NULL DEFAULT FALSE,
    hourly_rate DECIMAL(10,2),
    user_id VARCHAR(36) NOT NULL,
    project_id VARCHAR(36),
    task_id VARCHAR(36),
    tags TEXT,
    is_running BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL
);

-- 创建索引以提高查询性能

-- 团队相关索引
CREATE INDEX idx_teams_owner_id ON teams(owner_id);
CREATE INDEX idx_teams_is_active ON teams(is_active);
CREATE INDEX idx_team_members_team_id ON team_members(team_id);
CREATE INDEX idx_team_members_user_id ON team_members(user_id);

-- 项目相关索引
CREATE INDEX idx_projects_owner_id ON projects(owner_id);
CREATE INDEX idx_projects_team_id ON projects(team_id);
CREATE INDEX idx_projects_is_archived ON projects(is_archived);
CREATE INDEX idx_projects_name ON projects(name);
CREATE INDEX idx_project_members_project_id ON project_members(project_id);
CREATE INDEX idx_project_members_user_id ON project_members(user_id);

-- 任务相关索引
CREATE INDEX idx_tasks_project_id ON tasks(project_id);
CREATE INDEX idx_tasks_assignee_id ON tasks(assignee_id);
CREATE INDEX idx_tasks_creator_id ON tasks(creator_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_created_at ON tasks(created_at);

-- 时间条目相关索引
CREATE INDEX idx_time_entries_user_id ON time_entries(user_id);
CREATE INDEX idx_time_entries_project_id ON time_entries(project_id);
CREATE INDEX idx_time_entries_task_id ON time_entries(task_id);
CREATE INDEX idx_time_entries_start_time ON time_entries(start_time);
CREATE INDEX idx_time_entries_end_time ON time_entries(end_time);
CREATE INDEX idx_time_entries_is_running ON time_entries(is_running);
CREATE INDEX idx_time_entries_is_billable ON time_entries(is_billable);
CREATE INDEX idx_time_entries_user_running ON time_entries(user_id, is_running);
CREATE INDEX idx_time_entries_date_range ON time_entries(user_id, start_time, end_time);

-- 添加注释
COMMENT ON TABLE teams IS '团队表';
COMMENT ON TABLE team_members IS '团队成员表';
COMMENT ON TABLE projects IS '项目表';
COMMENT ON TABLE project_members IS '项目成员表';
COMMENT ON TABLE tasks IS '任务表';
COMMENT ON TABLE time_entries IS '时间条目表';

COMMENT ON COLUMN teams.id IS '团队ID';
COMMENT ON COLUMN teams.name IS '团队名称';
COMMENT ON COLUMN teams.description IS '团队描述';
COMMENT ON COLUMN teams.owner_id IS '团队所有者ID';
COMMENT ON COLUMN teams.is_active IS '是否激活';

COMMENT ON COLUMN team_members.id IS '团队成员ID';
COMMENT ON COLUMN team_members.team_id IS '团队ID';
COMMENT ON COLUMN team_members.user_id IS '用户ID';
COMMENT ON COLUMN team_members.role IS '角色(OWNER/ADMIN/MEMBER)';
COMMENT ON COLUMN team_members.joined_at IS '加入时间';

COMMENT ON COLUMN projects.id IS '项目ID';
COMMENT ON COLUMN projects.name IS '项目名称';
COMMENT ON COLUMN projects.description IS '项目描述';
COMMENT ON COLUMN projects.client_name IS '客户名称';
COMMENT ON COLUMN projects.hourly_rate IS '小时费率';
COMMENT ON COLUMN projects.budget IS '项目预算';
COMMENT ON COLUMN projects.color IS '项目颜色';
COMMENT ON COLUMN projects.is_billable IS '是否可计费';
COMMENT ON COLUMN projects.is_archived IS '是否已归档';
COMMENT ON COLUMN projects.owner_id IS '项目所有者ID';
COMMENT ON COLUMN projects.team_id IS '所属团队ID';

COMMENT ON COLUMN project_members.id IS '项目成员ID';
COMMENT ON COLUMN project_members.project_id IS '项目ID';
COMMENT ON COLUMN project_members.user_id IS '用户ID';
COMMENT ON COLUMN project_members.role IS '角色(OWNER/ADMIN/MEMBER)';
COMMENT ON COLUMN project_members.hourly_rate IS '成员小时费率';
COMMENT ON COLUMN project_members.joined_at IS '加入时间';

COMMENT ON COLUMN tasks.id IS '任务ID';
COMMENT ON COLUMN tasks.name IS '任务名称';
COMMENT ON COLUMN tasks.description IS '任务描述';
COMMENT ON COLUMN tasks.project_id IS '所属项目ID';
COMMENT ON COLUMN tasks.assignee_id IS '分配给用户ID';
COMMENT ON COLUMN tasks.creator_id IS '创建者ID';
COMMENT ON COLUMN tasks.status IS '任务状态(TODO/IN_PROGRESS/COMPLETED/CANCELLED)';
COMMENT ON COLUMN tasks.priority IS '优先级(LOW/MEDIUM/HIGH/URGENT)';
COMMENT ON COLUMN tasks.estimated_duration IS '预估时长(毫秒)';
COMMENT ON COLUMN tasks.actual_duration IS '实际时长(毫秒)';
COMMENT ON COLUMN tasks.due_date IS '截止日期';
COMMENT ON COLUMN tasks.tags IS '标签(JSON格式)';
COMMENT ON COLUMN tasks.completed_at IS '完成时间';

COMMENT ON COLUMN time_entries.id IS '时间条目ID';
COMMENT ON COLUMN time_entries.description IS '描述';
COMMENT ON COLUMN time_entries.start_time IS '开始时间';
COMMENT ON COLUMN time_entries.end_time IS '结束时间';
COMMENT ON COLUMN time_entries.duration IS '持续时长(毫秒)';
COMMENT ON COLUMN time_entries.is_billable IS '是否可计费';
COMMENT ON COLUMN time_entries.hourly_rate IS '小时费率';
COMMENT ON COLUMN time_entries.user_id IS '用户ID';
COMMENT ON COLUMN time_entries.project_id IS '项目ID';
COMMENT ON COLUMN time_entries.task_id IS '任务ID';
COMMENT ON COLUMN time_entries.tags IS '标签(JSON格式)';
COMMENT ON COLUMN time_entries.is_running IS '是否正在运行';