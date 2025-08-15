-- 创建事件存储表
CREATE TABLE IF NOT EXISTS event_store (
    id BIGSERIAL PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB NOT NULL,
    version BIGINT NOT NULL,
    occurred_on TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 确保同一聚合根的版本唯一性
    CONSTRAINT uk_event_store_aggregate_version UNIQUE (aggregate_id, version)
);

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_event_store_aggregate_id ON event_store (aggregate_id);
CREATE INDEX IF NOT EXISTS idx_event_store_aggregate_type ON event_store (aggregate_type);
CREATE INDEX IF NOT EXISTS idx_event_store_event_type ON event_store (event_type);
CREATE INDEX IF NOT EXISTS idx_event_store_occurred_on ON event_store (occurred_on);
CREATE INDEX IF NOT EXISTS idx_event_store_version ON event_store (aggregate_id, version);

-- 创建聚合根快照表
CREATE TABLE IF NOT EXISTS aggregate_snapshots (
    id BIGSERIAL PRIMARY KEY,
    snapshot_id UUID NOT NULL UNIQUE,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL,
    snapshot_data JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 确保同一聚合根的同一版本只有一个快照
    CONSTRAINT uk_aggregate_snapshots_aggregate_version UNIQUE (aggregate_id, version)
);

-- 创建快照表索引
CREATE INDEX IF NOT EXISTS idx_aggregate_snapshots_aggregate_id ON aggregate_snapshots (aggregate_id);
CREATE INDEX IF NOT EXISTS idx_aggregate_snapshots_aggregate_type ON aggregate_snapshots (aggregate_type);
CREATE INDEX IF NOT EXISTS idx_aggregate_snapshots_version ON aggregate_snapshots (aggregate_id, version DESC);
CREATE INDEX IF NOT EXISTS idx_aggregate_snapshots_created_at ON aggregate_snapshots (created_at);

-- 创建Saga状态表（用于长流程协调）
CREATE TABLE IF NOT EXISTS saga_instances (
    id BIGSERIAL PRIMARY KEY,
    saga_id UUID NOT NULL UNIQUE,
    saga_type VARCHAR(100) NOT NULL,
    correlation_id VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'STARTED',
    current_step VARCHAR(100),
    saga_data JSONB NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT chk_saga_status CHECK (status IN ('STARTED', 'RUNNING', 'COMPLETED', 'FAILED', 'COMPENSATING', 'COMPENSATED'))
);

-- 创建Saga表索引
CREATE INDEX IF NOT EXISTS idx_saga_instances_saga_type ON saga_instances (saga_type);
CREATE INDEX IF NOT EXISTS idx_saga_instances_correlation_id ON saga_instances (correlation_id);
CREATE INDEX IF NOT EXISTS idx_saga_instances_status ON saga_instances (status);
CREATE INDEX IF NOT EXISTS idx_saga_instances_started_at ON saga_instances (started_at);

-- 创建Saga步骤表（记录Saga执行的每个步骤）
CREATE TABLE IF NOT EXISTS saga_steps (
    id BIGSERIAL PRIMARY KEY,
    saga_id UUID NOT NULL REFERENCES saga_instances(saga_id) ON DELETE CASCADE,
    step_name VARCHAR(100) NOT NULL,
    step_type VARCHAR(50) NOT NULL, -- COMMAND, COMPENSATION
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    command_data JSONB,
    result_data JSONB,
    error_message TEXT,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_saga_step_type CHECK (step_type IN ('COMMAND', 'COMPENSATION')),
    CONSTRAINT chk_saga_step_status CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'SKIPPED'))
);

-- 创建Saga步骤表索引
CREATE INDEX IF NOT EXISTS idx_saga_steps_saga_id ON saga_steps (saga_id);
CREATE INDEX IF NOT EXISTS idx_saga_steps_status ON saga_steps (status);
CREATE INDEX IF NOT EXISTS idx_saga_steps_step_name ON saga_steps (step_name);

-- 创建事件发布状态表（确保事件至少发布一次）
CREATE TABLE IF NOT EXISTS event_publications (
    id BIGSERIAL PRIMARY KEY,
    event_id UUID NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    publication_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    publication_attempts INTEGER NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMP WITH TIME ZONE,
    next_attempt_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_publication_status CHECK (publication_status IN ('PENDING', 'PUBLISHED', 'FAILED', 'ABANDONED'))
);

-- 创建事件发布表索引
CREATE INDEX IF NOT EXISTS idx_event_publications_event_id ON event_publications (event_id);
CREATE INDEX IF NOT EXISTS idx_event_publications_status ON event_publications (publication_status);
CREATE INDEX IF NOT EXISTS idx_event_publications_next_attempt ON event_publications (next_attempt_at) WHERE publication_status = 'PENDING';

-- 添加表注释
COMMENT ON TABLE event_store IS '事件存储表，记录所有领域事件';
COMMENT ON TABLE aggregate_snapshots IS '聚合根快照表，用于优化事件重放性能';
COMMENT ON TABLE saga_instances IS 'Saga实例表，记录长流程的执行状态';
COMMENT ON TABLE saga_steps IS 'Saga步骤表，记录每个Saga的执行步骤';
COMMENT ON TABLE event_publications IS '事件发布状态表，确保事件可靠发布';

-- 添加列注释
COMMENT ON COLUMN event_store.event_id IS '事件唯一标识符';
COMMENT ON COLUMN event_store.aggregate_id IS '聚合根标识符';
COMMENT ON COLUMN event_store.aggregate_type IS '聚合根类型';
COMMENT ON COLUMN event_store.event_type IS '事件类型';
COMMENT ON COLUMN event_store.event_data IS '事件数据（JSON格式）';
COMMENT ON COLUMN event_store.version IS '事件版本号';
COMMENT ON COLUMN event_store.occurred_on IS '事件发生时间';

COMMENT ON COLUMN aggregate_snapshots.snapshot_id IS '快照唯一标识符';
COMMENT ON COLUMN aggregate_snapshots.aggregate_id IS '聚合根标识符';
COMMENT ON COLUMN aggregate_snapshots.version IS '快照对应的聚合根版本';
COMMENT ON COLUMN aggregate_snapshots.snapshot_data IS '快照数据（JSON格式）';

COMMENT ON COLUMN saga_instances.saga_id IS 'Saga唯一标识符';
COMMENT ON COLUMN saga_instances.saga_type IS 'Saga类型';
COMMENT ON COLUMN saga_instances.correlation_id IS '关联标识符';
COMMENT ON COLUMN saga_instances.status IS 'Saga状态';
COMMENT ON COLUMN saga_instances.saga_data IS 'Saga数据（JSON格式）';