-- 创建推荐表
CREATE TABLE recommendations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建推荐项表
CREATE TABLE recommendation_items (
    id BIGSERIAL PRIMARY KEY,
    recommendation_id UUID NOT NULL,
    content_id UUID NOT NULL,
    score DECIMAL(3,2) NOT NULL CHECK (score >= 0.0 AND score <= 1.0),
    type VARCHAR(50) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recommendation_id) REFERENCES recommendations(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_recommendations_user_id ON recommendations(user_id);
CREATE INDEX idx_recommendation_items_recommendation_id ON recommendation_items(recommendation_id);
CREATE INDEX idx_recommendation_items_content_id ON recommendation_items(content_id);
CREATE INDEX idx_recommendation_items_type ON recommendation_items(type);
CREATE INDEX idx_recommendation_items_score ON recommendation_items(score DESC);
CREATE INDEX idx_recommendation_items_created_at ON recommendation_items(created_at DESC);

-- 创建唯一约束，确保同一推荐中不会有重复的内容
CREATE UNIQUE INDEX idx_recommendation_items_unique_content 
ON recommendation_items(recommendation_id, content_id);

-- 创建用户推荐唯一约束
CREATE UNIQUE INDEX idx_recommendations_unique_user 
ON recommendations(user_id);

-- 添加注释
COMMENT ON TABLE recommendations IS '用户推荐表';
COMMENT ON COLUMN recommendations.id IS '推荐ID';
COMMENT ON COLUMN recommendations.user_id IS '用户ID';
COMMENT ON COLUMN recommendations.created_at IS '创建时间';
COMMENT ON COLUMN recommendations.updated_at IS '更新时间';

COMMENT ON TABLE recommendation_items IS '推荐项表';
COMMENT ON COLUMN recommendation_items.id IS '推荐项ID';
COMMENT ON COLUMN recommendation_items.recommendation_id IS '推荐ID';
COMMENT ON COLUMN recommendation_items.content_id IS '内容ID';
COMMENT ON COLUMN recommendation_items.score IS '推荐分数(0.0-1.0)';
COMMENT ON COLUMN recommendation_items.type IS '推荐类型';
COMMENT ON COLUMN recommendation_items.reason IS '推荐原因';
COMMENT ON COLUMN recommendation_items.created_at IS '创建时间';

-- 创建更新时间触发器
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_recommendations_updated_at 
    BEFORE UPDATE ON recommendations 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();