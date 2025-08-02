# LifeE 技术架构设计文档

## 概述

LifeE是一个基于领域驱动设计(DDD)和六边形架构的智能知识库Web应用，采用多模块单体架构，支持水平扩展和微服务演进。

本文档整合了技术架构设计和架构优化调整，基于用户反馈和实际开发考虑，对系统架构进行了优化，旨在提高系统的可维护性、降低复杂度，并确保技术选型的合理性。

## 架构原则

### 1. 设计原则
- **领域驱动设计(DDD)**: 以业务领域为核心的设计方法
- **六边形架构**: 端口适配器模式，实现业务逻辑与技术实现的解耦
- **CQRS**: 命令查询职责分离，优化读写性能
- **事件驱动**: 基于领域事件的松耦合架构
- **单一职责**: 每个模块专注于特定的业务能力

### 2. 质量属性
- **可扩展性**: 支持水平扩展和垂直扩展
- **可维护性**: 清晰的模块边界和依赖关系
- **可测试性**: 高内聚低耦合的设计
- **安全性**: 多层次的安全防护
- **性能**: 亚秒级响应时间

## 架构优化调整

### 1. 搜索策略优化

#### 原方案
- Elasticsearch + pgvector双搜索引擎
- 增加系统复杂度和运维成本

#### 优化方案
```yaml
初期方案:
  - PostgreSQL全文搜索（内置FTS功能）
  - pgvector向量搜索（语义相似度）
  - 混合搜索算法（关键词 + 语义）

后期扩展:
  - 根据业务量级考虑引入Elasticsearch
  - 提供更强大的全文搜索能力
  - 支持复杂的聚合查询和分析
```

#### 优化理由
1. **降低初期复杂度**：避免过度设计，专注核心功能
2. **减少运维成本**：少一个组件意味着更简单的部署和维护
3. **PostgreSQL FTS能力**：足以满足初期全文搜索需求
4. **渐进式架构**：保留后期升级到Elasticsearch的可能性

#### 技术实现
```sql
-- PostgreSQL全文搜索示例
CREATE INDEX idx_documents_fts ON documents 
USING gin(to_tsvector('english', title || ' ' || content));

-- 混合搜索查询
SELECT d.*, 
       ts_rank(to_tsvector('english', d.title || ' ' || d.content), query) as text_score,
       1 - (d.embedding <=> query_embedding) as semantic_score,
       (ts_rank(...) * 0.3 + (1 - (d.embedding <=> query_embedding)) * 0.7) as final_score
FROM documents d, plainto_tsquery('english', ?) query
WHERE to_tsvector('english', d.title || ' ' || d.content) @@ query
ORDER BY final_score DESC;
```

## 整体架构

### 1. 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层 (Frontend)                      │
├─────────────────────────────────────────────────────────────┤
│  React 18 + TypeScript + Next.js 14 + Tailwind CSS        │
│  • 知识库管理界面  • 对话交互界面  • 推荐系统界面  • 配置界面    │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ HTTP/WebSocket
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      API网关层 (API Gateway)                  │
├─────────────────────────────────────────────────────────────┤
│  Spring Cloud Gateway / Nginx                              │
│  • 路由转发  • 负载均衡  • 限流熔断  • 认证授权  • 日志监控     │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      应用服务层 (Application)                  │
├─────────────────────────────────────────────────────────────┤
│  Spring Boot 3.x + Kotlin                                  │
│  ┌─────────────┬─────────────┬─────────────┬─────────────┐   │
│  │  用户服务    │  知识库服务   │  对话服务    │  推荐服务    │   │
│  │ UserService │KnowledgeServ│ConversationS│RecommendServ│   │
│  └─────────────┴─────────────┴─────────────┴─────────────┘   │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      领域层 (Domain)                         │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┬─────────────┬─────────────┬─────────────┐   │
│  │   用户域     │   知识库域   │   对话域     │   推荐域     │   │
│  │ User Domain │Knowledge Dom│Conversation │Recommend Dom│   │
│  │             │ain          │Domain       │ain          │   │
│  └─────────────┴─────────────┴─────────────┴─────────────┘   │
│  • 聚合根  • 实体  • 值对象  • 领域服务  • 领域事件           │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    基础设施层 (Infrastructure)                 │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┬─────────────┬─────────────┬─────────────┐   │
│  │  数据持久化  │   消息队列   │   外部服务   │   文件存储   │   │
│  │ PostgreSQL  │  RabbitMQ   │  LLM APIs   │   MinIO     │   │
│  │   Redis     │ (初期方案)   │  MCP服务    │             │   │
│  │  pgvector   │             │             │             │   │
│  └─────────────┴─────────────┴─────────────┴─────────────┘   │
│  注：Elasticsearch作为后期扩展选项                              │
└─────────────────────────────────────────────────────────────┘
```

### 2. 模块架构优化

#### 原方案（技术层次划分）
```
lifee-domain（领域层）
lifee-application（应用层）
lifee-infrastructure（基础设施层）
lifee-web（Web接口层）
```

#### 优化方案（功能域横向拆分）
```
lifee-backend/
├── user/                      # 用户域
│   └── user-app/              # 用户应用服务
├── knowledge/                 # 知识库域
│   └── knowledge-app/         # 知识库应用服务
├── conversation/              # 对话域
│   └── conversation-app/      # 对话应用服务
├── recommendation/            # 推荐域
│   └── recommendation-app/    # 推荐应用服务
├── config/                    # 配置域
│   └── config-app/            # 配置应用服务
└── shared/                    # 共享组件
    ├── common/                # 通用工具
    ├── security/              # 安全组件
    └── monitoring/            # 监控组件
```

#### 优化理由
1. **符合DDD原则**：按业务边界而非技术层次划分
2. **团队协作友好**：不同团队可以独立开发不同域
3. **微服务演进**：为后期微服务拆分奠定基础
4. **技术栈灵活性**：不同域可以选择不同的技术实现

#### 模块依赖关系
```mermaid
graph TD
    A[user-app] --> B[user]
    C[knowledge-app] --> D[knowledge]
    E[conversation-app] --> F[conversation]
    G[recommendation-app] --> H[recommendation]
    I[config-app] --> J[config]
    
    A --> K[shared]
    C --> K
    E --> K
    G --> K
    I --> K
    
    E --> D  # 对话域依赖知识库域
    G --> B  # 推荐域依赖用户域
    G --> D  # 推荐域依赖知识库域
```

### 3. 日志框架优化

#### 原方案
- Spring Boot默认Logback

#### 优化方案
- Log4j2替代Logback

#### 优化理由
1. **性能优势**：Log4j2的异步日志性能更优
2. **内存效率**：更好的垃圾回收表现
3. **功能丰富**：支持更多的日志格式和输出方式
4. **配置灵活**：支持动态配置重载

#### 配置示例
```xml
<!-- log4j2-spring.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
        <RollingFile name="RollingFile" fileName="logs/lifee.log"
                     filePattern="logs/lifee-%d{yyyy-MM-dd}-%i.log.gz">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
            <Policies>
                <TimeBasedTriggeringPolicy/>
                <SizeBasedTriggeringPolicy size="100MB"/>
            </Policies>
            <DefaultRolloverStrategy max="10"/>
        </RollingFile>
    </Appenders>
    <Loggers>
        <AsyncLogger name="com.lifee" level="INFO" additivity="false">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="RollingFile"/>
        </AsyncLogger>
        <Root level="WARN">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

### 4. 消息队列策略优化

#### 原方案
- 直接使用Kafka


### 5. 原型驱动开发

#### 新增流程
在每个开发阶段增加原型验证环节：

```yaml
原型验证流程:
  1. 领域模型设计完成
  2. 核心功能原型开发
  3. 技术可行性验证
  4. 性能基准测试
  5. 架构决策记录（ADR）
  6. 进入正式开发阶段
```

#### 原型验证内容
1. **技术可行性**：验证关键技术点是否可行
2. **性能基准**：建立性能基线，避免后期性能问题
3. **用户体验**：早期获得用户反馈
4. **架构验证**：确保架构设计的合理性
│   ├── websocket/            # WebSocket
│   └── graphql/              # GraphQL (可选)
└── lifee-shared/             # 共享模块
    ├── common/               # 通用工具
    ├── security/             # 安全组件
    └── monitoring/           # 监控组件
```

## 技术栈详解

### 1. 后端技术栈

#### 核心框架
```kotlin
// Spring Boot 3.x 配置
@SpringBootApplication
@EnableJpaRepositories
@EnableRabbitMQ
@EnableRedisRepositories
class LifeeApplication

// 依赖管理 (build.gradle.kts)
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // 排除默认的Logback，使用Log4j2
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    configurations.all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    
    // Kotlin支持
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    
    // 数据库
    implementation("org.postgresql:postgresql")
    implementation("com.pgvector:pgvector:0.1.4")
    implementation("org.flywaydb:flyway-core")
    
    // 文档处理
    implementation("org.apache.tika:tika-core:2.9.1")
    implementation("org.apache.tika:tika-parsers-standard-package:2.9.1")
    
    // HTTP客户端
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.netty:netty-resolver-dns-native-macos:4.1.100.Final")
    
    // 监控
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("net.logstash.logback:logstash-logback-encoder")
}
```

#### 数据访问层
```kotlin
// JPA实体示例
@Entity
@Table(name = "knowledge_bases")
data class KnowledgeBaseEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(nullable = false)
    val name: String,
    
    val description: String?,
    
    @Column(name = "owner_id", nullable = false)
    val ownerId: UUID,
    
    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    val config: KnowledgeBaseConfig = KnowledgeBaseConfig(),
    
    @Enumerated(EnumType.STRING)
    val status: KnowledgeBaseStatus = KnowledgeBaseStatus.ACTIVE,
    
    @CreationTimestamp
    val createdAt: Instant = Instant.now(),
    
    @UpdateTimestamp
    val updatedAt: Instant = Instant.now()
)

// Repository接口
@Repository
interface KnowledgeBaseRepository : JpaRepository<KnowledgeBaseEntity, UUID> {
    fun findByOwnerIdAndStatus(ownerId: UUID, status: KnowledgeBaseStatus): List<KnowledgeBaseEntity>
    
    @Query("""
        SELECT kb FROM KnowledgeBaseEntity kb 
        WHERE kb.ownerId = :userId 
        OR kb.id IN (
            SELECT tm.team.knowledgeBaseId FROM TeamMemberEntity tm 
            WHERE tm.userId = :userId
        )
    """)
    fun findAccessibleByUser(userId: UUID): List<KnowledgeBaseEntity>
}
```

#### 向量数据库集成
```kotlin
// pgvector支持
@Entity
@Table(name = "document_chunks")
data class DocumentChunkEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "document_id", nullable = false)
    val documentId: UUID,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,
    
    @Column(columnDefinition = "vector(1536)")
    val embedding: FloatArray?,
    
    val sequence: Int,
    
    @CreationTimestamp
    val createdAt: Instant = Instant.now()
)

// 向量搜索Repository
@Repository
interface DocumentChunkRepository : JpaRepository<DocumentChunkEntity, UUID> {
    
    @Query(value = """
        SELECT *, (embedding <=> CAST(:queryEmbedding AS vector)) AS distance
        FROM document_chunks dc
        JOIN documents d ON dc.document_id = d.id
        WHERE d.knowledge_base_id = :knowledgeBaseId
        ORDER BY distance
        LIMIT :limit
    """, nativeQuery = true)
    fun findSimilarChunks(
        knowledgeBaseId: UUID,
        queryEmbedding: String,
        limit: Int
    ): List<DocumentChunkWithDistance>
}
```

### 2. 前端技术栈

#### 核心配置
```typescript
// next.config.js
const nextConfig = {
  experimental: {
    appDir: true,
  },
  typescript: {
    ignoreBuildErrors: false,
  },
  eslint: {
    ignoreDuringBuilds: false,
  },
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL,
    NEXT_PUBLIC_WS_URL: process.env.NEXT_PUBLIC_WS_URL,
  },
}

module.exports = nextConfig

// package.json依赖
{
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "next": "^14.0.0",
    "typescript": "^5.0.0",
    "tailwindcss": "^3.3.0",
    "@tanstack/react-query": "^5.0.0",
    "zustand": "^4.4.0",
    "react-hook-form": "^7.45.0",
    "@hookform/resolvers": "^3.3.0",
    "zod": "^3.22.0",
    "lucide-react": "^0.290.0",
    "@radix-ui/react-dialog": "^1.0.5",
    "@radix-ui/react-dropdown-menu": "^2.0.6",
    "@radix-ui/react-toast": "^1.1.5",
    "react-markdown": "^9.0.0",
    "remark-gfm": "^4.0.0",
    "rehype-highlight": "^7.0.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.0",
    "@types/react-dom": "^18.2.0",
    "@types/node": "^20.0.0",
    "eslint": "^8.0.0",
    "eslint-config-next": "^14.0.0",
    "prettier": "^3.0.0",
    "@tailwindcss/typography": "^0.5.10"
  }
}
```

#### 状态管理
```typescript
// stores/useAuthStore.ts
import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface User {
  id: string
  username: string
  email: string
  displayName: string
  avatarUrl?: string
}

interface AuthState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  login: (user: User, token: string) => void
  logout: () => void
  updateUser: (user: Partial<User>) => void
}

export const useAuthStore = create<AuthState>()()
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      
      login: (user, token) => {
        set({ user, token, isAuthenticated: true })
      },
      
      logout: () => {
        set({ user: null, token: null, isAuthenticated: false })
      },
      
      updateUser: (userData) => {
        const currentUser = get().user
        if (currentUser) {
          set({ user: { ...currentUser, ...userData } })
        }
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({ 
        user: state.user, 
        token: state.token, 
        isAuthenticated: state.isAuthenticated 
      }),
    }
  )
)
```

#### API客户端
```typescript
// lib/api-client.ts
import { useAuthStore } from '@/stores/useAuthStore'

class ApiClient {
  private baseURL: string
  
  constructor() {
    this.baseURL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
  }
  
  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const { token } = useAuthStore.getState()
    
    const config: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        ...(token && { Authorization: `Bearer ${token}` }),
        ...options.headers,
      },
      ...options,
    }
    
    const response = await fetch(`${this.baseURL}${endpoint}`, config)
    
    if (!response.ok) {
      throw new Error(`API Error: ${response.status} ${response.statusText}`)
    }
    
    return response.json()
  }
  
  // 知识库API
  knowledgeBases = {
    list: () => this.request<KnowledgeBase[]>('/api/knowledge-bases'),
    create: (data: CreateKnowledgeBaseRequest) => 
      this.request<KnowledgeBase>('/api/knowledge-bases', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    get: (id: string) => this.request<KnowledgeBase>(`/api/knowledge-bases/${id}`),
    update: (id: string, data: UpdateKnowledgeBaseRequest) =>
      this.request<KnowledgeBase>(`/api/knowledge-bases/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data),
      }),
    delete: (id: string) => 
      this.request<void>(`/api/knowledge-bases/${id}`, { method: 'DELETE' }),
  }
  
  // 对话API
  conversations = {
    list: () => this.request<Conversation[]>('/api/conversations'),
    create: (data: CreateConversationRequest) =>
      this.request<Conversation>('/api/conversations', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    get: (id: string) => this.request<Conversation>(`/api/conversations/${id}`),
    sendMessage: (id: string, message: string) =>
      this.request<Message>(`/api/conversations/${id}/messages`, {
        method: 'POST',
        body: JSON.stringify({ content: message }),
      }),
  }
}

export const apiClient = new ApiClient()
```

## 核心功能实现

### 1. 文档处理流水线

```kotlin
@Service
class DocumentProcessingService(
    private val documentRepository: DocumentRepository,
    private val chunkRepository: DocumentChunkRepository,
    private val embeddingService: EmbeddingService,
    private val eventPublisher: ApplicationEventPublisher
) {
    
    @Async
    fun processDocument(documentId: UUID) {
        try {
            val document = documentRepository.findById(documentId)
                .orElseThrow { DocumentNotFoundException(documentId) }
            
            // 1. 解析文档内容
            val content = parseDocumentContent(document)
            
            // 2. 文本分块
            val chunks = chunkText(content, document.knowledgeBaseId)
            
            // 3. 生成向量嵌入
            val chunksWithEmbeddings = generateEmbeddings(chunks)
            
            // 4. 保存到数据库
            chunkRepository.saveAll(chunksWithEmbeddings)
            
            // 5. 更新文档状态
            document.status = DocumentStatus.COMPLETED
            document.processedAt = Instant.now()
            document.chunkCount = chunks.size
            documentRepository.save(document)
            
            // 6. 发布事件
            eventPublisher.publishEvent(
                DocumentProcessedEvent(documentId, chunks.size)
            )
            
        } catch (e: Exception) {
            handleProcessingError(documentId, e)
        }
    }
    
    private fun parseDocumentContent(document: DocumentEntity): String {
        return when (document.type) {
            "pdf" -> parsePdf(document.fileUrl)
            "docx" -> parseDocx(document.fileUrl)
            "txt" -> parseText(document.fileUrl)
            "md" -> parseMarkdown(document.fileUrl)
            else -> throw UnsupportedDocumentTypeException(document.type)
        }
    }
    
    private fun chunkText(
        content: String, 
        knowledgeBaseId: UUID
    ): List<DocumentChunkEntity> {
        val config = getKnowledgeBaseConfig(knowledgeBaseId)
        val chunkSize = config.chunkSize
        val overlap = config.chunkOverlap
        
        return TextSplitter.splitText(
            text = content,
            chunkSize = chunkSize,
            overlap = overlap
        ).mapIndexed { index, chunk ->
            DocumentChunkEntity(
                documentId = documentId,
                content = chunk,
                sequence = index,
                tokenCount = countTokens(chunk)
            )
        }
    }
}
```

### 2. RAG检索增强生成

```kotlin
@Service
class RAGService(
    private val chunkRepository: DocumentChunkRepository,
    private val embeddingService: EmbeddingService,
    private val llmService: LLMService,
    private val promptTemplateService: PromptTemplateService
) {
    
    suspend fun generateResponse(
        query: String,
        knowledgeBaseId: UUID,
        conversationContext: List<Message> = emptyList()
    ): RAGResponse {
        
        // 1. 生成查询向量
        val queryEmbedding = embeddingService.generateEmbedding(query)
        
        // 2. 检索相关文档块
        val relevantChunks = chunkRepository.findSimilarChunks(
            knowledgeBaseId = knowledgeBaseId,
            queryEmbedding = queryEmbedding.toString(),
            limit = 5
        )
        
        // 3. 构建上下文
        val context = buildContext(relevantChunks, query)
        
        // 4. 生成提示词
        val prompt = promptTemplateService.renderRAGPrompt(
            query = query,
            context = context,
            conversationHistory = conversationContext
        )
        
        // 5. 调用LLM生成回答
        val response = llmService.generateResponse(prompt)
        
        return RAGResponse(
            answer = response.content,
            sources = relevantChunks.map { it.toSource() },
            confidence = calculateConfidence(relevantChunks),
            tokenUsage = response.tokenUsage
        )
    }
    
    private fun buildContext(
        chunks: List<DocumentChunkWithDistance>,
        query: String
    ): String {
        return chunks
            .filter { it.distance < 0.8 } // 相似度阈值
            .sortedBy { it.distance }
            .take(3)
            .joinToString("\n\n") { chunk ->
                "文档: ${chunk.documentName}\n内容: ${chunk.content}"
            }
    }
}
```

### 3. 智能推荐系统

```kotlin
@Service
class RecommendationEngine(
    private val behaviorRepository: UserBehaviorRepository,
    private val documentRepository: DocumentRepository,
    private val progressRepository: LearningProgressRepository,
    private val embeddingService: EmbeddingService
) {
    
    fun generateRecommendations(
        userId: UUID,
        limit: Int = 10
    ): List<RecommendationItem> {
        
        // 1. 获取用户行为数据
        val userBehaviors = behaviorRepository.findByUserIdOrderByCreatedAtDesc(
            userId, PageRequest.of(0, 100)
        )
        
        // 2. 分析用户兴趣
        val userInterests = analyzeUserInterests(userBehaviors)
        
        // 3. 获取学习进度
        val learningProgress = progressRepository.findByUserId(userId)
        
        // 4. 计算遗忘曲线
        val forgettingCurveItems = calculateForgettingCurve(learningProgress)
        
        // 5. 基于内容的推荐
        val contentBasedItems = generateContentBasedRecommendations(
            userInterests, userId
        )
        
        // 6. 协同过滤推荐
        val collaborativeItems = generateCollaborativeRecommendations(
            userId, userBehaviors
        )
        
        // 7. 合并和排序
        return mergeAndRankRecommendations(
            forgettingCurveItems,
            contentBasedItems,
            collaborativeItems
        ).take(limit)
    }
    
    private fun calculateForgettingCurve(
        progressList: List<LearningProgressEntity>
    ): List<RecommendationItem> {
        return progressList
            .filter { it.nextReviewAt?.isBefore(Instant.now()) == true }
            .map { progress ->
                RecommendationItem(
                    documentId = progress.documentId,
                    type = RecommendationType.REVIEW,
                    score = calculateReviewScore(progress),
                    reason = "基于遗忘曲线，建议复习此文档"
                )
            }
    }
    
    private fun generateContentBasedRecommendations(
        userInterests: UserInterestProfile,
        userId: UUID
    ): List<RecommendationItem> {
        // 基于用户兴趣向量和文档向量的相似度计算
        val interestEmbedding = userInterests.toEmbedding()
        
        return documentRepository.findSimilarDocuments(
            embedding = interestEmbedding,
            excludeUserId = userId,
            limit = 20
        ).map { document ->
            RecommendationItem(
                documentId = document.id,
                type = RecommendationType.CONTENT_BASED,
                score = document.similarity,
                reason = "基于您的兴趣偏好推荐"
            )
        }
    }
}
```

### 4. 实时通信

```kotlin
@Component
class ConversationWebSocketHandler : TextWebSocketHandler() {
    
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()
    private val userSessions = ConcurrentHashMap<UUID, MutableSet<String>>()
    
    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = extractUserId(session)
        val sessionId = session.id
        
        sessions[sessionId] = session
        userSessions.computeIfAbsent(userId) { mutableSetOf() }.add(sessionId)
        
        logger.info("WebSocket连接建立: userId=$userId, sessionId=$sessionId")
    }
    
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            val request = objectMapper.readValue(
                message.payload, 
                ConversationMessage::class.java
            )
            
            when (request.type) {
                "SEND_MESSAGE" -> handleSendMessage(session, request)
                "TYPING" -> handleTyping(session, request)
                "JOIN_CONVERSATION" -> handleJoinConversation(session, request)
                "LEAVE_CONVERSATION" -> handleLeaveConversation(session, request)
            }
            
        } catch (e: Exception) {
            sendError(session, "消息处理失败: ${e.message}")
        }
    }
    
    private suspend fun handleSendMessage(
        session: WebSocketSession, 
        request: ConversationMessage
    ) {
        val userId = extractUserId(session)
        val conversationId = request.conversationId
        
        // 保存用户消息
        val userMessage = messageService.saveMessage(
            conversationId = conversationId,
            type = MessageType.USER,
            content = request.content,
            userId = userId
        )
        
        // 广播用户消息
        broadcastToConversation(conversationId, userMessage)
        
        // 异步生成AI回复
        GlobalScope.launch {
            try {
                val aiResponse = ragService.generateResponse(
                    query = request.content,
                    knowledgeBaseId = request.knowledgeBaseId,
                    conversationContext = getConversationHistory(conversationId)
                )
                
                val aiMessage = messageService.saveMessage(
                    conversationId = conversationId,
                    type = MessageType.ASSISTANT,
                    content = aiResponse.answer,
                    metadata = mapOf(
                        "sources" to aiResponse.sources,
                        "tokenUsage" to aiResponse.tokenUsage
                    )
                )
                
                broadcastToConversation(conversationId, aiMessage)
                
            } catch (e: Exception) {
                sendError(session, "AI回复生成失败: ${e.message}")
            }
        }
    }
    
    private fun broadcastToConversation(
        conversationId: UUID, 
        message: MessageEntity
    ) {
        val participants = conversationService.getParticipants(conversationId)
        
        participants.forEach { userId ->
            userSessions[userId]?.forEach { sessionId ->
                sessions[sessionId]?.let { session ->
                    sendMessage(session, message.toWebSocketMessage())
                }
            }
        }
    }
}
```

## 部署架构

### 1. 容器化部署

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

VOLUME /tmp
VOLUME /app/logs
VOLUME /app/uploads

COPY build/libs/lifee-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'

services:
  # 应用服务
  lifee-app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DATABASE_URL=jdbc:postgresql://postgres:5432/lifee
      - REDIS_URL=redis://redis:6379
      - ELASTICSEARCH_URL=http://elasticsearch:9200
    depends_on:
      - postgres
      - redis
      - elasticsearch
      - rabbitmq
    volumes:
      - ./logs:/app/logs
      - ./uploads:/app/uploads
    networks:
      - lifee-network
  
  # PostgreSQL数据库
  postgres:
    image: pgvector/pgvector:pg15
    environment:
      - POSTGRES_DB=lifee
      - POSTGRES_USER=lifee
      - POSTGRES_PASSWORD=lifee123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - lifee-network
  
  # Redis缓存
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - lifee-network
  
  # Elasticsearch搜索引擎
  elasticsearch:
    image: elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data
    networks:
      - lifee-network
  
  # RabbitMQ消息队列
  rabbitmq:
    image: rabbitmq:3-management
    environment:
      - RABBITMQ_DEFAULT_USER=lifee
      - RABBITMQ_DEFAULT_PASS=lifee123
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    networks:
      - lifee-network
  
  # MinIO文件存储
  minio:
    image: minio/minio
    command: server /data --console-address ":9001"
    environment:
      - MINIO_ROOT_USER=lifee
      - MINIO_ROOT_PASSWORD=lifee123
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_data:/data
    networks:
      - lifee-network
  
  # 前端应用
  lifee-frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    ports:
      - "3000:3000"
    environment:
      - NEXT_PUBLIC_API_URL=http://localhost:8080
      - NEXT_PUBLIC_WS_URL=ws://localhost:8080
    depends_on:
      - lifee-app
    networks:
      - lifee-network

volumes:
  postgres_data:
  redis_data:
  elasticsearch_data:
  rabbitmq_data:
  minio_data:

networks:
  lifee-network:
    driver: bridge
```

### 2. Kubernetes部署

```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: lifee

---
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: lifee-config
  namespace: lifee
data:
  application.yml: |
    spring:
      profiles:
        active: kubernetes
      datasource:
        url: jdbc:postgresql://postgres-service:5432/lifee
        username: lifee
        password: lifee123
      redis:
        host: redis-service
        port: 6379
      elasticsearch:
        uris: http://elasticsearch-service:9200
      rabbitmq:
        host: rabbitmq-service
        port: 5672
        username: lifee
        password: lifee123

---
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: lifee-app
  namespace: lifee
spec:
  replicas: 3
  selector:
    matchLabels:
      app: lifee-app
  template:
    metadata:
      labels:
        app: lifee-app
    spec:
      containers:
      - name: lifee-app
        image: lifee/lifee-app:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        - name: logs-volume
          mountPath: /app/logs
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
      volumes:
      - name: config-volume
        configMap:
          name: lifee-config
      - name: logs-volume
        emptyDir: {}

---
# k8s/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: lifee-app-service
  namespace: lifee
spec:
  selector:
    app: lifee-app
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP

---
# k8s/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: lifee-ingress
  namespace: lifee
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  tls:
  - hosts:
    - lifee.example.com
    secretName: lifee-tls
  rules:
  - host: lifee.example.com
    http:
      paths:
      - path: /api
        pathType: Prefix
        backend:
          service:
            name: lifee-app-service
            port:
              number: 80
      - path: /
        pathType: Prefix
        backend:
          service:
            name: lifee-frontend-service
            port:
              number: 80
```

## 监控和运维

### 1. 应用监控

```yaml
# prometheus配置
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'lifee-app'
    static_configs:
      - targets: ['lifee-app:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s

  - job_name: 'postgres'
    static_configs:
      - targets: ['postgres-exporter:9187']

  - job_name: 'redis'
    static_configs:
      - targets: ['redis-exporter:9121']
```

```kotlin
// 自定义指标
@Component
class CustomMetrics {
    
    private val documentProcessingCounter = Counter.builder("document_processing_total")
        .description("Total number of processed documents")
        .tag("status", "success")
        .register(Metrics.globalRegistry)
    
    private val ragResponseTimer = Timer.builder("rag_response_duration")
        .description("RAG response generation time")
        .register(Metrics.globalRegistry)
    
    private val activeConversationsGauge = Gauge.builder("active_conversations")
        .description("Number of active conversations")
        .register(Metrics.globalRegistry) { getActiveConversationCount() }
    
    fun recordDocumentProcessed(status: String) {
        documentProcessingCounter.increment(Tags.of("status", status))
    }
    
    fun recordRAGResponse(duration: Duration) {
        ragResponseTimer.record(duration)
    }
}
```

### 2. 日志管理

```xml
<!-- logback-spring.xml -->
<configuration>
    <springProfile name="!local">
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>
    </springProfile>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/lifee.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/lifee.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

## 安全架构

### 1. 认证授权

```kotlin
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {
    
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { 
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtDecoder(jwtDecoder())
                }
            }
            .build()
    }
    
    @Bean
    fun jwtDecoder(): JwtDecoder {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build()
    }
}
```

### 2. 数据加密

```kotlin
@Component
class EncryptionService {
    
    @Value("\${app.encryption.key}")
    private lateinit var encryptionKey: String
    
    private val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    
    fun encrypt(plainText: String): String {
        val key = SecretKeySpec(encryptionKey.toByteArray(), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(plainText.toByteArray())
        
        return Base64.getEncoder().encodeToString(iv + encryptedData)
    }
    
    fun decrypt(encryptedText: String): String {
        val data = Base64.getDecoder().decode(encryptedText)
        val iv = data.sliceArray(0..11)
        val encryptedData = data.sliceArray(12 until data.size)
        
        val key = SecretKeySpec(encryptionKey.toByteArray(), "AES")
        val spec = GCMParameterSpec(128, iv)
        
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        val decryptedData = cipher.doFinal(encryptedData)
        
        return String(decryptedData)
    }
}
```

## 性能优化

### 1. 缓存策略

```kotlin
@Configuration
@EnableCaching
class CacheConfig {
    
    @Bean
    fun cacheManager(): CacheManager {
        return RedisCacheManager.builder(redisConnectionFactory())
            .cacheDefaults(
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(30))
                    .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                            .fromSerializer(StringRedisSerializer())
                    )
                    .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                            .fromSerializer(GenericJackson2JsonRedisSerializer())
                    )
            )
            .build()
    }
}

@Service
class KnowledgeBaseService {
    
    @Cacheable(value = ["knowledge-bases"], key = "#id")
    fun getKnowledgeBase(id: UUID): KnowledgeBase {
        return knowledgeBaseRepository.findById(id)
            .orElseThrow { KnowledgeBaseNotFoundException(id) }
    }
    
    @CacheEvict(value = ["knowledge-bases"], key = "#id")
    fun updateKnowledgeBase(id: UUID, request: UpdateKnowledgeBaseRequest) {
        // 更新逻辑
    }
}
```

### 2. 数据库优化

```sql
-- 索引优化
CREATE INDEX CONCURRENTLY idx_documents_kb_status_created 
ON documents(knowledge_base_id, status, created_at DESC);

-- 分区表
CREATE TABLE user_behaviors_2024 PARTITION OF user_behaviors
FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');

-- 物化视图
CREATE MATERIALIZED VIEW mv_user_activity_summary AS
SELECT 
    user_id,
    DATE(created_at) as activity_date,
    COUNT(*) as activity_count,
    COUNT(DISTINCT target_id) as unique_targets
FROM user_behaviors
GROUP BY user_id, DATE(created_at);

CREATE UNIQUE INDEX ON mv_user_activity_summary(user_id, activity_date);
```

## 架构优化影响评估

### 正面影响
1. **降低复杂度**：减少了系统组件数量和依赖关系
2. **提高可维护性**：模块化设计便于独立开发和维护
3. **优化性能**：Log4j2提供更好的日志性能
4. **降低风险**：原型验证减少开发风险
5. **渐进式演进**：为后期扩展预留空间

### 潜在风险
1. **搜索能力限制**：初期搜索功能可能不如Elasticsearch强大
2. **消息处理能力**：RabbitMQ的吞吐量限制
3. **模块间耦合**：需要仔细设计模块间接口

### 风险缓解措施
1. **搜索能力**：PostgreSQL FTS + pgvector可以满足初期需求，后期可升级
2. **消息处理**：监控消息队列性能，及时升级到Kafka
3. **模块耦合**：严格遵循DDD原则，通过事件和接口解耦

## 实施建议

### 短期（1-3个月）
1. 按新的模块结构搭建项目骨架
2. 实现PostgreSQL + pgvector的混合搜索
3. 配置Log4j2日志框架
4. 建立原型验证流程

### 中期（3-6个月）
1. 完善各功能域的核心功能
2. 监控系统性能和瓶颈
3. 评估是否需要引入Elasticsearch
4. 优化RabbitMQ配置和性能

### 长期（6个月以上）
1. 根据业务量级考虑微服务拆分
2. 评估升级到Kafka的必要性
3. 引入更多高级功能（如分布式追踪）
4. 持续优化架构和性能

## 总结

本技术架构设计文档整合了LifeE系统的完整技术实现方案和架构优化调整，体现了"简单优于复杂"的设计原则。通过渐进式的架构演进策略，在保证功能完整性的同时，降低了系统的复杂度和维护成本，既满足了当前需求，又为未来扩展预留了空间。

### 关键成功因素
1. **严格遵循DDD原则**：确保模块边界清晰
2. **持续监控和评估**：及时发现性能瓶颈
3. **保持架构灵活性**：为后期升级预留接口
4. **重视原型验证**：降低开发风险
5. **文档驱动开发**：确保架构决策的可追溯性

该架构设计涵盖了从架构设计到具体实现的各个方面，为开发团队提供了详细的技术指导和演进路径。