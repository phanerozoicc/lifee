# LifeE系统事件风暴战略建模

## 概述

本文档记录了LifeE智能知识库系统的事件风暴工作坊成果，通过协作式的领域建模方法，识别了系统的核心业务流程、领域事件、命令、聚合和上下文边界。

## 事件风暴方法论

### 便签颜色约定
- 🟠 **橙色**：领域事件（Domain Events）- 业务中发生的重要事情
- 🔵 **蓝色**：命令（Commands）- 触发事件的用户意图
- 🟡 **黄色**：聚合（Aggregates）- 业务规则的执行边界
- 🟢 **绿色**：读模型/查询（Read Models/Queries）- 数据查询需求
- 🟣 **紫色**：策略（Policies）- 业务规则和自动化流程
- 🩷 **粉色**：外部系统（External Systems）- 系统边界外的依赖
- 🔴 **红色**：热点问题（Hotspots）- 需要进一步讨论的复杂点

## 1. 核心用户角色

### 1.1 主要用户
- **终端用户**: 使用智能知识库进行学习和工作的个人用户

## 核心用户旅程

### 1. 知识管理旅程
```
用户注册 → 创建知识库 → 上传文档 → 文档处理 → 索引构建 → 知识检索
```

### 2. AI对话旅程
```
开始对话 → 发送消息 → 知识检索 → AI响应生成 → 对话保存 → 内容归档
```

### 3. 智能推荐旅程
```
行为记录 → 偏好分析 → 推荐生成 → 学习轨迹 → 目标达成
```

## 领域事件识别

### 用户域事件
🟠 **UserRegistered** - 用户注册完成
🟠 **UserLoggedIn** - 用户登录成功
🟠 **UserProfileUpdated** - 用户资料更新

### 知识库域事件
🟠 **KnowledgeBaseCreated** - 知识库创建
🟠 **KnowledgeBaseConfigured** - 知识库配置完成
🟠 **DocumentUploaded** - 文档上传完成
🟠 **DocumentProcessed** - 文档解析处理完成
🟠 **DocumentIndexed** - 文档索引构建完成
🟠 **DocumentDeleted** - 文档删除
🟠 **DocumentVersionCreated** - 文档版本创建
🟠 **KnowledgeBaseShared** - 知识库共享

### 对话域事件
🟠 **ConversationStarted** - 对话会话开始
🟠 **MessageSent** - 用户消息发送
🟠 **KnowledgeRetrieved** - 相关知识检索完成
🟠 **AIResponseGenerated** - AI响应生成
🟠 **ConversationSaved** - 对话保存
🟠 **ConversationArchived** - 对话归档到知识库
🟠 **ConversationShared** - 对话分享

### 推荐域事件
🟠 **UserBehaviorRecorded** - 用户行为记录
🟠 **LearningGoalSet** - 学习目标设定
🟠 **RecommendationGenerated** - 推荐内容生成
🟠 **RecommendationClicked** - 推荐内容点击
🟠 **LearningProgressUpdated** - 学习进度更新
🟠 **UserPreferenceUpdated** - 用户偏好更新

### 配置域事件
🟠 **ModelConfigured** - AI模型配置
🟠 **SystemSettingsUpdated** - 系统设置更新
🟠 **ConfigurationImported** - 配置导入
🟠 **ConfigurationExported** - 配置导出
🟠 **ModelHealthChecked** - 模型健康检查

## 命令识别

### 用户域命令
🔵 **RegisterUser** - 注册用户
🔵 **LoginUser** - 用户登录
🔵 **UpdateUserProfile** - 更新用户资料

### 知识库域命令
🔵 **CreateKnowledgeBase** - 创建知识库
🔵 **ConfigureKnowledgeBase** - 配置知识库
🔵 **UploadDocument** - 上传文档
🔵 **ProcessDocument** - 处理文档
🔵 **DeleteDocument** - 删除文档
🔵 **SearchDocuments** - 搜索文档
🔵 **ShareKnowledgeBase** - 分享知识库

### 对话域命令
🔵 **StartConversation** - 开始对话
🔵 **SendMessage** - 发送消息
🔵 **RetrieveKnowledge** - 检索知识
🔵 **GenerateAIResponse** - 生成AI响应
🔵 **SaveConversation** - 保存对话
🔵 **ArchiveConversation** - 归档对话
🔵 **ShareConversation** - 分享对话

### 推荐域命令
🔵 **RecordUserBehavior** - 记录用户行为
🔵 **SetLearningGoal** - 设定学习目标
🔵 **GenerateRecommendations** - 生成推荐
🔵 **ClickRecommendation** - 点击推荐
🔵 **UpdateLearningProgress** - 更新学习进度

### 配置域命令
🔵 **ConfigureModel** - 配置模型
🔵 **UpdateSystemSettings** - 更新系统设置
🔵 **ImportConfiguration** - 导入配置
🔵 **ExportConfiguration** - 导出配置
🔵 **CheckModelHealth** - 检查模型健康

## 聚合设计

### 用户域聚合
🟡 **User聚合**
- 聚合根：User
- 实体：UserProfile
- 值对象：UserId, Email, Password
- 业务规则：密码复杂度、账户状态管理、用户只能访问自己的知识库

### 知识库域聚合
🟡 **KnowledgeBase聚合**
- 聚合根：KnowledgeBase
- 实体：Document, DocumentVersion
- 值对象：KnowledgeBaseId, DocumentId, FileMetadata
- 业务规则：文档数量限制、存储配额、访问权限

🟡 **Document聚合**
- 聚合根：Document
- 实体：DocumentChunk, DocumentIndex
- 值对象：Content, Embedding, Metadata
- 业务规则：文档格式验证、分块策略、索引更新、DocumentChunk通过DocumentId与Document关联，支持文档分块存储和检索

### 对话域聚合
🟡 **Conversation聚合**
- 聚合根：Conversation
- 实体：Message, ConversationContext
- 值对象：ConversationId, MessageId, MessageContent
- 业务规则：消息顺序、上下文管理、会话超时、对话可选关联知识库

### 推荐域聚合
🟡 **UserProfile聚合**
- 聚合根：UserProfile
- 实体：UserBehavior, LearningGoal
- 值对象：BehaviorRecord, Preference, LearningProgress
- 业务规则：行为权重、偏好计算、目标追踪

🟡 **Recommendation聚合**
- 聚合根：Recommendation
- 实体：RecommendationItem, RecommendationFeedback
- 值对象：RecommendationScore, RecommendationReason
- 业务规则：推荐算法、评分计算、反馈处理

### 配置域聚合
🟡 **ModelConfig聚合**
- 聚合根：ModelConfig
- 实体：ModelEndpoint, ModelParameter
- 值对象：ModelType, APIKey, ConfigValue
- 业务规则：配置验证、模型兼容性、参数范围

🟡 **SystemConfig聚合**
- 聚合根：SystemConfig
- 实体：ConfigurationItem, ConfigurationHistory
- 值对象：ConfigKey, ConfigValue, ConfigType
- 业务规则：配置依赖、版本管理、回滚策略

## 读模型和查询

### 用户域查询
🟢 **GetUserProfile** - 获取用户资料
🟢 **GetUserPermissions** - 获取用户权限
🟢 **ListUsers** - 用户列表查询
🟢 **SearchUsers** - 用户搜索

### 知识库域查询
🟢 **SearchDocuments** - 文档搜索（语义+关键词）
🟢 **GetDocumentDetails** - 文档详情
🟢 **ListKnowledgeBases** - 知识库列表
🟢 **GetKnowledgeBaseStatistics** - 知识库统计
🟢 **GetDocumentVersionHistory** - 文档版本历史

### 对话域查询
🟢 **GetConversationHistory** - 对话历史
🟢 **SearchConversations** - 对话搜索
🟢 **GetConversationSummary** - 对话摘要
🟢 **GetMessageContext** - 消息上下文

### 推荐域查询
🟢 **GetPersonalizedRecommendations** - 个性化推荐
🟢 **GetLearningProgress** - 学习进度
🟢 **GetUserBehaviorAnalytics** - 用户行为分析
🟢 **GetRecommendationFeedback** - 推荐反馈

### 配置域查询
🟢 **GetModelConfigurations** - 模型配置
🟢 **GetSystemSettings** - 系统设置
🟢 **GetConfigurationHistory** - 配置历史
🟢 **GetModelHealthStatus** - 模型健康状态

## 业务策略

### 自动化策略
🟣 **文档处理策略**
- 当DocumentUploaded事件发生时，自动触发ProcessDocument命令
- 当DocumentProcessed事件发生时，自动触发文档索引构建

🟣 **推荐生成策略**
- 当UserBehaviorRecorded事件累积到一定数量时，触发推荐生成
- 基于记忆曲线算法，定时生成复习推荐

🟣 **对话归档策略**
- 当对话标记为重要时，自动归档到指定知识库
- 定期清理过期的临时对话

🟣 **模型健康检查策略**
- 定时检查AI模型的可用性和响应时间
- 当模型异常时，自动切换到备用模型

## 外部系统集成

### AI服务提供商
🩷 **OpenAI API** - GPT模型服务
🩷 **Anthropic Claude** - Claude模型服务
🩷 **本地LLM服务** - Ollama等本地模型
🩷 **嵌入模型服务** - 文本向量化服务

### 基础设施服务
🩷 **PostgreSQL** - 主数据库
🩷 **Redis** - 缓存和会话存储
🩷 **MinIO** - 对象存储服务
🩷 **Kafka** - 消息队列
🩷 **Elasticsearch** - 搜索引擎（可选）

### 监控和运维
🩷 **Prometheus** - 指标收集
🩷 **Grafana** - 监控面板
🩷 **Jaeger** - 分布式追踪
🩷 **ALG Stack** - 日志分析

## 上下文边界划分

### 1. 用户上下文（User Context）
**职责**：用户管理、认证授权
**核心聚合**：User
**对外接口**：用户注册、登录、用户资料管理
**发布事件**：UserRegistered, UserLoggedIn, UserProfileUpdated
**订阅事件**：无（独立上下文）

### 2. 知识库上下文（Knowledge Context）
**职责**：文档管理、索引构建、搜索检索
**核心聚合**：KnowledgeBase, Document
**对外接口**：文档上传、搜索、管理
**发布事件**：DocumentUploaded, DocumentIndexed, KnowledgeBaseCreated
**订阅事件**：UserRegistered（创建默认知识库）

### 3. 对话上下文（Conversation Context）
**职责**：AI对话、消息管理、会话控制
**核心聚合**：Conversation
**对外接口**：对话管理、消息发送、AI响应
**发布事件**：ConversationStarted, MessageSent, AIResponseGenerated
**订阅事件**：DocumentIndexed（更新可用知识源）, ModelConfigured（更新模型设置）

### 4. 推荐上下文（Recommendation Context）
**职责**：行为分析、智能推荐、学习轨迹
**核心聚合**：UserProfile, Recommendation
**对外接口**：推荐生成、行为记录、学习分析
**发布事件**：RecommendationGenerated, UserBehaviorRecorded
**订阅事件**：UserRegistered（创建用户画像）, MessageSent（记录行为）

### 5. 配置上下文（Configuration Context）
**职责**：模型配置、系统设置、参数管理
**核心聚合**：ModelConfig, SystemConfig
**对外接口**：配置管理、导入导出、健康检查
**发布事件**：ModelConfigured, SystemSettingsUpdated
**订阅事件**：无（独立配置上下文）

## 上下文映射

### 上下文关系类型

#### 1. 用户上下文 ↔ 其他上下文
- **关系类型**：开放主机服务（Open Host Service）
- **集成方式**：用户上下文提供统一的用户认证和权限服务
- **防腐层**：其他上下文通过适配器访问用户服务

#### 2. 知识库上下文 → 对话上下文
- **关系类型**：发布者-订阅者（Publisher-Subscriber）
- **集成方式**：知识库发布文档索引事件，对话上下文订阅更新知识源
- **事件**：DocumentIndexed → 更新对话可用知识

#### 3. 对话上下文 → 推荐上下文
- **关系类型**：发布者-订阅者（Publisher-Subscriber）
- **集成方式**：对话发布用户行为事件，推荐上下文订阅分析行为
- **事件**：MessageSent → 记录用户交互行为

#### 4. 配置上下文 → 对话上下文
- **关系类型**：供应商-客户（Supplier-Customer）
- **集成方式**：配置上下文提供模型配置，对话上下文消费配置
- **事件**：ModelConfigured → 更新对话模型设置

#### 5. 推荐上下文 → 用户上下文
- **关系类型**：客户-供应商（Customer-Supplier）
- **集成方式**：推荐上下文消费用户信息，向用户上下文反馈偏好
- **事件**：UserPreferenceUpdated → 更新用户偏好设置

## 热点问题识别

🔴 **性能热点**
- 大文档的向量化处理性能
- 实时搜索的响应时间
- 高并发对话的处理能力

🔴 **一致性热点**
- 跨上下文的数据一致性
- 事件顺序和幂等性
- 分布式事务处理

🔴 **安全热点**
- 用户数据隐私保护
- AI模型的安全调用
- 跨域访问控制

🔴 **扩展性热点**
- 多租户数据隔离
- 微服务拆分策略
- 插件系统架构

## 实施优先级

### 第一阶段：核心MVP
1. **用户上下文**：基础用户管理和认证
2. **知识库上下文**：文档上传和基础搜索
3. **对话上下文**：基础AI对话功能
4. **配置上下文**：基础模型配置

### 第二阶段：功能完善
1. **推荐上下文**：智能推荐系统
2. **知识库上下文**：高级搜索和版本管理
3. **对话上下文**：对话归档和分享
4. **用户上下文**：权限管理和团队协作

### 第三阶段：高级特性
1. **跨上下文集成**：完整的事件驱动架构
2. **性能优化**：缓存、索引、查询优化
3. **扩展功能**：插件系统、API开放
4. **运维支持**：监控、日志、健康检查

## 架构决策记录（ADR）

### ADR-001：事件驱动架构
**决策**：采用事件驱动架构进行上下文间集成
**理由**：松耦合、可扩展、支持异步处理
**后果**：需要处理事件顺序和一致性问题

### ADR-002：CQRS模式
**决策**：在复杂查询场景使用CQRS模式
**理由**：优化查询性能，支持复杂的读模型
**后果**：增加系统复杂度，需要维护读写模型一致性

### ADR-003：聚合边界
**决策**：以业务一致性为边界划分聚合
**理由**：确保业务规则的完整性和数据一致性
**后果**：可能需要跨聚合的最终一致性处理

### ADR-004：上下文集成
**决策**：通过领域事件进行上下文集成
**理由**：保持上下文的独立性和松耦合
**后果**：需要设计事件版本兼容性和错误处理

## 下一步行动

1. **创建PlantUML事件风暴图**：可视化展示事件流和上下文关系
2. **设计领域模型代码结构**：基于聚合设计创建代码骨架
3. **定义事件和命令接口**：标准化跨上下文通信协议
4. **实现核心聚合**：从最重要的用户价值开始实现
5. **建立事件基础设施**：消息队列、事件存储、事件处理器
6. **创建集成测试**：验证跨上下文的业务流程

---

**文档版本**：1.0  
**创建日期**：2024年  
**最后更新**：2024年  
**负责人**：LifeE开发团队  
**审核状态**：待审核