-- 创建配置表
CREATE TABLE configurations (
    id VARCHAR(36) PRIMARY KEY,
    namespace VARCHAR(200) NOT NULL,
    environment VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_config_namespace_env UNIQUE (namespace, environment)
);

-- 创建配置项表
CREATE TABLE config_items (
    id BIGSERIAL PRIMARY KEY,
    configuration_id VARCHAR(36) NOT NULL,
    key VARCHAR(200) NOT NULL,
    value TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    environment VARCHAR(50) NOT NULL,
    description TEXT DEFAULT '',
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_config_item_configuration FOREIGN KEY (configuration_id) REFERENCES configurations(id) ON DELETE CASCADE,
    CONSTRAINT uk_config_item_key UNIQUE (configuration_id, key)
);

-- 创建索引
CREATE INDEX idx_config_namespace ON configurations(namespace);
CREATE INDEX idx_config_environment ON configurations(environment);
CREATE INDEX idx_config_namespace_env ON configurations(namespace, environment);
CREATE INDEX idx_config_updated_at ON configurations(updated_at);

CREATE INDEX idx_config_item_key ON config_items(key);
CREATE INDEX idx_config_item_type ON config_items(type);
CREATE INDEX idx_config_item_encrypted ON config_items(is_encrypted);
CREATE INDEX idx_config_item_environment ON config_items(environment);
CREATE INDEX idx_config_item_updated_at ON config_items(updated_at);

-- 添加表注释
COMMENT ON TABLE configurations IS '配置表';
COMMENT ON COLUMN configurations.id IS '配置ID';
COMMENT ON COLUMN configurations.namespace IS '命名空间';
COMMENT ON COLUMN configurations.environment IS '环境';
COMMENT ON COLUMN configurations.created_at IS '创建时间';
COMMENT ON COLUMN configurations.updated_at IS '更新时间';

COMMENT ON TABLE config_items IS '配置项表';
COMMENT ON COLUMN config_items.id IS '配置项ID';
COMMENT ON COLUMN config_items.configuration_id IS '配置ID';
COMMENT ON COLUMN config_items.key IS '配置键';
COMMENT ON COLUMN config_items.value IS '配置值';
COMMENT ON COLUMN config_items.type IS '配置类型';
COMMENT ON COLUMN config_items.environment IS '环境';
COMMENT ON COLUMN config_items.description IS '描述';
COMMENT ON COLUMN config_items.is_encrypted IS '是否加密';
COMMENT ON COLUMN config_items.created_at IS '创建时间';
COMMENT ON COLUMN config_items.updated_at IS '更新时间';

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为配置表创建更新时间触发器
CREATE TRIGGER update_configurations_updated_at
    BEFORE UPDATE ON configurations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 为配置项表创建更新时间触发器
CREATE TRIGGER update_config_items_updated_at
    BEFORE UPDATE ON config_items
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 插入一些初始数据
INSERT INTO configurations (id, namespace, environment, created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'system', 'development', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440002', 'system', 'production', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440003', 'app', 'development', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO config_items (configuration_id, key, value, type, environment, description, is_encrypted, created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'app.name', 'Lifee Development', 'STRING', 'development', '应用名称', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440001', 'app.version', '1.0.0', 'STRING', 'development', '应用版本', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440001', 'database.pool.size', '10', 'INTEGER', 'development', '数据库连接池大小', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440002', 'app.name', 'Lifee Production', 'STRING', 'production', '应用名称', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440002', 'app.version', '1.0.0', 'STRING', 'production', '应用版本', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440002', 'database.pool.size', '50', 'INTEGER', 'production', '数据库连接池大小', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440003', 'feature.recommendation.enabled', 'true', 'BOOLEAN', 'development', '推荐功能开关', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);