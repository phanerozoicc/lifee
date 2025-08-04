# 用户管理模块 (User Module)

基于领域驱动设计(DDD)和CQRS架构模式的用户管理微服务，使用Kotlin和Spring Boot构建。

## 🏗️ 架构概览

本项目采用六边形架构(Hexagonal Architecture)和领域驱动设计(DDD)，结合CQRS(命令查询职责分离)模式：

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │   Controllers   │  │      DTOs       │  │  Exception  │ │
│  │                 │  │                 │  │  Handlers   │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                   Application Layer                         │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │   Application   │  │     Command     │  │    Query    │ │
│  │    Services     │  │    Handlers     │  │   Handlers  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │   Aggregates    │  │     Entities    │  │    Value    │ │
│  │                 │  │                 │  │   Objects   │ │
│  │  ┌───────────┐  │  │  ┌───────────┐  │  │             │ │
│  │  │   User    │  │  │  │  Profile  │  │  │             │ │
│  │  └───────────┘  │  │  │  Status   │  │  │             │ │
│  │                 │  │  │Preferences│  │  │             │ │
│  └─────────────────┘  │  └───────────┘  │  └─────────────┘ │
│  ┌─────────────────┐  └─────────────────┘  ┌─────────────┐ │
│  │     Domain      │  ┌─────────────────┐  │   Domain    │ │
│  │    Services     │  │   Repositories  │  │  Exceptions │ │
│  │                 │  │  (Interfaces)   │  │             │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                 Infrastructure Layer                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │   Repository    │  │   Persistence   │  │    CQRS     │ │
│  │ Implementations │  │    Entities     │  │    Bus      │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │   Mappers       │  │  Configuration  │  │  External   │ │
│  │                 │  │                 │  │  Services   │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 📁 项目结构

```
user/
├── src/main/kotlin/com/github/phanerozoicc/user/
│   ├── application/                    # 应用层
│   │   ├── command/                   # 命令处理器
│   │   ├── query/                     # 查询处理器
│   │   └── service/                   # 应用服务
│   ├── domain/                        # 领域层
│   │   ├── aggregate/                 # 聚合根
│   │   ├── entity/                    # 实体
│   │   ├── valueobject/              # 值对象
│   │   ├── service/                   # 领域服务
│   │   ├── repository/                # 仓储接口
│   │   ├── policy/                    # 策略
│   │   ├── cqrs/                      # CQRS定义
│   │   ├── query/                     # 查询定义
│   │   └── exception/                 # 领域异常
│   ├── infrastructure/                # 基础设施层
│   │   ├── repository/                # 仓储实现
│   │   ├── persistence/               # 持久化
│   │   │   ├── entity/               # JPA实体
│   │   │   ├── repository/           # JPA仓储
│   │   │   └── mapper/               # 映射器
│   │   ├── cqrs/                      # CQRS实现
│   │   └── config/                    # 配置
│   ├── presentation/                  # 表现层
│   │   ├── controller/                # REST控制器
│   │   ├── dto/                       # 数据传输对象
│   │   └── exception/                 # 异常处理
│   └── UserApplication.kt             # 启动类
├── src/main/resources/
│   └── application.yml                # 配置文件
├── build.gradle.kts                   # 构建脚本
└── README.md                          # 项目文档
```

## 🚀 核心功能

### 用户管理
- ✅ 用户注册与登录
- ✅ 用户资料管理
- ✅ 密码管理（修改、重置）
- ✅ 邮箱验证
- ✅ 用户状态管理（激活、停用、锁定）
- ✅ 用户偏好设置
- ✅ 用户搜索与列表
- ✅ 批量操作

### 安全特性
- 🔐 密码策略验证
- 🔐 登录尝试限制
- 🔐 会话管理
- 🔐 JWT令牌支持
- 🔐 权限验证

### 系统特性
- 📊 用户统计
- 📝 操作日志
- 🔍 审计追踪
- 📈 监控指标
- 🚨 异常处理

## 🛠️ 技术栈

### 核心框架
- **Kotlin 1.9.20** - 主要编程语言
- **Spring Boot 3.2.0** - 应用框架
- **Spring Data JPA** - 数据访问
- **Spring Security** - 安全框架
- **Spring Validation** - 参数验证

### 数据库
- **H2** - 开发/测试数据库
- **PostgreSQL** - 生产数据库
- **Hibernate** - ORM框架

### 工具库
- **Jackson** - JSON序列化
- **JWT** - 令牌认证
- **BCrypt** - 密码加密
- **Caffeine** - 缓存
- **Micrometer** - 监控指标

### 测试
- **JUnit 5** - 单元测试
- **MockK** - Mock框架
- **Testcontainers** - 集成测试
- **WireMock** - API模拟

## 🏃‍♂️ 快速开始

### 环境要求
- JDK 17+
- Gradle 8.0+
- PostgreSQL 13+ (生产环境)

### 本地开发

1. **克隆项目**
```bash
git clone <repository-url>
cd user
```

2. **构建项目**
```bash
./gradlew build
```

3. **运行应用**
```bash
./gradlew bootRun
```

4. **访问应用**
- API文档: http://localhost:8080/swagger-ui.html
- H2控制台: http://localhost:8080/h2-console
- 健康检查: http://localhost:8080/actuator/health

### Docker部署

```bash
# 构建镜像
docker build -t user-service .

# 运行容器
docker run -p 8080:8080 user-service
```

## 📚 API文档

### 用户注册
```http
POST /api/v1/users/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "nickname": "用户昵称",
  "firstName": "张",
  "lastName": "三"
}
```

### 用户登录
```http
POST /api/v1/users/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "ipAddress": "192.168.1.1",
  "userAgent": "Mozilla/5.0..."
}
```

### 获取用户资料
```http
GET /api/v1/users/{userId}
Authorization: Bearer <jwt-token>
```

### 搜索用户
```http
GET /api/v1/users/search?keyword=张三&status=ACTIVE&page=0&size=20
Authorization: Bearer <jwt-token>
```

更多API详情请查看Swagger文档。

## 🔧 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/userdb
    username: ${DB_USERNAME:user}
    password: ${DB_PASSWORD:password}
```

### 密码策略配置
```yaml
user:
  password:
    policy:
      min-length: 8
      max-length: 128
      require-uppercase: true
      require-lowercase: true
      require-digit: true
      require-special-char: true
```

### 安全配置
```yaml
user:
  security:
    jwt:
      secret: "your-secret-key"
      expiration-hours: 24
    cors:
      allowed-origins:
        - "http://localhost:3000"
```

## 🧪 测试

### 运行所有测试
```bash
./gradlew test
```

### 运行特定测试
```bash
./gradlew test --tests "*UserServiceTest"
```

### 测试覆盖率
```bash
./gradlew jacocoTestReport
```

## 📊 监控

### 健康检查
```bash
curl http://localhost:8080/actuator/health
```

### 指标监控
```bash
curl http://localhost:8080/actuator/metrics
```

### Prometheus指标
```bash
curl http://localhost:8080/actuator/prometheus
```

## 🐛 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查数据库服务是否启动
   - 验证连接配置是否正确
   - 确认网络连通性

2. **JWT令牌验证失败**
   - 检查令牌是否过期
   - 验证密钥配置
   - 确认令牌格式正确

3. **密码策略验证失败**
   - 检查密码是否符合策略要求
   - 验证策略配置
   - 查看详细错误信息

### 日志查看
```bash
# 查看应用日志
tail -f logs/user-service.log

# 查看错误日志
grep ERROR logs/user-service.log
```

## 🤝 贡献指南

1. Fork项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

### 代码规范
- 遵循Kotlin编码规范
- 编写单元测试
- 添加适当的文档注释
- 确保代码覆盖率 > 80%

## 📄 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

- 项目维护者: [Your Name]
- 邮箱: your.email@example.com
- 项目地址: https://github.com/your-username/user-service

## 🗺️ 路线图

- [ ] 添加OAuth2支持
- [ ] 实现多因素认证(MFA)
- [ ] 添加用户行为分析
- [ ] 支持LDAP集成
- [ ] 实现用户数据导出
- [ ] 添加GraphQL API
- [ ] 支持多租户架构