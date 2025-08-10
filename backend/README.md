# Lifee Backend - 知识库管理系统后端

基于Spring Boot + Kotlin构建的微服务架构后端系统，采用DDD方法论和六边形架构，实现CQRS和事件驱动模式。

## 技术栈

- **框架**: Spring Boot 3.2+ + Kotlin 1.9+
- **数据库**: PostgreSQL 15 (关系数据 + 向量数据)
- **缓存**: Redis 7
- **消息队列**: Apache Kafka 3.5
- **CQRS实现**: 自定义实现CommandBus、QueryBus、EventBus
- **测试框架**: Kotest (BDD集成测试) + JUnit5 (单元测试)
- **容器化**: Docker + Kubernetes
- **监控**: Prometheus + Grafana + OpenTelemetry

## 项目结构

```
lifee-backend/
├── lifee-common/                    # 公共模块(CQRS基础组件)
├── lifee-user/                      # 用户领域模块
├── lifee-user-app/                  # 用户应用模块
├── lifee-knowledge/                 # 知识库领域模块
├── lifee-knowledge-app/             # 知识库应用模块
├── lifee-chat/                      # 对话领域模块
├── lifee-chat-app/                  # 对话应用模块
├── lifee-recommendation/            # 推荐领域模块
├── lifee-recommendation-app/        # 推荐应用模块
├── lifee-config/                    # 配置领域模块
├── lifee-config-app/                # 配置应用模块
├── lifee-gateway/                   # API网关
├── docker-compose.yml               # 本地开发环境
├── k8s/                            # Kubernetes部署配置
└── pom.xml                          # 根构建脚本
```

## 快速开始

### 环境要求

- Java 17+
- Maven 3.9+
- Docker & Docker Compose

### 1. 启动基础设施

```bash
# 启动PostgreSQL、Redis、Kafka等基础服务
docker-compose up -d

# 检查服务状态
docker-compose ps
```

### 2. 构建项目

```bash
# 编译所有模块
./mvnw clean compile

# 运行测试
./mvnw test

# 打包
./mvnw clean package
```

### 3. 运行应用

```bash
# 运行用户服务
cd lifee-user-app
../mvnw spring-boot:run

# 运行知识库服务
cd lifee-knowledge-app
../mvnw spring-boot:run
```

## 开发指南

### 架构原则

1. **领域驱动设计(DDD)**: 按业务领域划分模块
2. **六边形架构**: 领域层独立于基础设施
3. **CQRS模式**: 命令查询职责分离
4. **事件驱动**: 通过领域事件实现模块间解耦

### 代码规范

- 使用Kotlin编写所有业务代码
- 遵循DDD分层架构
- 单元测试覆盖率 > 90%
- 集成测试使用BDD风格

### 测试策略

```bash
# 运行单元测试
./mvnw test

# 运行集成测试
./mvnw verify

# 生成测试覆盖率报告
./mvnw jacoco:report
```

## 部署

### Docker部署

```bash
# 构建Docker镜像
./mvnw spring-boot:build-image

# 运行容器
docker run -p 8080:8080 lifee/user-app:latest
```

### Kubernetes部署

```bash
# 应用Kubernetes配置
kubectl apply -f k8s/

# 检查部署状态
kubectl get pods
```

## 监控

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Kafka UI**: http://localhost:8080

## API文档

启动应用后访问:
- 用户服务: http://localhost:8081/swagger-ui.html
- 知识库服务: http://localhost:8082/swagger-ui.html

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。