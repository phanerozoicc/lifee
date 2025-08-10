-- 创建用户事件表（用于事件溯源）
CREATE TABLE IF NOT EXISTS user_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB NOT NULL,
    event_version INTEGER NOT NULL DEFAULT 1,
    occurred_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_user_events_aggregate_id ON user_events(aggregate_id);
CREATE INDEX IF NOT EXISTS idx_user_events_event_type ON user_events(event_type);
CREATE INDEX IF NOT EXISTS idx_user_events_occurred_on ON user_events(occurred_on);
CREATE INDEX IF NOT EXISTS idx_user_events_aggregate_id_occurred_on ON user_events(aggregate_id, occurred_on);

-- 创建用户会话表
CREATE TABLE IF NOT EXISTS user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_token VARCHAR(255) NOT NULL UNIQUE,
    refresh_token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    refresh_expires_at TIMESTAMP NOT NULL,
    ip_address INET,
    user_agent TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建会话表索引
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_session_token ON user_sessions(session_token);
CREATE INDEX IF NOT EXISTS idx_user_sessions_refresh_token ON user_sessions(refresh_token);
CREATE INDEX IF NOT EXISTS idx_user_sessions_expires_at ON user_sessions(expires_at);
CREATE INDEX IF NOT EXISTS idx_user_sessions_is_active ON user_sessions(is_active);

-- 创建用户登录日志表
CREATE TABLE IF NOT EXISTS user_login_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    email VARCHAR(255) NOT NULL,
    login_result VARCHAR(20) NOT NULL,
    ip_address INET,
    user_agent TEXT,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建登录日志表索引
CREATE INDEX IF NOT EXISTS idx_user_login_logs_user_id ON user_login_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_user_login_logs_email ON user_login_logs(email);
CREATE INDEX IF NOT EXISTS idx_user_login_logs_login_result ON user_login_logs(login_result);
CREATE INDEX IF NOT EXISTS idx_user_login_logs_created_at ON user_login_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_user_login_logs_ip_address ON user_login_logs(ip_address);

-- 为会话表添加更新时间触发器
CREATE TRIGGER update_user_sessions_updated_at
    BEFORE UPDATE ON user_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加约束
ALTER TABLE user_login_logs ADD CONSTRAINT chk_login_result 
    CHECK (login_result IN ('SUCCESS', 'FAILED_INVALID_CREDENTIALS', 'FAILED_ACCOUNT_LOCKED', 'FAILED_ACCOUNT_DISABLED'));

-- 添加注释
COMMENT ON TABLE user_events IS '用户事件表（事件溯源）';
COMMENT ON COLUMN user_events.aggregate_id IS '聚合根ID（用户ID）';
COMMENT ON COLUMN user_events.event_type IS '事件类型';
COMMENT ON COLUMN user_events.event_data IS '事件数据（JSON格式）';
COMMENT ON COLUMN user_events.event_version IS '事件版本';
COMMENT ON COLUMN user_events.occurred_on IS '事件发生时间';

COMMENT ON TABLE user_sessions IS '用户会话表';
COMMENT ON COLUMN user_sessions.session_token IS '会话令牌';
COMMENT ON COLUMN user_sessions.refresh_token IS '刷新令牌';
COMMENT ON COLUMN user_sessions.expires_at IS '会话过期时间';
COMMENT ON COLUMN user_sessions.refresh_expires_at IS '刷新令牌过期时间';

COMMENT ON TABLE user_login_logs IS '用户登录日志表';
COMMENT ON COLUMN user_login_logs.login_result IS '登录结果：SUCCESS-成功，FAILED_INVALID_CREDENTIALS-凭据无效，FAILED_ACCOUNT_LOCKED-账户锁定，FAILED_ACCOUNT_DISABLED-账户禁用';