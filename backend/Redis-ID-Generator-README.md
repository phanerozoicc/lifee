# Redis全局ID生成器实现说明

## 概述

本项目已成功实现基于Redis的全局ID生成器，替换了原有的基于数据库序列的实现。新的实现使用Redisson客户端，提供了分布式环境下的高性能ID生成能力。

## 实现内容

### 1. 依赖配置

- **lifee-user模块**: 添加了Redisson依赖 (版本3.24.3)
- **lifee-user-app模块**: 添加了Redisson依赖 (版本3.24.3)

### 2. Redis配置

在`application.yml`中配置了Redis连接参数：

```yaml
# 开发环境
redisson:
  singleServerConfig:
    address: "redis://localhost:6379"
    database: 0

# 生产环境
redisson:
  singleServerConfig:
    address: "redis://your-redis-host:6379"
    password: "your-redis-password"
    database: 0
```

### 3. 核心实现

#### RedisUserIdGenerator

新的ID生成器实现，位于：
`lifee-user-app/src/main/kotlin/com/lifee/user/infrastructure/services/RedisUserIdGenerator.kt`

**主要功能：**
- 使用Redis原子递增操作生成唯一ID
- 保持原有的`U00000001`格式
- 提供异常处理和日志记录
- 支持计数器初始化

**关键方法：**
- `generateNext()`: 生成下一个用户ID
- `getCurrentSequence()`: 获取当前序列号
- `initializeCounter(initialValue)`: 初始化计数器

#### UserIdMigrationService

数据迁移服务，位于：
`lifee-user-app/src/main/kotlin/com/lifee/user/infrastructure/services/UserIdMigrationService.kt`

**功能：**
- 应用启动时自动同步数据库序列到Redis
- 确保ID生成的连续性
- 提供手动同步方法

### 4. 配置更新

#### UserIdGeneratorConfig

更新了Spring配置，将默认的`UserIdGenerator`实现从`DatabaseUserIdGenerator`切换到`RedisUserIdGenerator`。

### 5. 测试验证

#### 单元测试

`RedisUserIdGeneratorTest.kt`包含以下测试用例：
- ID格式验证
- 递增性验证
- 大数字处理
- 异常处理
- 当前序列号获取

## 使用方式

### 1. 启动Redis服务

确保Redis服务正在运行：
```bash
# 使用Docker启动Redis
docker run -d -p 6379:6379 redis:latest

# 或使用本地Redis服务
redis-server
```

### 2. 配置环境变量（生产环境）

```bash
export REDIS_HOST=your-redis-host
export REDIS_PASSWORD=your-redis-password
```

### 3. 启动应用

```bash
./mvnw spring-boot:run -pl lifee-user-app
```

### 4. 验证功能

应用启动后，`UserIdMigrationService`会自动将数据库序列同步到Redis，之后所有的用户ID生成都将使用Redis实现。

## 性能优势

1. **高并发支持**: Redis原子操作确保高并发环境下的ID唯一性
2. **低延迟**: 内存操作比数据库查询更快
3. **分布式友好**: 支持多实例部署
4. **可扩展性**: Redis集群支持水平扩展

## 注意事项

1. **Redis可用性**: 确保Redis服务的高可用性
2. **数据持久化**: 配置Redis持久化以防止数据丢失
3. **监控**: 监控Redis连接状态和性能指标
4. **备份**: 定期备份Redis数据

## 故障恢复

如果Redis服务不可用，可以通过以下步骤恢复：

1. 修改配置切换回`DatabaseUserIdGenerator`
2. 重启应用
3. 修复Redis问题后重新切换

## 监控指标

建议监控以下指标：
- Redis连接数
- ID生成响应时间
- Redis内存使用率
- 错误率和异常日志