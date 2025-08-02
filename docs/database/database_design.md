# LifeE 数据库设计文档

## 概述

本文档基于领域驱动设计(DDD)的原则，为LifeE智能知识库系统设计数据库结构。采用PostgreSQL作为主数据库，Redis作为缓存，Elasticsearch作为搜索引擎。

## 技术栈

- **主数据库**: PostgreSQL 15+
- **向量数据库**: pgvector 扩展
- **缓存**: Redis 7+
- **搜索引擎**: Elasticsearch 8+
- **文件存储**: MinIO / AWS S3

## 数据库架构

### 1. 用户域 (User Domain)

#### users 表
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    avatar_url VARCHAR(500),
    bio TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    email_verified BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_status ON users(status);
```

#### teams 表
```sql
CREATE TABLE teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    avatar_url VARCHAR(500),
    settings JSONB DEFAULT '{}',
    member_limit INTEGER DEFAULT 50,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_teams_owner_id ON teams(owner_id);
CREATE INDEX idx_teams_name ON teams(name);
```

#### team_members 表
```sql
CREATE TABLE team_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) DEFAULT 'MEMBER' CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER')),
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(team_id, user_id)
);

CREATE INDEX idx_team_members_team_id ON team_members(team_id);
CREATE INDEX idx_team_members_user_id ON team_members(user_id);
```

#### roles 表
```sql
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    permissions JSONB DEFAULT '[]',
    type VARCHAR(20) DEFAULT 'CUSTOM' CHECK (type IN ('SYSTEM', 'CUSTOM')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_roles_type ON roles(type);
```

#### user_roles 表
```sql
CREATE TABLE user_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    granted_by UUID REFERENCES users(id),
    granted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(user_id, role_id)
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
```

### 2. 知识库域 (Knowledge Domain)

#### knowledge_bases 表
```sql
CREATE TABLE knowledge_bases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    team_id UUID REFERENCES teams(id) ON DELETE SET NULL,
    config JSONB DEFAULT '{}',
    embedding_model VARCHAR(100) DEFAULT 'text-embedding-ada-002',
    chunk_size INTEGER DEFAULT 1000,
    chunk_overlap INTEGER DEFAULT 200,
    visibility VARCHAR(20) DEFAULT 'PRIVATE' CHECK (visibility IN ('PRIVATE', 'TEAM', 'PUBLIC')),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
    document_count INTEGER DEFAULT 0,
    total_size BIGINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_knowledge_bases_owner_id ON knowledge_bases(owner_id);
CREATE INDEX idx_knowledge_bases_team_id ON knowledge_bases(team_id);
CREATE INDEX idx_knowledge_bases_status ON knowledge_bases(status);
CREATE INDEX idx_knowledge_bases_visibility ON knowledge_bases(visibility);
```

#### documents 表
```sql
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    knowledge_base_id UUID NOT NULL REFERENCES knowledge_bases(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(1000) NOT NULL,
    type VARCHAR(50) NOT NULL,
    mime_type VARCHAR(100),
    size BIGINT NOT NULL,
    content TEXT,
    metadata JSONB DEFAULT '{}',
    file_url VARCHAR(1000),
    hash VARCHAR(64),
    status VARCHAR(20) DEFAULT 'PROCESSING' CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED', 'DELETED')),
    chunk_count INTEGER DEFAULT 0,
    uploaded_by UUID NOT NULL REFERENCES users(id),
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_documents_knowledge_base_id ON documents(knowledge_base_id);
CREATE INDEX idx_documents_status ON documents(status);
CREATE INDEX idx_documents_type ON documents(type);
CREATE INDEX idx_documents_uploaded_by ON documents(uploaded_by);
CREATE INDEX idx_documents_hash ON documents(hash);
```

#### document_chunks 表
```sql
CREATE TABLE document_chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    embedding vector(1536), -- OpenAI embedding dimension
    metadata JSONB DEFAULT '{}',
    sequence INTEGER NOT NULL,
    start_position INTEGER,
    end_position INTEGER,
    token_count INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_document_chunks_document_id ON document_chunks(document_id);
CREATE INDEX idx_document_chunks_sequence ON document_chunks(document_id, sequence);

-- 向量相似度搜索索引
CREATE INDEX idx_document_chunks_embedding ON document_chunks USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
```

#### directory_nodes 表
```sql
CREATE TABLE directory_nodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    knowledge_base_id UUID NOT NULL REFERENCES knowledge_bases(id) ON DELETE CASCADE,
    parent_id UUID REFERENCES directory_nodes(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(1000) NOT NULL,
    type VARCHAR(20) DEFAULT 'FOLDER' CHECK (type IN ('FOLDER', 'FILE')),
    document_id UUID REFERENCES documents(id) ON DELETE SET NULL,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_directory_nodes_knowledge_base_id ON directory_nodes(knowledge_base_id);
CREATE INDEX idx_directory_nodes_parent_id ON directory_nodes(parent_id);
CREATE INDEX idx_directory_nodes_path ON directory_nodes(path);
```

### 3. 对话域 (Conversation Domain)

#### conversations 表
```sql
CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    knowledge_base_id UUID REFERENCES knowledge_bases(id) ON DELETE SET NULL,
    config JSONB DEFAULT '{}',
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ARCHIVED', 'DELETED')),
    message_count INTEGER DEFAULT 0,
    total_tokens INTEGER DEFAULT 0,
    last_message_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_conversations_user_id ON conversations(user_id);
CREATE INDEX idx_conversations_knowledge_base_id ON conversations(knowledge_base_id);
CREATE INDEX idx_conversations_status ON conversations(status);
CREATE INDEX idx_conversations_last_message_at ON conversations(last_message_at);
```

#### messages 表
```sql
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('USER', 'ASSISTANT', 'SYSTEM')),
    content TEXT NOT NULL,
    metadata JSONB DEFAULT '{}',
    token_count INTEGER,
    model_used VARCHAR(100),
    prompt_tokens INTEGER,
    completion_tokens INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_type ON messages(type);
CREATE INDEX idx_messages_created_at ON messages(created_at);
```

#### prompt_templates 表
```sql
CREATE TABLE prompt_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    template TEXT NOT NULL,
    variables JSONB DEFAULT '[]',
    category VARCHAR(50),
    tags JSONB DEFAULT '[]',
    is_public BOOLEAN DEFAULT FALSE,
    usage_count INTEGER DEFAULT 0,
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_prompt_templates_created_by ON prompt_templates(created_by);
CREATE INDEX idx_prompt_templates_category ON prompt_templates(category);
CREATE INDEX idx_prompt_templates_is_public ON prompt_templates(is_public);
```

#### rag_contexts 表
```sql
CREATE TABLE rag_contexts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    message_id UUID NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    query TEXT NOT NULL,
    relevant_chunks JSONB NOT NULL,
    relevance_scores JSONB NOT NULL,
    context_text TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_rag_contexts_conversation_id ON rag_contexts(conversation_id);
CREATE INDEX idx_rag_contexts_message_id ON rag_contexts(message_id);
```

### 4. 推荐域 (Recommendation Domain)

#### user_behaviors 表
```sql
CREATE TABLE user_behaviors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id UUID NOT NULL,
    metadata JSONB DEFAULT '{}',
    weight DECIMAL(5,4) DEFAULT 1.0,
    session_id VARCHAR(100),
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_behaviors_user_id ON user_behaviors(user_id);
CREATE INDEX idx_user_behaviors_type ON user_behaviors(type);
CREATE INDEX idx_user_behaviors_target ON user_behaviors(target_type, target_id);
CREATE INDEX idx_user_behaviors_created_at ON user_behaviors(created_at);
```

#### learning_goals 表
```sql
CREATE TABLE learning_goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    target_documents JSONB DEFAULT '[]',
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'PAUSED', 'CANCELLED')),
    progress DECIMAL(5,2) DEFAULT 0.00,
    deadline TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_learning_goals_user_id ON learning_goals(user_id);
CREATE INDEX idx_learning_goals_status ON learning_goals(status);
CREATE INDEX idx_learning_goals_deadline ON learning_goals(deadline);
```

#### recommendation_items 表
```sql
CREATE TABLE recommendation_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    score DECIMAL(5,4) NOT NULL,
    reason TEXT,
    metadata JSONB DEFAULT '{}',
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'VIEWED', 'CLICKED', 'DISMISSED')),
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recommendation_items_user_id ON recommendation_items(user_id);
CREATE INDEX idx_recommendation_items_document_id ON recommendation_items(document_id);
CREATE INDEX idx_recommendation_items_type ON recommendation_items(type);
CREATE INDEX idx_recommendation_items_score ON recommendation_items(score DESC);
CREATE INDEX idx_recommendation_items_status ON recommendation_items(status);
```

#### learning_progress 表
```sql
CREATE TABLE learning_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    completion_rate DECIMAL(5,2) DEFAULT 0.00,
    review_count INTEGER DEFAULT 0,
    last_review_at TIMESTAMP WITH TIME ZONE,
    next_review_at TIMESTAMP WITH TIME ZONE,
    forgetting_curve_params JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, document_id)
);

CREATE INDEX idx_learning_progress_user_id ON learning_progress(user_id);
CREATE INDEX idx_learning_progress_document_id ON learning_progress(document_id);
CREATE INDEX idx_learning_progress_next_review_at ON learning_progress(next_review_at);
```

### 5. 配置域 (Configuration Domain)

#### model_configs 表
```sql
CREATE TABLE model_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('LLM', 'EMBEDDING', 'RERANK')),
    provider VARCHAR(50) NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    endpoint VARCHAR(500),
    api_key_encrypted TEXT,
    parameters JSONB DEFAULT '{}',
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_model_configs_type ON model_configs(type);
CREATE INDEX idx_model_configs_provider ON model_configs(provider);
CREATE INDEX idx_model_configs_is_default ON model_configs(is_default);
CREATE INDEX idx_model_configs_is_active ON model_configs(is_active);
```

#### system_configs 表
```sql
CREATE TABLE system_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key VARCHAR(100) UNIQUE NOT NULL,
    value TEXT,
    type VARCHAR(50) DEFAULT 'STRING' CHECK (type IN ('STRING', 'INTEGER', 'BOOLEAN', 'JSON')),
    description TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    updated_by UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_system_configs_key ON system_configs(key);
CREATE INDEX idx_system_configs_type ON system_configs(type);
CREATE INDEX idx_system_configs_is_public ON system_configs(is_public);
```

#### mcp_services 表
```sql
CREATE TABLE mcp_services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    endpoint VARCHAR(500) NOT NULL,
    capabilities JSONB DEFAULT '{}',
    config JSONB DEFAULT '{}',
    status VARCHAR(20) DEFAULT 'INACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'ERROR')),
    health_check_url VARCHAR(500),
    last_health_check TIMESTAMP WITH TIME ZONE,
    registered_by UUID NOT NULL REFERENCES users(id),
    registered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_mcp_services_name ON mcp_services(name);
CREATE INDEX idx_mcp_services_status ON mcp_services(status);
CREATE INDEX idx_mcp_services_registered_by ON mcp_services(registered_by);
```

### 6. 审计和日志表

#### audit_logs 表
```sql
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id UUID,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_type, resource_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
```

#### system_events 表
```sql
CREATE TABLE system_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_data JSONB NOT NULL,
    version INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_system_events_aggregate ON system_events(aggregate_type, aggregate_id);
CREATE INDEX idx_system_events_event_type ON system_events(event_type);
CREATE INDEX idx_system_events_created_at ON system_events(created_at);
```

## 数据库优化策略

### 1. 分区策略

```sql
-- 按时间分区用户行为表
CREATE TABLE user_behaviors_y2024m01 PARTITION OF user_behaviors
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

-- 按时间分区审计日志表
CREATE TABLE audit_logs_y2024m01 PARTITION OF audit_logs
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
```

### 2. 性能优化

```sql
-- 复合索引优化
CREATE INDEX idx_documents_kb_status_type ON documents(knowledge_base_id, status, type);
CREATE INDEX idx_messages_conv_created ON messages(conversation_id, created_at DESC);
CREATE INDEX idx_recommendations_user_score ON recommendation_items(user_id, score DESC, status);

-- 部分索引
CREATE INDEX idx_conversations_active ON conversations(user_id, last_message_at DESC) 
WHERE status = 'ACTIVE';

CREATE INDEX idx_documents_processing ON documents(knowledge_base_id, uploaded_at) 
WHERE status = 'PROCESSING';
```

### 3. 数据清理策略

```sql
-- 定期清理过期的推荐项
DELETE FROM recommendation_items 
WHERE expires_at < CURRENT_TIMESTAMP - INTERVAL '30 days';

-- 归档旧的用户行为数据
INSERT INTO user_behaviors_archive 
SELECT * FROM user_behaviors 
WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '1 year';
```

## Redis 缓存设计

### 1. 缓存键命名规范

```
lifee:user:{user_id}:profile
lifee:kb:{kb_id}:config
lifee:conversation:{conv_id}:messages
lifee:document:{doc_id}:chunks
lifee:recommendation:{user_id}:items
```

### 2. 缓存策略

- **用户会话**: TTL 24小时
- **知识库配置**: TTL 1小时
- **文档块**: TTL 30分钟
- **推荐结果**: TTL 15分钟
- **搜索结果**: TTL 5分钟

## Elasticsearch 索引设计

### 1. 文档索引

```json
{
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "knowledge_base_id": { "type": "keyword" },
      "name": { 
        "type": "text", 
        "analyzer": "ik_max_word",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "content": { 
        "type": "text", 
        "analyzer": "ik_max_word" 
      },
      "type": { "type": "keyword" },
      "tags": { "type": "keyword" },
      "created_at": { "type": "date" },
      "updated_at": { "type": "date" }
    }
  }
}
```

### 2. 对话索引

```json
{
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "user_id": { "type": "keyword" },
      "title": { 
        "type": "text", 
        "analyzer": "ik_max_word" 
      },
      "messages": {
        "type": "nested",
        "properties": {
          "type": { "type": "keyword" },
          "content": { 
            "type": "text", 
            "analyzer": "ik_max_word" 
          },
          "created_at": { "type": "date" }
        }
      },
      "created_at": { "type": "date" }
    }
  }
}
```

## 数据迁移和版本控制

### 1. Flyway 迁移脚本

```sql
-- V1__Initial_schema.sql
-- V2__Add_vector_support.sql
-- V3__Add_team_support.sql
-- V4__Add_mcp_services.sql
```

### 2. 数据备份策略

- **全量备份**: 每日凌晨2点
- **增量备份**: 每4小时
- **WAL归档**: 实时
- **备份保留**: 30天

## 监控和告警

### 1. 性能监控

- 慢查询监控 (>1秒)
- 连接池使用率
- 缓存命中率
- 磁盘使用率

### 2. 业务监控

- 文档处理失败率
- 向量搜索响应时间
- 用户活跃度
- 推荐系统准确率

## 安全考虑

### 1. 数据加密

- 敏感字段加密存储
- API密钥加密
- 传输层TLS加密

### 2. 访问控制

- 行级安全策略
- 角色权限控制
- 审计日志记录

### 3. 数据脱敏

```sql
-- 开发环境数据脱敏
UPDATE users SET 
  email = CONCAT('user_', id, '@example.com'),
  password_hash = 'dummy_hash'
WHERE environment = 'development';
```