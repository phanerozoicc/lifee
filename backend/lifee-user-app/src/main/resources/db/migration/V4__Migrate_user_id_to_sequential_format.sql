-- 迁移用户ID格式从UUID到递增格式 (U00000001)

-- 1. 创建用户ID序列
CREATE SEQUENCE IF NOT EXISTS user_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999999
    CACHE 1;

-- 2. 添加新的ID列（临时）
ALTER TABLE users ADD COLUMN new_id VARCHAR(9);

-- 3. 为现有用户生成新的ID格式
DO $$
DECLARE
    user_record RECORD;
    sequence_num BIGINT;
BEGIN
    FOR user_record IN SELECT id FROM users ORDER BY created_at LOOP
        sequence_num := nextval('user_id_seq');
        UPDATE users 
        SET new_id = 'U' || LPAD(sequence_num::TEXT, 8, '0')
        WHERE id = user_record.id;
    END LOOP;
END $$;

-- 4. 验证所有用户都有新ID
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM users WHERE new_id IS NULL) THEN
        RAISE EXCEPTION '存在用户没有分配新ID';
    END IF;
END $$;

-- 5. 删除旧的主键约束
ALTER TABLE users DROP CONSTRAINT users_pkey;

-- 6. 删除旧的ID列
ALTER TABLE users DROP COLUMN id;

-- 7. 重命名新ID列为id
ALTER TABLE users RENAME COLUMN new_id TO id;

-- 8. 设置新ID列为NOT NULL
ALTER TABLE users ALTER COLUMN id SET NOT NULL;

-- 9. 添加新的主键约束
ALTER TABLE users ADD CONSTRAINT users_pkey PRIMARY KEY (id);

-- 10. 添加ID格式检查约束
ALTER TABLE users ADD CONSTRAINT chk_users_id_format 
    CHECK (id ~ '^U[0-9]{8}$');

-- 11. 更新注释
COMMENT ON COLUMN users.id IS '用户ID，格式：U00000001';
COMMENT ON SEQUENCE user_id_seq IS '用户ID序列，用于生成递增的用户ID';

-- 12. 创建索引（如果需要）
CREATE INDEX IF NOT EXISTS idx_users_id_sequence ON users(CAST(SUBSTRING(id, 2) AS INTEGER));

-- 验证迁移结果
DO $$
DECLARE
    user_count INTEGER;
    invalid_id_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users;
    SELECT COUNT(*) INTO invalid_id_count FROM users WHERE id !~ '^U[0-9]{8}$';
    
    RAISE NOTICE '迁移完成：总用户数 %, 无效ID数 %', user_count, invalid_id_count;
    
    IF invalid_id_count > 0 THEN
        RAISE EXCEPTION '存在无效格式的用户ID';
    END IF;
END $$;