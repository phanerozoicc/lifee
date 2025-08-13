-- 更新推荐表的用户ID字段格式
-- 从UUID改为U00000001格式

-- 修改recommendations表的user_id字段
ALTER TABLE recommendations 
ALTER COLUMN user_id TYPE VARCHAR(9);

-- 添加user_id字段的检查约束
ALTER TABLE recommendations 
ADD CONSTRAINT chk_user_id_format 
CHECK (user_id ~ '^U[0-9]{8}$');

-- 添加注释
COMMENT ON COLUMN recommendations.user_id IS '用户ID，格式为U00000001';