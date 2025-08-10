-- 插入初始测试数据

-- 插入管理员用户（密码为：admin123）
INSERT INTO users (
    id,
    email,
    password_hash,
    first_name,
    last_name,
    status,
    email_verified,
    created_at,
    updated_at
) VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'admin@lifee.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLVZqpjBWNOx0wYdLWrG', -- admin123
    'Admin',
    'User',
    'ACTIVE',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- 插入测试用户1（密码为：test123）
INSERT INTO users (
    id,
    email,
    password_hash,
    first_name,
    last_name,
    date_of_birth,
    phone_number,
    status,
    email_verified,
    created_at,
    updated_at
) VALUES (
    'a0000000-0000-0000-0000-000000000002',
    'test1@lifee.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- test123
    'Test',
    'User1',
    '1990-01-15',
    '+86-13800138001',
    'ACTIVE',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- 插入测试用户2（密码为：test123）
INSERT INTO users (
    id,
    email,
    password_hash,
    first_name,
    last_name,
    date_of_birth,
    phone_number,
    status,
    email_verified,
    created_at,
    updated_at
) VALUES (
    'a0000000-0000-0000-0000-000000000003',
    'test2@lifee.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- test123
    'Test',
    'User2',
    '1985-05-20',
    '+86-13800138002',
    'PENDING_ACTIVATION',
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- 插入一些示例事件数据
INSERT INTO user_events (
    aggregate_id,
    event_type,
    event_data,
    occurred_on
) VALUES 
(
    'a0000000-0000-0000-0000-000000000001',
    'UserRegistered',
    '{
        "userId": "a0000000-0000-0000-0000-000000000001",
        "email": "admin@lifee.com",
        "firstName": "Admin",
        "lastName": "User"
    }',
    CURRENT_TIMESTAMP - INTERVAL '30 days'
),
(
    'a0000000-0000-0000-0000-000000000001',
    'UserActivated',
    '{
        "userId": "a0000000-0000-0000-0000-000000000001"
    }',
    CURRENT_TIMESTAMP - INTERVAL '30 days' + INTERVAL '1 hour'
),
(
    'a0000000-0000-0000-0000-000000000002',
    'UserRegistered',
    '{
        "userId": "a0000000-0000-0000-0000-000000000002",
        "email": "test1@lifee.com",
        "firstName": "Test",
        "lastName": "User1"
    }',
    CURRENT_TIMESTAMP - INTERVAL '15 days'
),
(
    'a0000000-0000-0000-0000-000000000002',
    'UserActivated',
    '{
        "userId": "a0000000-0000-0000-0000-000000000002"
    }',
    CURRENT_TIMESTAMP - INTERVAL '15 days' + INTERVAL '2 hours'
),
(
    'a0000000-0000-0000-0000-000000000003',
    'UserRegistered',
    '{
        "userId": "a0000000-0000-0000-0000-000000000003",
        "email": "test2@lifee.com",
        "firstName": "Test",
        "lastName": "User2"
    }',
    CURRENT_TIMESTAMP - INTERVAL '7 days'
);

-- 插入一些登录日志示例
INSERT INTO user_login_logs (
    user_id,
    email,
    login_result,
    ip_address,
    user_agent,
    created_at
) VALUES 
(
    'a0000000-0000-0000-0000-000000000001',
    'admin@lifee.com',
    'SUCCESS',
    '127.0.0.1',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '1 day'
),
(
    'a0000000-0000-0000-0000-000000000002',
    'test1@lifee.com',
    'SUCCESS',
    '192.168.1.100',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
),
(
    NULL,
    'invalid@example.com',
    'FAILED_INVALID_CREDENTIALS',
    '192.168.1.200',
    'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes'
);