# Lifee Time Tracking Module

## 概述

Lifee Time Tracking Module 是一个基于 DDD（领域驱动设计）架构的时间管理模块，提供完整的时间追踪、项目管理、团队协作和报告分析功能。

## 核心功能

### 1. 时间追踪 (Time Tracking)
- 开始/停止/暂停/恢复时间记录
- 支持手动创建和编辑时间条目
- 时间条目标签管理
- 计费状态管理
- 实时时间追踪

### 2. 项目管理 (Project Management)
- 项目创建、更新、归档、恢复
- 项目成员管理（添加、移除、角色管理）
- 项目权限控制
- 项目统计和分析
- 项目复制功能

### 3. 任务管理 (Task Management)
- 任务创建、分配、状态管理
- 任务优先级和截止日期
- 任务标签和分类
- 任务进度跟踪
- 批量操作支持

### 4. 团队协作 (Team Collaboration)
- 团队创建和管理
- 团队成员角色管理
- 团队权限控制
- 团队统计分析

### 5. 报告分析 (Reporting & Analytics)
- 时间报告生成
- 项目时间分布分析
- 团队生产力趋势
- 多格式导出（CSV、Excel、PDF）
- 实时统计仪表板

## 技术架构

### DDD 架构层次

```
├── domain/                    # 领域层
│   ├── entity/               # 实体
│   ├── valueobject/          # 值对象
│   ├── event/                # 领域事件
│   ├── service/              # 领域服务
│   └── repository/           # 仓储接口
├── application/              # 应用层
│   ├── service/              # 应用服务
│   └── dto/                  # 数据传输对象
├── infrastructure/           # 基础设施层
│   ├── repository/           # 仓储实现
│   ├── event/                # 事件处理
│   └── config/               # 配置
└── web/                      # 表现层
    ├── controller/           # REST 控制器
    └── exception/            # 异常处理
```

### 核心领域实体

#### TimeEntry（时间条目）
- 时间追踪的核心聚合根
- 管理开始、停止、暂停、恢复等状态
- 支持描述、项目、任务、标签等属性

#### Project（项目）
- 项目管理的聚合根
- 包含项目信息、成员管理、权限控制
- 支持归档、恢复、复制等操作

#### Task（任务）
- 任务管理的聚合根
- 包含任务状态、优先级、分配等信息
- 支持任务生命周期管理

#### Team（团队）
- 团队协作的聚合根
- 管理团队成员和角色
- 提供团队级别的权限控制

### 值对象

- `TimeEntryId`: 时间条目唯一标识
- `ProjectId`: 项目唯一标识
- `TaskId`: 任务唯一标识
- `TeamId`: 团队唯一标识
- `Duration`: 时间长度
- `TimeRange`: 时间范围
- `Money`: 金额（支持多币种）
- `Email`: 邮箱地址
- `Color`: 颜色值

### 领域事件

- `TimeEntryStarted`: 时间追踪开始
- `TimeEntryStopped`: 时间追踪停止
- `TimeEntryPaused`: 时间追踪暂停
- `TimeEntryResumed`: 时间追踪恢复
- `ProjectCreated`: 项目创建
- `ProjectArchived`: 项目归档
- `TaskCreated`: 任务创建
- `TaskCompleted`: 任务完成
- `TeamMemberAdded`: 团队成员添加

## API 接口

### 时间追踪 API

```http
# 开始时间追踪
POST /api/time-tracking/start

# 停止时间追踪
POST /api/time-tracking/{id}/stop

# 暂停时间追踪
POST /api/time-tracking/{id}/pause

# 恢复时间追踪
POST /api/time-tracking/{id}/resume

# 获取用户时间条目
GET /api/time-tracking/user/{userId}

# 更新时间条目
PUT /api/time-tracking/{id}
```

### 项目管理 API

```http
# 创建项目
POST /api/projects

# 获取项目详情
GET /api/projects/{id}

# 更新项目
PUT /api/projects/{id}

# 归档项目
POST /api/projects/{id}/archive

# 添加项目成员
POST /api/projects/{id}/members

# 获取用户项目列表
GET /api/projects/user/{userId}
```

### 任务管理 API

```http
# 创建任务
POST /api/tasks

# 获取任务详情
GET /api/tasks/{id}

# 更新任务状态
PUT /api/tasks/{id}/status

# 分配任务
POST /api/tasks/{id}/assign

# 获取项目任务列表
GET /api/tasks/project/{projectId}
```

### 团队管理 API

```http
# 创建团队
POST /api/teams

# 获取团队详情
GET /api/teams/{id}

# 添加团队成员
POST /api/teams/{id}/members

# 更新成员角色
PUT /api/teams/{id}/members/{userId}/role

# 获取用户团队列表
GET /api/teams/user/{userId}
```

### 报告分析 API

```http
# 生成时间报告
POST /api/reports/time

# 生成项目报告
POST /api/reports/project

# 导出报告
GET /api/reports/{id}/export

# 获取统计数据
GET /api/statistics/dashboard

# 获取生产力趋势
GET /api/statistics/productivity-trends
```

## 数据库设计

### 主要表结构

- `time_entries`: 时间条目表
- `projects`: 项目表
- `project_members`: 项目成员表
- `tasks`: 任务表
- `teams`: 团队表
- `team_members`: 团队成员表

### 索引优化

- 用户ID索引：快速查询用户相关数据
- 时间范围索引：优化时间查询性能
- 项目ID索引：提升项目相关查询效率
- 复合索引：支持复杂查询场景

## 配置说明

### 应用配置 (application-time.yml)

```yaml
spring:
  application:
    name: lifee-time-tracking
  
  datasource:
    url: jdbc:mysql://localhost:3306/lifee_time
    username: ${DB_USERNAME:lifee}
    password: ${DB_PASSWORD:password}
  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}

lifee:
  time:
    tracking:
      max-concurrent-entries: 1
      auto-stop-idle-minutes: 480
    projects:
      max-members: 100
      default-hourly-rate: 50.0
    reports:
      max-export-records: 10000
      cache-duration-minutes: 30
```

## 部署说明

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.8+

### 构建命令

```bash
# 编译
./mvnw clean compile -pl lifee-time

# 运行测试
./mvnw test -pl lifee-time

# 打包
./mvnw package -pl lifee-time

# 安装到本地仓库
./mvnw install -pl lifee-time
```

### 数据库初始化

```bash
# 运行 Flyway 迁移
./mvnw flyway:migrate -pl lifee-time
```

## 使用示例

### 1. 开始时间追踪

```kotlin
val request = StartTimeTrackingRequest(
    userId = "user123",
    description = "开发新功能",
    projectId = "project456",
    taskId = "task789",
    tags = listOf("开发", "前端")
)

val timeEntry = timeTrackingService.startTimeTracking(request)
```

### 2. 创建项目

```kotlin
val request = CreateProjectRequest(
    name = "新项目",
    description = "项目描述",
    color = "#FF5722",
    hourlyRate = Money.of(100.0, "CNY"),
    ownerId = "user123"
)

val project = projectService.createProject(request)
```

### 3. 生成报告

```kotlin
val request = GenerateTimeReportRequest(
    userId = "user123",
    startDate = LocalDate.now().minusDays(30),
    endDate = LocalDate.now(),
    projectIds = listOf("project456"),
    format = ReportFormat.PDF
)

val report = reportService.generateTimeReport(request)
```

## 扩展开发

### 添加新的领域事件

1. 在 `domain/event` 包下创建事件类
2. 在相应的聚合根中发布事件
3. 在 `infrastructure/event` 包下创建事件处理器

### 添加新的查询功能

1. 在 Repository 接口中定义查询方法
2. 在 JPA Repository 实现中添加具体实现
3. 在应用服务中调用查询方法

### 添加新的 API 接口

1. 在 `web/controller` 包下创建控制器
2. 定义请求和响应 DTO
3. 添加相应的异常处理

## 监控和日志

### 应用监控

- Spring Boot Actuator 健康检查
- Micrometer 指标收集
- 自定义业务指标

### 日志配置

- 结构化日志输出
- 不同级别的日志配置
- 敏感信息脱敏

## 性能优化

### 缓存策略

- Redis 缓存热点数据
- 查询结果缓存
- 分布式缓存一致性

### 数据库优化

- 索引优化
- 查询优化
- 连接池配置

## 安全考虑

### 权限控制

- 基于角色的访问控制（RBAC）
- 资源级别权限验证
- API 接口权限保护

### 数据安全

- 敏感数据加密
- SQL 注入防护
- XSS 攻击防护

## 故障排查

### 常见问题

1. **时间追踪无法开始**
   - 检查用户是否已有运行中的时间条目
   - 验证项目和任务是否存在且有权限

2. **报告生成失败**
   - 检查数据范围是否过大
   - 验证导出格式是否支持

3. **权限验证失败**
   - 检查用户角色和权限配置
   - 验证 JWT Token 是否有效

### 日志分析

- 查看应用日志定位具体错误
- 分析数据库慢查询日志
- 监控 Redis 缓存命中率

## 版本历史

- v1.0.0: 初始版本，包含基础时间追踪功能
- 后续版本将持续优化和扩展功能

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交代码变更
4. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证，详见 LICENSE 文件。