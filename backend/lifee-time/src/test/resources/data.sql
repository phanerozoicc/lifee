-- 测试数据初始化脚本
-- 用于集成测试的基础数据

-- 插入测试团队数据
INSERT INTO teams (id, name, description, owner_id, is_active, created_at, updated_at) VALUES
('11111111-1111-1111-1111-111111111111', 'Development Team', 'Main development team', '22222222-2222-2222-2222-222222222222', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('33333333-3333-3333-3333-333333333333', 'Design Team', 'UI/UX design team', '44444444-4444-4444-4444-444444444444', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('55555555-5555-5555-5555-555555555555', 'QA Team', 'Quality assurance team', '66666666-6666-6666-6666-666666666666', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入测试团队成员数据
INSERT INTO team_members (id, team_id, user_id, role, joined_at, is_active) VALUES
('77777777-7777-7777-7777-777777777777', '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', 'OWNER', CURRENT_TIMESTAMP, true),
('88888888-8888-8888-8888-888888888888', '11111111-1111-1111-1111-111111111111', '99999999-9999-9999-9999-999999999999', 'ADMIN', CURRENT_TIMESTAMP, true),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'MEMBER', CURRENT_TIMESTAMP, true),
('cccccccc-cccc-cccc-cccc-cccccccccccc', '33333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444444', 'OWNER', CURRENT_TIMESTAMP, true),
('dddddddd-dddd-dddd-dddd-dddddddddddd', '55555555-5555-5555-5555-555555555555', '66666666-6666-6666-6666-666666666666', 'OWNER', CURRENT_TIMESTAMP, true);

-- 插入测试项目数据
INSERT INTO projects (id, name, description, color, billable, hourly_rate, owner_id, team_id, is_public, is_archived, created_at, updated_at) VALUES
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Web Application', 'Main web application project', '#FF5733', true, 75.00, '22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('ffffffff-ffff-ffff-ffff-ffffffffffff', 'Mobile App', 'Mobile application project', '#33FF57', true, 80.00, '22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('10101010-1010-1010-1010-101010101010', 'Design System', 'Company design system', '#3357FF', false, 60.00, '44444444-4444-4444-4444-444444444444', '33333333-3333-3333-3333-333333333333', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('20202020-2020-2020-2020-202020202020', 'Legacy System', 'Old system maintenance', '#FF3357', true, 50.00, '22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入测试项目成员数据
INSERT INTO project_members (id, project_id, user_id, role, hourly_rate, joined_at) VALUES
('30303030-3030-3030-3030-303030303030', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '22222222-2222-2222-2222-222222222222', 'OWNER', 75.00, CURRENT_TIMESTAMP),
('40404040-4040-4040-4040-404040404040', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '99999999-9999-9999-9999-999999999999', 'ADMIN', 70.00, CURRENT_TIMESTAMP),
('50505050-5050-5050-5050-505050505050', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'MEMBER', 60.00, CURRENT_TIMESTAMP),
('60606060-6060-6060-6060-606060606060', 'ffffffff-ffff-ffff-ffff-ffffffffffff', '22222222-2222-2222-2222-222222222222', 'OWNER', 80.00, CURRENT_TIMESTAMP),
('70707070-7070-7070-7070-707070707070', '10101010-1010-1010-1010-101010101010', '44444444-4444-4444-4444-444444444444', 'OWNER', 60.00, CURRENT_TIMESTAMP);

-- 插入测试任务数据
INSERT INTO tasks (id, name, description, project_id, assignee_id, creator_id, status, priority, due_date, estimated_hours, tags, created_at, updated_at) VALUES
('80808080-8080-8080-8080-808080808080', 'User Authentication', 'Implement user login and registration', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '99999999-9999-9999-9999-999999999999', '22222222-2222-2222-2222-222222222222', 'IN_PROGRESS', 'HIGH', DATEADD('DAY', 7, CURRENT_DATE), 16, 'authentication,security', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('90909090-9090-9090-9090-909090909090', 'Dashboard Design', 'Create main dashboard UI', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 'TODO', 'MEDIUM', DATEADD('DAY', 14, CURRENT_DATE), 24, 'ui,dashboard', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 'API Documentation', 'Write comprehensive API docs', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '99999999-9999-9999-9999-999999999999', '22222222-2222-2222-2222-222222222222', 'COMPLETED', 'LOW', DATEADD('DAY', -3, CURRENT_DATE), 8, 'documentation,api', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 'Mobile Navigation', 'Implement mobile app navigation', 'ffffffff-ffff-ffff-ffff-ffffffffffff', '22222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222', 'IN_PROGRESS', 'HIGH', DATEADD('DAY', 10, CURRENT_DATE), 12, 'mobile,navigation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', 'Color Palette', 'Define brand color palette', '10101010-1010-1010-1010-101010101010', '44444444-4444-4444-4444-444444444444', '44444444-4444-4444-4444-444444444444', 'COMPLETED', 'MEDIUM', DATEADD('DAY', -7, CURRENT_DATE), 4, 'design,branding', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入测试时间条目数据
INSERT INTO time_entries (id, user_id, project_id, task_id, description, start_time, end_time, billable, tags, created_at, updated_at) VALUES
('d4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4', '22222222-2222-2222-2222-222222222222', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '80808080-8080-8080-8080-808080808080', 'Working on user authentication logic', DATEADD('HOUR', -3, CURRENT_TIMESTAMP), DATEADD('HOUR', -1, CURRENT_TIMESTAMP), true, 'development,backend', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e5e5e5e5-e5e5-e5e5-e5e5-e5e5e5e5e5e5', '99999999-9999-9999-9999-999999999999', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '90909090-9090-9090-9090-909090909090', 'Dashboard wireframe creation', DATEADD('HOUR', -5, CURRENT_TIMESTAMP), DATEADD('HOUR', -3, CURRENT_TIMESTAMP), true, 'design,wireframe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f6f6f6f6-f6f6-f6f6-f6f6-f6f6f6f6f6f6', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 'Writing API documentation', DATEADD('DAY', -1, CURRENT_TIMESTAMP), DATEADD('DAY', -1, DATEADD('HOUR', 4, CURRENT_TIMESTAMP)), false, 'documentation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('17171717-1717-1717-1717-171717171717', '22222222-2222-2222-2222-222222222222', 'ffffffff-ffff-ffff-ffff-ffffffffffff', 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 'Mobile navigation implementation', DATEADD('HOUR', -2, CURRENT_TIMESTAMP), NULL, true, 'mobile,development', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('18181818-1818-1818-1818-181818181818', '44444444-4444-4444-4444-444444444444', '10101010-1010-1010-1010-101010101010', 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', 'Color palette research', DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -2, DATEADD('HOUR', 2, CURRENT_TIMESTAMP)), false, 'research,design', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入更多历史时间条目数据用于统计测试
INSERT INTO time_entries (id, user_id, project_id, task_id, description, start_time, end_time, billable, tags, created_at, updated_at) VALUES
('19191919-1919-1919-1919-191919191919', '22222222-2222-2222-2222-222222222222', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '80808080-8080-8080-8080-808080808080', 'Authentication testing', DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', -3, DATEADD('HOUR', 3, CURRENT_TIMESTAMP)), true, 'testing,backend', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('1a1a1a1a-1a1a-1a1a-1a1a-1a1a1a1a1a1a', '99999999-9999-9999-9999-999999999999', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '90909090-9090-9090-9090-909090909090', 'Dashboard component development', DATEADD('DAY', -4, CURRENT_TIMESTAMP), DATEADD('DAY', -4, DATEADD('HOUR', 5, CURRENT_TIMESTAMP)), true, 'frontend,development', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('1b1b1b1b-1b1b-1b1b-1b1b-1b1b1b1b1b1b', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', NULL, 'Code review and refactoring', DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('DAY', -5, DATEADD('HOUR', 2, CURRENT_TIMESTAMP)), false, 'review,refactoring', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('1c1c1c1c-1c1c-1c1c-1c1c-1c1c1c1c1c1c', '22222222-2222-2222-2222-222222222222', 'ffffffff-ffff-ffff-ffff-ffffffffffff', NULL, 'Mobile app architecture planning', DATEADD('DAY', -6, CURRENT_TIMESTAMP), DATEADD('DAY', -6, DATEADD('HOUR', 4, CURRENT_TIMESTAMP)), true, 'planning,architecture', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('1d1d1d1d-1d1d-1d1d-1d1d-1d1d1d1d1d1d', '44444444-4444-4444-4444-444444444444', '10101010-1010-1010-1010-101010101010', NULL, 'Design system documentation', DATEADD('DAY', -7, CURRENT_TIMESTAMP), DATEADD('DAY', -7, DATEADD('HOUR', 3, CURRENT_TIMESTAMP)), false, 'documentation,design', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 提交事务
COMMIT;