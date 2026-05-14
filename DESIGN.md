# Enterprise Knowledge Copilot 设计文档

## 1. 项目概述

### 1.1 项目名称

Enterprise Knowledge Copilot

### 1.2 项目定位

这是一个面向企业内部知识管理场景的 AI 知识库问答系统。系统支持上传企业文档，自动完成解析、切分、向量化和索引构建，并基于检索增强生成（RAG）为用户提供带引用来源的回答。同时提供问答日志、用户反馈和效果评测能力。

该项目适合作为简历项目，目标是体现以下能力：

- Java 后端工程能力
- AI 应用开发能力
- RAG 系统设计能力
- 全栈产品实现能力
- 工程化部署和可观测能力

### 1.3 目标用户

| 角色     | 权限                                             |
| -------- | ------------------------------------------------ |
| 普通用户 | 上传文档、基于知识库提问、查看历史会话、反馈评价 |
| 管理员   | 管理知识库、查看日志、查看评测结果、分析系统效果 |

---

## 2. 项目目标

### 2.1 业务目标

- 为企业内部文档提供统一检索和问答入口
- 降低重复查阅文档和内部问询成本
- 提高知识利用率和问答效率

### 2.2 技术目标

- 搭建完整 Java 主栈 AI 应用
- 实现文档导入、向量检索、答案生成、引用回溯闭环
- 实现基础评测与日志追踪能力
- 具备部署上线和持续扩展能力

### 2.3 阶段性性能目标

以下目标面向个人项目 MVP 阶段，以单实例部署和中小规模知识库为前提：

| 指标 | 目标值 |
| --- | --- |
| 问答响应时间 | P95 < 8s |
| 文档解析吞吐量 | 3-5 docs/min |
| 并发用户数 | 10-20 |
| 知识库容量 | 100-1000 文档 |

---

## 3. 核心功能范围

### 3.1 分阶段交付计划

一个人 + 有限时间的约束下，必须分清优先级。把功能拆成三个层级，每一层都可以独立演示：

#### V0 —— 可演示核心（第 1-3 周，必须完成）

这是面试演示的最小闭环。**其他功能都可以没做完，这部分必须完整、稳定、体验好**：

| 模块 | 功能 | 为什么必须在 V0 |
| --- | --- | --- |
| 认证 | 注册、登录、JWT 鉴权 | 没登录就没法做数据隔离，后续所有功能都依赖它 |
| 知识库 | 创建知识库、查看列表 | 文档和问答都需要挂载在知识库下 |
| 文档 | 上传 PDF、自动解析/切分/向量化、状态追踪 | RAG 的核心输入，没有文档就没东西可问 |
| 问答 | 基于知识库提问、返回带引用来源的回答 | **项目的核心价值点，面试演示的亮点** |
| 会话 | 单次会话内多轮对话、消息历史查看 | 展示多轮对话能力是 RAG 系统的基本要求 |

**V0 里程碑：** 上传一个 PDF → 提问 → 得到带引用的回答 → 可以在面试中完整演示这个流程。

#### V1 —— 完整 MVP（第 4-6 周，尽力完成）

| 模块 | 功能 |
| --- | --- |
| 知识库 | 删除知识库、知识库详情（含统计） |
| 文档 | 支持 DOCX / Markdown、文档列表筛选、文档删除、重新索引 |
| 会话 | 会话列表管理、切换会话、会话标题自动生成 |
| 反馈 | 点赞/点踩、反馈备注 |
| 日志 | 问答日志查询（管理员）、检索日志查询、Token 用量统计 |
| 评测 | 基础评测任务管理、检索命中率/耗时/得分统计 |
| 安全 | 文件类型 Magic Number 校验、上传频率限制、数据隔离校验 |

**V1 里程碑：** 功能完整、前后端稳定联调、写入简历。

#### V2 —— 加分项（时间允许再做，不影响投简历）

| 功能 | 优先级 | 说明 |
| --- | --- | --- |
| 流式输出（SSE） | P0 | 体验提升巨大，面试演示效果好 |
| 混合检索（BM25 + 向量） | P1 | 面试时可以说"已规划并实现了混合检索" |
| Rerank 重排 | P1 | 同上，体现对检索质量的深度思考 |
| Docker Compose 一键部署 | P1 | 方便面试官 clone 后快速启动 |
| 多知识库联合检索 | P2 | 进阶功能 |
| OCR 扫描件支持 | P2 | 需要额外依赖 |
| 管理员仪表盘 | P2 | 锦上添花 |
| Redis 缓存热点问题 | P2 | 生产环境才需要 |

### 3.2 不做（避免范围蔓延）

以下功能明确不做，不要花时间：

- 社交功能（评论、分享、协作编辑）
- 复杂的权限模型（RBAC、组织架构）
- 实时通知（WebSocket 推送）
- 移动端适配
- 多语言国际化
- 文档协同编辑

### 3.3 不做（避免范围蔓延）

以下功能明确不做，避免分散精力。面试时被问到可以说明"这是 V0 阶段的有意取舍"：

- 社交功能（评论、分享、协作编辑）
- RBAC 权限模型（角色过多设计过度）
- 实时通知（WebSocket 推送）
- 移动端适配
- 多语言国际化
- 协同编辑

---

## 4. 技术栈

### 4.1 后端技术栈

| 类别 | 技术选型 | 版本 | 为什么这么选（面试必问） |
| --- | --- | --- | --- |
| 语言 | Java | 21 | LTS 版本；虚拟线程对 LLM 调用这种 I/O 密集型场景有性能红利；国内后端岗位最多 |
| 框架 | Spring Boot | 3.2+ | Java 生态事实标准；Spring AI Alibaba 一站式接入国内模型 |
| ORM | MyBatis-Plus 3.5+ | **国内 Java 岗实际标准**，见下方详细说明 |
| 数据库迁移 | Flyway | - | 比 Liquibase 轻量；SQL 脚本直接可读可调试 |
| 构建工具 | Maven | 3.9+ | 国内主流构建工具 |
| API 文档 | Knife4j | 4.x | Springdoc 的国产增强版；国内公司标配，UI 比 Swagger 原生好用 |

**为什么用 MyBatis-Plus 而不是 JPA？** —— 面试时主动解释：

1. **国内事实标准**：国内公司 90% 用 MyBatis/MyBatis-Plus，面试官更熟悉，你学到的技能更可迁移
2. **复杂查询友好**：LambdaQueryWrapper 写动态条件查询比 JPA Specification 直观太多；复杂聚合（日志统计、评测指标汇总）直接写 SQL，清晰可控
3. **与 pgvector 不冲突**：RAG 的核心向量检索 SQL（`<=>` 操作符）本身是手写原生 SQL，无论 MP 还是 JPA 都是直接执行，ORM 框架的选择不影响核心检索
4. **分页插件好用**：MP 的 PaginationInterceptor 一行配置搞定，JPA 的 Pageable 排序复杂时很麻烦

**为什么用 Knife4j 而不是 Springdoc？**
- Knife4j 是基于 Swagger/OpenAPI 的国产增强版，国内公司几乎都用它
- UI 界面更好看，支持离线文档导出，参数分组和排序比 Springdoc 原生好
- 面试官看到 Knife4j 就知道你了解国内技术栈

**为什么 Java 而不是 Python？** —— 面试高频问题：
- Java 后端岗位更多，Spring Boot 的依赖注入、事务管理、安全框架比 FastAPI 更成熟，能体现更强的后端工程能力
- Python 做 AI 很自然，但做 Web API 的数据校验、事务、安全需要额外补很多库，而 Spring Boot 全家桶开箱即用
- 反过来，这个项目也证明了"Java 开发者能做 AI 应用"，打破"AI 只能 Python"的刻板印象

**为什么不用 LangChain / 为什么用 Spring AI Alibaba？**
- LangChain 抽象层次多，隐藏了 RAG 核心细节。用 Spring AI Alibaba 自己编排流程，每一步都可控可调试
- LangChain4j（Java 版）还不够成熟，文档和社区远不如 Python 版
- Spring AI Alibaba 对比原版 Spring AI：原生对接阿里云百炼（DashScope），通义千问的 Chat + Embedding 一个平台搞定；中文文档和钉钉群，遇到问题更容易解决；对学生有免费额度
- 面试时你能清楚地解释"我的 RAG 链路每一步在做什么"，而 LangChain 用户的回答往往是"调了个 chain"

### 4.2 AI 与检索技术栈

| 类别 | 技术选型 | 为什么这么选 |
| --- | --- | --- |
| LLM 接入 | Spring AI Alibaba | 在 Spring Boot 体系内统一接入通义千问/DeepSeek 等国内模型 |
| 模型服务 | 阿里云百炼 DashScope | 通义千问 Chat + Embedding 一个平台；学生免费额度 |
| Embedding | text-embedding-v2（通义千问） | 1536 维，专门针对中文优化，比 OpenAI Embedding 中文效果更好 |
| 向量数据库 | PostgreSQL + pgvector | **pgvector 只能跑在 PG 上**，见下方 PG vs MySQL 说明 |
| PDF 解析 | Apache PDFBox | 纯 Java 解析，不依赖 Python 服务；免费无限制 |

**pgvector vs Milvus/Qdrant/Weaviate —— 面试高频问题：**

选择 pgvector 的核心原因：
1. **零额外运维**：不需要单独部署和维护一个向量数据库服务。对于个人项目和在实习面试中展示，简单就是优势
2. **学习成本低**：SQL 就能做向量检索，不用学新的查询语言
3. **MVP 规模完全够用**：IVFFlat 索引在 10 万 chunk 级别内，召回延迟 < 50ms

pgvector 的劣势（面试时要主动提，展示技术判断力）：
1. 百万级以上向量时，IVFFlat 的召回速度会明显下降，不如 HNSW 索引（pgvector 也支持 HNSW，但内存占用更高）
2. 没有内置的混合检索和 Rerank，需要自己实现
3. 缺少专业向量数据库的监控、调优工具

**如果面试官问"什么时候会换 Milvus"：**
- 知识库规模超过 100 万 chunk 时
- 需要分布式部署和高可用时
- 需要专业的性能调优和监控时
- 迁移成本其实很低——Embedding 可以重新生成或从 pgvector 导出，只需要改 `RetrievalService` 的实现

**为什么用 PostgreSQL 而不是 MySQL？** —— 国内面试常见疑问：
- 国内 MySQL 确实是主流，但 pgvector 是唯一一个能"不额外部署服务"的向量数据库方案。如果选 MySQL，就必须额外搭一个 Milvus 或 Qdrant，个人项目运维负担太重
- PostgreSQL 在国内的使用率在快速增长（尤其是互联网大厂），不是冷门技术
- 面试时可以说："关键决策因素是有 pgvector 这个向量检索需求，MySQL 无法满足。如果用 MySQL，需要额外维护一个向量数据库服务"

### 4.3 前端技术栈

| 类别 | 技术选型 | 为什么这么选 |
| --- | --- | --- |
| 框架 | Next.js 14+ | App Router 是 React 官方推荐；国内 React 岗需求大 |
| 语言 | TypeScript | 类型安全；减少前后端接口联调错误 |
| UI | Tailwind CSS + shadcn/ui | shadcn/ui 代码直接在你项目里，不像 npm 包那样不好修改；视觉上比 Ant Design 现代，适合 AI 产品 |
| 状态管理 | React Query + Zustand | TanStack Query 管理服务端状态，Zustand 管客户端状态 |

**shadcn/ui vs Ant Design？** —— 面试时可能被问：
- Ant Design 是国内企业级 React 项目的标配（阿里出品），中后台管理页面用它确实更快
- 这个项目选 shadcn/ui 的原因：1）AI 聊天产品不是传统的表单表格中后台，视觉上需要更现代；2）shadcn/ui 的代码直接存在于你自己的项目里，面试官能看到你的组件封装能力；3）Tailwind 的原子化 CSS 写起来比 Ant Design 的 CSS-in-JS 更快
- 如果面试官是 Ant Design 重度用户，就说"管理后台页面（日志查询、评测管理）后续可以考虑接 Ant Design，聊天页面保持 shadcn/ui"

### 4.4 基础设施

| 类别 | 技术选型 |
| --- | --- |
| 数据库 | PostgreSQL 15+ |
| 缓存 | Redis 7+（可选） |
| 对象存储 | 本地存储 / MinIO |
| 容器化 | Docker |
| 反向代理 | Nginx（可选） |
| 监控 | 应用日志 + 基础指标 |
| CI/CD | GitHub Actions（可选） |

---

## 5. 架构设计

### 5.1 总体架构

```
┌─────────────────────────────────────────────────────────────────────────┐
│                            前端展示层 (Next.js)                           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │  登录页   │ │知识库管理 │ │ 文档管理  │ │ 聊天界面  │ │ 评测页面  │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         Java 业务服务层 (Spring Boot)                     │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ 认证模块  │ │知识库模块 │ │ 文档模块  │ │ 问答模块  │ │ 评测模块  │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                      RAG 编排层 (RagService)                       │   │
│  └──────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                            AI 检索层                                     │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ 文档解析  │ │ 文本切分  │ │ Embedding │ │ 向量检索  │ │ 答案生成  │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                            数据存储层                                    │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐                    │
│  │PostgreSQL│ │ pgvector │ │  Redis   │ │  MinIO   │                    │
│  │(关系数据) │ │ (向量数据)│ │  (缓存)  │ │ (文件存储)│                    │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘                    │
└─────────────────────────────────────────────────────────────────────────┘
```

### 5.2 核心调用链路

#### 5.2.1 文档导入链路

```
用户上传文件
    │
    ▼
┌─────────────────┐
│ 保存文件到 MinIO │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ 创建文档记录     │ ──► 状态: UPLOADED
└────────┬────────┘
         │
         ▼ (异步)
┌─────────────────┐
│ 解析文档正文     │ ──► 状态: PARSING
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ 文本清洗预处理   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Chunk 切分      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ 批量 Embedding  │ ──► 状态: INDEXING
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ 向量写入 pgvector│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ 更新文档状态     │ ──► 状态: READY
└─────────────────┘
```

#### 5.2.2 问答链路（时序图）

```
┌──────┐          ┌──────────┐        ┌───────────┐        ┌──────────┐        ┌──────────┐
│ User │          │ Frontend │        │ Backend   │        │ Embedding│        │ LLM API  │
└──┬───┘          └────┬─────┘        └─────┬─────┘        └────┬─────┘        └────┬─────┘
   │                   │                    │                   │                   │
   │  发起提问          │                    │                   │                   │
   │──────────────────>│                    │                   │                   │
   │                   │                    │                   │                   │
   │                   │ POST /messages     │                   │                   │
   │                   │───────────────────>│                   │                   │
   │                   │                    │                   │                   │
   │                   │                    │ 1. 权限校验        │                   │
   │                   │                    │─────────          │                   │
   │                   │                    │                   │                   │
   │                   │                    │ 2. Query Embedding │                   │
   │                   │                    │──────────────────>│                   │
   │                   │                    │                   │                   │
   │                   │                    │<──────────────────│                   │
   │                   │                    │   返回向量         │                   │
   │                   │                    │                   │                   │
   │                   │                    │ 3. 向量检索 Top-K  │                   │
   │                   │                    │─────────          │                   │
   │                   │                    │                   │                   │
   │                   │                    │ 4. 拼装 Prompt     │                   │
   │                   │                    │─────────          │                   │
   │                   │                    │                   │                   │
   │                   │                    │ 5. 调用 LLM        │                   │
   │                   │                    │───────────────────────────────────>│
   │                   │                    │                   │                   │
   │                   │                    │<───────────────────────────────────│
   │                   │                    │   返回答案         │                   │
   │                   │                    │                   │                   │
   │                   │                    │ 6. 保存日志        │                   │
   │                   │                    │─────────          │                   │
   │                   │                    │                   │                   │
   │                   │  返回答案 + 引用    │                   │                   │
   │                   │<───────────────────│                   │                   │
   │                   │                    │                   │                   │
   │  显示答案          │                    │                   │                   │
   │<──────────────────│                    │                   │                   │
   │                   │                    │                   │                   │
```

#### 5.2.3 评测链路

```
加载评测集 (JSON/CSV)
    │
    ▼
┌─────────────────────┐
│ 遍历评测用例         │
└──────────┬──────────┘
           │
           ▼ (每个用例)
    ┌──────────────┐
    │ 执行问答流程  │
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │ 对比预期结果  │
    │ - 来源命中    │
    │ - 答案相似度  │
    └──────┬───────┘
           │
           ▼
┌─────────────────────┐
│ 计算聚合指标         │
│ - Hit Rate          │
│ - MRR               │
│ - Avg Latency       │
│ - Answer Score      │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ 保存评测报告         │
└─────────────────────┘
```

---

## 6. 核心模块设计

### 6.1 认证模块

**职责：**

- 用户注册与登录
- JWT 签发与校验
- Token 刷新机制
- 接口权限控制

**关键组件：**

| 组件                      | 职责           |
| ------------------------- | -------------- |
| `AuthController`          | 认证接口入口   |
| `AuthService`             | 认证业务逻辑   |
| `JwtTokenProvider`        | JWT 生成与验证 |
| `JwtAuthenticationFilter` | JWT 认证过滤器 |
| `SecurityConfig`          | 安全配置       |

**Token 设计：**

| Token 类型    | 有效期  | 存储位置            |
| ------------- | ------- | ------------------- |
| Access Token  | 15 分钟 | 内存 / localStorage |
| Refresh Token | 7 天    | HttpOnly Cookie     |

### 6.2 知识库模块

**职责：**

- 创建、查询、删除知识库
- 维护知识库与文档的关联
- 知识库统计信息

**关键组件：**

| 组件                      | 职责           |
| ------------------------- | -------------- |
| `KnowledgeBaseController` | 知识库接口入口 |
| `KnowledgeBaseService`    | 知识库业务逻辑 |
| `KnowledgeBaseMapper` | 数据访问层（MyBatis-Plus BaseMapper）     |

### 6.3 文档导入模块

**职责：**

- 接收上传文档
- 执行文档解析
- 执行文本切分与索引构建
- 处理解析失败重试

**关键组件：**

| 组件                     | 职责              |
| ------------------------ | ----------------- |
| `DocumentController`     | 文档接口入口      |
| `DocumentService`        | 文档业务逻辑      |
| `DocumentParserFactory`  | 解析器工厂        |
| `PdfDocumentParser`      | PDF 解析实现      |
| `DocxDocumentParser`     | DOCX 解析实现     |
| `MarkdownDocumentParser` | Markdown 解析实现 |
| `ChunkingService`        | 文本切分服务      |
| `EmbeddingService`       | 向量化服务        |
| `IndexingService`        | 索引构建服务      |

**异步处理设计：**

```java
@Async("documentProcessingExecutor")
public void processDocument(Long documentId) {
    try {
        // 1. 解析文档
        String content = documentParser.parse(document);
        
        // 2. 切分
        List<Chunk> chunks = chunkingService.chunk(content);
        
        // 3. 向量化
        List<float[]> embeddings = embeddingService.embed(chunks);
        
        // 4. 入库
        indexingService.index(documentId, chunks, embeddings);
        
        // 5. 更新状态
        documentService.updateStatus(documentId, DocumentStatus.READY);
    } catch (Exception e) {
        documentService.updateStatus(documentId, DocumentStatus.FAILED, e.getMessage());
        throw e;
    }
}
```

### 6.4 问答模块

**职责：**

- 接收用户问题
- 组织 RAG 检索与生成流程
- 生成可回溯引用答案
- 支持流式输出

**关键组件：**

| 组件                      | 职责         |
| ------------------------- | ------------ |
| `ChatController`          | 聊天接口入口 |
| `ChatService`             | 会话管理     |
| `RagService`              | RAG 流程编排 |
| `RetrievalService`        | 向量检索     |
| `PromptBuilder`           | Prompt 构建  |
| `AnswerGenerationService` | 答案生成     |

### 6.5 日志与反馈模块

**职责：**

- 保存问答记录和检索记录
- 保存用户反馈
- 提供后台查询能力

**关键组件：**

| 组件              | 职责         |
| ----------------- | ------------ |
| `LogController`   | 日志接口入口 |
| `LogService`      | 日志查询逻辑 |
| `FeedbackService` | 反馈处理逻辑 |

### 6.6 评测模块

**职责：**

- 管理评测集
- 触发批量评测
- 汇总评测指标

**关键组件：**

| 组件                    | 职责         |
| ----------------------- | ------------ |
| `EvalController`        | 评测接口入口 |
| `EvalService`           | 评测业务逻辑 |
| `EvalRunner`            | 评测执行器   |
| `EvalMetricsCalculator` | 指标计算器   |

**评测指标：**

| 指标         | 计算方式          | 说明                 |
| ------------ | ----------------- | -------------------- |
| Hit Rate     | 命中数 / 总数     | 检索是否找到正确来源 |
| MRR          | Σ(1/rank_i) / N   | 检索排序质量         |
| Answer Score | LLM 评分 / 相似度 | 回答质量             |
| Avg Latency  | 总耗时 / 数量     | 平均响应时间         |

---

## 7. 数据库设计

### 7.1 ER 图

```
┌─────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   users     │       │ knowledge_bases │       │   documents     │
├─────────────┤       ├─────────────────┤       ├─────────────────┤
│ id (PK)     │◄──┐   │ id (PK)         │◄──┐   │ id (PK)         │
│ email       │   │   │ owner_id (FK)   │───┘   │ kb_id (FK)      │───┐
│ password    │   │   │ name            │       │ file_name       │   │
│ name        │   │   │ description     │       │ file_type       │   │
│ role        │   │   │ created_at      │       │ status          │   │
│ created_at  │   │   │ updated_at      │       │ storage_path    │   │
│ updated_at  │   │   │ deleted_at      │       │ created_at      │   │
│ deleted_at  │   │   └─────────────────┘       │ updated_at      │   │
└─────────────┘   │                             │ deleted_at      │   │
                  │                             └─────────────────┘   │
                  │                                     │             │
                  │                                     ▼             │
                  │                             ┌─────────────────┐   │
                  │                             │ document_chunks │   │
                  │                             ├─────────────────┤   │
                  │                             │ id (PK)         │   │
                  │                             │ doc_id (FK)     │───┘
                  │                             │ kb_id (FK)      │───┐
                  │                             │ chunk_index     │   │
                  │                             │ content         │   │
                  │                             │ token_count     │   │
                  │                             │ embedding       │   │
                  │                             │ metadata        │   │
                  │                             │ created_at      │   │
                  │                             └─────────────────┘   │
                  │                                                   │
                  │   ┌─────────────────┐                             │
                  │   │  chat_sessions  │                             │
                  │   ├─────────────────┤                             │
                  │   │ id (PK)         │                             │
                  ├───│ user_id (FK)    │                             │
                  │   │ kb_id (FK)      │─────────────────────────────┘
                  │   │ title           │
                  │   │ created_at      │
                  │   │ updated_at      │
                  │   └────────┬────────┘
                  │            │
                  │            ▼
                  │   ┌─────────────────┐       ┌─────────────────┐
                  │   │    messages     │       │ answer_feedback │
                  │   ├─────────────────┤       ├─────────────────┤
                  │   │ id (PK)         │       │ id (PK)         │
                  │   │ session_id (FK) │       │ message_id (FK) │
                  │   │ role            │       │ user_id (FK)    │───┐
                  │   │ content         │       │ rating          │   │
                  │   │ citations       │       │ comment         │   │
                  │   │ latency_ms      │       │ created_at      │   │
                  │   │ prompt_tokens   │       └─────────────────┘   │
                  │   │ completion_tok  │                             │
                  │   │ created_at      │                             │
                  │   └────────┬────────┘                             │
                  │            │                                      │
                  │            ▼                                      │
                  │   ┌─────────────────┐                             │
                  │   │ retrieval_logs  │                             │
                  │   ├─────────────────┤                             │
                  │   │ id (PK)         │                             │
                  │   │ message_id (FK) │                             │
                  │   │ query_text      │                             │
                  │   │ retrieved_chunks│                             │
                  │   │ top_k           │                             │
                  │   │ latency_ms      │                             │
                  │   │ created_at      │                             │
                  │   └─────────────────┘                             │
                  │                                                   │
                  │   ┌─────────────────┐       ┌─────────────────┐   │
                  │   │   eval_runs     │       │   eval_cases    │   │
                  │   ├─────────────────┤       ├─────────────────┤   │
                  │   │ id (PK)         │◄──────│ eval_run_id(FK) │   │
                  │   │ kb_id (FK)      │───────│ question        │   │
                  │   │ name            │       │ expected_answer │   │
                  │   │ status          │       │ expected_sources│   │
                  │   │ hit_rate        │       │ actual_answer   │   │
                  │   │ avg_latency     │       │ actual_sources  │   │
                  │   │ avg_score       │       │ retrieval_hit   │   │
                  │   │ created_at      │       │ score           │   │
                  │   └─────────────────┘       │ latency_ms      │   │
                  │                             └─────────────────┘   │
                  │                                                   │
                  └───────────────────────────────────────────────────┘
```

### 7.2 表结构详细设计

#### 7.2.1 users

| 字段          | 类型         | 约束                     | 说明              |
| ------------- | ------------ | ------------------------ | ----------------- |
| id            | BIGSERIAL    | PK                       | 主键              |
| email         | VARCHAR(128) | UNIQUE, NOT NULL         | 用户邮箱          |
| password_hash | VARCHAR(255) | NOT NULL                 | 密码哈希 (BCrypt) |
| name          | VARCHAR(64)  | NOT NULL                 | 用户名            |
| role          | VARCHAR(32)  | NOT NULL, DEFAULT 'user' | 角色: user, admin |
| created_at    | TIMESTAMP    | NOT NULL, DEFAULT NOW()  | 创建时间          |
| updated_at    | TIMESTAMP    | NOT NULL, DEFAULT NOW()  | 更新时间          |
| deleted_at    | TIMESTAMP    | NULL                     | 软删除时间        |

**索引：**

- `idx_users_email` ON (email)
- `idx_users_deleted_at` ON (deleted_at) WHERE deleted_at IS NULL

#### 7.2.2 knowledge_bases

| 字段           | 类型         | 约束                    | 说明             |
| -------------- | ------------ | ----------------------- | ---------------- |
| id             | BIGSERIAL    | PK                      | 主键             |
| owner_id       | BIGINT       | FK → users.id, NOT NULL | 所属用户         |
| name           | VARCHAR(128) | NOT NULL                | 知识库名称       |
| description    | TEXT         | NULL                    | 描述             |
| document_count | INT          | DEFAULT 0               | 文档数量（冗余） |
| chunk_count    | INT          | DEFAULT 0               | 分片数量（冗余） |
| created_at     | TIMESTAMP    | NOT NULL, DEFAULT NOW() | 创建时间         |
| updated_at     | TIMESTAMP    | NOT NULL, DEFAULT NOW() | 更新时间         |
| deleted_at     | TIMESTAMP    | NULL                    | 软删除时间       |

**索引：**

- `idx_kb_owner_id` ON (owner_id)
- `idx_kb_deleted_at` ON (deleted_at) WHERE deleted_at IS NULL

#### 7.2.3 documents

| 字段              | 类型         | 约束                              | 说明                    |
| ----------------- | ------------ | --------------------------------- | ----------------------- |
| id                | BIGSERIAL    | PK                                | 主键                    |
| knowledge_base_id | BIGINT       | FK → knowledge_bases.id, NOT NULL | 所属知识库              |
| file_name         | VARCHAR(255) | NOT NULL                          | 原始文件名              |
| file_type         | VARCHAR(32)  | NOT NULL                          | 文件类型: PDF, DOCX, MD |
| file_size         | BIGINT       | NOT NULL                          | 文件大小 (bytes)        |
| storage_path      | VARCHAR(512) | NOT NULL                          | 存储路径                |
| status            | VARCHAR(32)  | NOT NULL, DEFAULT 'UPLOADED'      | 文档状态                |
| error_message     | TEXT         | NULL                              | 错误信息                |
| chunk_count       | INT          | DEFAULT 0                         | 分片数量                |
| created_at        | TIMESTAMP    | NOT NULL, DEFAULT NOW()           | 创建时间                |
| updated_at        | TIMESTAMP    | NOT NULL, DEFAULT NOW()           | 更新时间                |
| deleted_at        | TIMESTAMP    | NULL                              | 软删除时间              |

**状态枚举：**

- `UPLOADED`: 已上传
- `PARSING`: 解析中
- `INDEXING`: 索引中
- `READY`: 就绪
- `FAILED`: 失败

**索引：**

- `idx_doc_kb_id` ON (knowledge_base_id)
- `idx_doc_status` ON (status)
- `idx_doc_deleted_at` ON (deleted_at) WHERE deleted_at IS NULL

#### 7.2.4 document_chunks

| 字段              | 类型         | 约束                              | 说明                                 |
| ----------------- | ------------ | --------------------------------- | ------------------------------------ |
| id                | BIGSERIAL    | PK                                | 主键                                 |
| document_id       | BIGINT       | FK → documents.id, NOT NULL       | 所属文档                             |
| knowledge_base_id | BIGINT       | FK → knowledge_bases.id, NOT NULL | 所属知识库                           |
| chunk_index       | INT          | NOT NULL                          | 分片序号                             |
| content           | TEXT         | NOT NULL                          | 分片文本                             |
| token_count       | INT          | NOT NULL                          | Token 数                             |
| embedding         | vector(1536) | NULL                              | 向量（通义千问 text-embedding-v2） |
| metadata          | JSONB        | DEFAULT '{}'                      | 页码、标题等元数据                   |
| created_at        | TIMESTAMP    | NOT NULL, DEFAULT NOW()           | 创建时间                             |

**索引：**

- `idx_chunk_doc_id` ON (document_id)
- `idx_chunk_kb_id` ON (knowledge_base_id)
- `idx_chunk_embedding` ON (embedding) USING ivfflat (vector_cosine_ops) WITH (lists = 100)

#### 7.2.5 chat_sessions

| 字段              | 类型         | 约束                              | 说明             |
| ----------------- | ------------ | --------------------------------- | ---------------- |
| id                | BIGSERIAL    | PK                                | 主键             |
| user_id           | BIGINT       | FK → users.id, NOT NULL           | 用户 ID          |
| knowledge_base_id | BIGINT       | FK → knowledge_bases.id, NOT NULL | 知识库 ID        |
| title             | VARCHAR(255) | NULL                              | 会话标题         |
| message_count     | INT          | DEFAULT 0                         | 消息数量（冗余） |
| created_at        | TIMESTAMP    | NOT NULL, DEFAULT NOW()           | 创建时间         |
| updated_at        | TIMESTAMP    | NOT NULL, DEFAULT NOW()           | 更新时间         |

**索引：**

- `idx_session_user_id` ON (user_id)
- `idx_session_kb_id` ON (knowledge_base_id)

#### 7.2.6 messages

| 字段              | 类型        | 约束                            | 说明             |
| ----------------- | ----------- | ------------------------------- | ---------------- |
| id                | BIGSERIAL   | PK                              | 主键             |
| session_id        | BIGINT      | FK → chat_sessions.id, NOT NULL | 会话 ID          |
| role              | VARCHAR(16) | NOT NULL                        | user / assistant |
| content           | TEXT        | NOT NULL                        | 消息内容         |
| citations         | JSONB       | NULL                            | 引用信息         |
| latency_ms        | INT         | NULL                            | 响应耗时         |
| prompt_tokens     | INT         | NULL                            | 输入 Token       |
| completion_tokens | INT         | NULL                            | 输出 Token       |
| model             | VARCHAR(64) | NULL                            | 使用的模型       |
| created_at        | TIMESTAMP   | NOT NULL, DEFAULT NOW()         | 创建时间         |

**索引：**

- `idx_msg_session_id` ON (session_id)
- `idx_msg_created_at` ON (created_at)

#### 7.2.7 retrieval_logs

| 字段             | 类型         | 约束                       | 说明         |
| ---------------- | ------------ | -------------------------- | ------------ |
| id               | BIGSERIAL    | PK                         | 主键         |
| message_id       | BIGINT       | FK → messages.id, NOT NULL | 对应回答消息 |
| query_text       | TEXT         | NOT NULL                   | 检索问题     |
| query_embedding  | vector(1536) | NULL                       | 查询向量     |
| retrieved_chunks | JSONB        | NOT NULL                   | 检索结果明细 |
| top_k            | INT          | NOT NULL                   | 检索数量     |
| latency_ms       | INT          | NOT NULL                   | 检索耗时     |
| created_at       | TIMESTAMP    | NOT NULL, DEFAULT NOW()    | 创建时间     |

**索引：**

- `idx_retrieval_msg_id` ON (message_id)
- `idx_retrieval_created_at` ON (created_at)

#### 7.2.8 answer_feedback

| 字段       | 类型        | 约束                       | 说明        |
| ---------- | ----------- | -------------------------- | ----------- |
| id         | BIGSERIAL   | PK                         | 主键        |
| message_id | BIGINT      | FK → messages.id, NOT NULL | 回答消息 ID |
| user_id    | BIGINT      | FK → users.id, NOT NULL    | 用户 ID     |
| rating     | VARCHAR(16) | NOT NULL                   | up / down   |
| comment    | TEXT        | NULL                       | 反馈备注    |
| created_at | TIMESTAMP   | NOT NULL, DEFAULT NOW()    | 创建时间    |

**索引：**

- `idx_feedback_msg_id` ON (message_id)
- `idx_feedback_user_id` ON (user_id)
- UNIQUE (message_id, user_id)

#### 7.2.9 eval_runs

| 字段              | 类型         | 约束                              | 说明      |
| ----------------- | ------------ | --------------------------------- | --------- |
| id                | BIGSERIAL    | PK                                | 主键      |
| knowledge_base_id | BIGINT       | FK → knowledge_bases.id, NOT NULL | 知识库 ID |
| name              | VARCHAR(128) | NOT NULL                          | 评测名称  |
| status            | VARCHAR(32)  | NOT NULL, DEFAULT 'PENDING'       | 评测状态  |
| total_cases       | INT          | DEFAULT 0                         | 总用例数  |
| hit_count         | INT          | DEFAULT 0                         | 命中数    |
| hit_rate          | DECIMAL(5,4) | NULL                              | 命中率    |
| mrr               | DECIMAL(5,4) | NULL                              | MRR 分数  |
| avg_latency_ms    | INT          | NULL                              | 平均耗时  |
| avg_score         | DECIMAL(5,4) | NULL                              | 平均得分  |
| started_at        | TIMESTAMP    | NULL                              | 开始时间  |
| finished_at       | TIMESTAMP    | NULL                              | 完成时间  |
| created_at        | TIMESTAMP    | NOT NULL, DEFAULT NOW()           | 创建时间  |

**状态枚举：**

- `PENDING`: 待执行
- `RUNNING`: 执行中
- `COMPLETED`: 已完成
- `FAILED`: 失败

#### 7.2.10 eval_cases

| 字段             | 类型         | 约束                        | 说明         |
| ---------------- | ------------ | --------------------------- | ------------ |
| id               | BIGSERIAL    | PK                          | 主键         |
| eval_run_id      | BIGINT       | FK → eval_runs.id, NOT NULL | 所属评测任务 |
| question         | TEXT         | NOT NULL                    | 评测问题     |
| expected_answer  | TEXT         | NULL                        | 预期答案     |
| expected_sources | JSONB        | NULL                        | 预期来源     |
| actual_answer    | TEXT         | NULL                        | 实际回答     |
| actual_sources   | JSONB        | NULL                        | 实际来源     |
| retrieval_hit    | BOOLEAN      | NULL                        | 是否命中来源 |
| retrieval_rank   | INT          | NULL                        | 检索排名     |
| score            | DECIMAL(5,4) | NULL                        | 回答得分     |
| latency_ms       | INT          | NULL                        | 耗时         |
| error_message    | TEXT         | NULL                        | 错误信息     |

---

## 8. API 设计

### 8.1 通用规范

#### 8.1.1 响应格式

**成功响应：**

```json
{
  "code": 0,
  "message": "success",
  "data": { ... }
}
```

**分页响应：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "items": [...],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

**错误响应：**

```json
{
  "code": 40001,
  "message": "参数校验失败",
  "data": {
    "errors": [
      { "field": "email", "message": "邮箱格式不正确" }
    ]
  }
}
```

#### 8.1.2 错误码规范

| 错误码范围 | 类型       | 示例                                 |
| ---------- | ---------- | ------------------------------------ |
| 0          | 成功       | 0                                    |
| 400xx      | 参数错误   | 40001 参数校验失败                   |
| 401xx      | 认证错误   | 40101 未登录, 40102 Token 过期       |
| 403xx      | 权限错误   | 40301 无权限访问                     |
| 404xx      | 资源不存在 | 40401 知识库不存在                   |
| 409xx      | 业务冲突   | 40901 知识库名称已存在               |
| 500xx      | 服务器错误 | 50001 内部错误, 50002 LLM 服务不可用 |

### 8.2 认证接口

#### POST /api/auth/register

**请求体：**

```json
{
  "email": "user@example.com",
  "password": "Password123!",
  "name": "张三"
}
```

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "张三",
    "role": "user",
    "createdAt": "2025-05-10T10:00:00Z"
  }
}
```

**错误码：**

| 状态码 | 错误码 | 说明         |
| ------ | ------ | ------------ |
| 400    | 40001  | 参数校验失败 |
| 409    | 40901  | 邮箱已注册   |

#### POST /api/auth/login

**请求体：**

```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 900,
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "张三",
      "role": "user"
    }
  }
}
```

**错误码：**

| 状态码 | 错误码 | 说明           |
| ------ | ------ | -------------- |
| 400    | 40001  | 参数校验失败   |
| 401    | 40101  | 邮箱或密码错误 |

#### POST /api/auth/refresh

**请求体：**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 900
  }
}
```

#### GET /api/auth/me

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "张三",
    "role": "user",
    "createdAt": "2025-05-10T10:00:00Z"
  }
}
```

### 8.3 知识库接口

#### GET /api/knowledge-bases

**查询参数：**

| 参数     | 类型   | 必填 | 说明                        |
| -------- | ------ | ---- | --------------------------- |
| page     | int    | 否   | 页码，默认 1                |
| pageSize | int    | 否   | 每页数量，默认 20，最大 100 |
| keyword  | string | 否   | 搜索关键词                  |

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "name": "产品文档库",
        "description": "产品相关文档",
        "documentCount": 15,
        "chunkCount": 1250,
        "createdAt": "2025-05-10T10:00:00Z"
      }
    ],
    "total": 5,
    "page": 1,
    "pageSize": 20
  }
}
```

#### POST /api/knowledge-bases

**请求体：**

```json
{
  "name": "产品文档库",
  "description": "产品相关文档"
}
```

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "name": "产品文档库",
    "description": "产品相关文档",
    "documentCount": 0,
    "chunkCount": 0,
    "createdAt": "2025-05-10T10:00:00Z"
  }
}
```

#### GET /api/knowledge-bases/{id}

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "name": "产品文档库",
    "description": "产品相关文档",
    "documentCount": 15,
    "chunkCount": 1250,
    "documents": [
      {
        "id": 1,
        "fileName": "产品手册.pdf",
        "fileType": "PDF",
        "fileSize": 2048576,
        "status": "READY",
        "chunkCount": 150,
        "createdAt": "2025-05-10T10:00:00Z"
      }
    ],
    "createdAt": "2025-05-10T10:00:00Z"
  }
}
```

#### DELETE /api/knowledge-bases/{id}

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

### 8.4 文档接口

#### POST /api/knowledge-bases/{id}/documents

**请求：** `multipart/form-data`

| 字段 | 类型 | 必填 | 说明                   |
| ---- | ---- | ---- | ---------------------- |
| file | file | 是   | 文档文件 (PDF/DOCX/MD) |

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "fileName": "产品手册.pdf",
    "fileType": "PDF",
    "fileSize": 2048576,
    "status": "UPLOADED",
    "createdAt": "2025-05-10T10:00:00Z"
  }
}
```

**错误码：**

| 状态码 | 错误码 | 说明                    |
| ------ | ------ | ----------------------- |
| 400    | 40001  | 文件格式不支持          |
| 400    | 40002  | 文件大小超过限制 (50MB) |
| 404    | 40401  | 知识库不存在            |

#### GET /api/knowledge-bases/{id}/documents

**查询参数：**

| 参数     | 类型   | 必填 | 说明              |
| -------- | ------ | ---- | ----------------- |
| page     | int    | 否   | 页码，默认 1      |
| pageSize | int    | 否   | 每页数量，默认 20 |
| status   | string | 否   | 状态筛选          |
| keyword  | string | 否   | 文件名搜索        |

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "fileName": "产品手册.pdf",
        "fileType": "PDF",
        "fileSize": 2048576,
        "status": "READY",
        "chunkCount": 150,
        "createdAt": "2025-05-10T10:00:00Z"
      }
    ],
    "total": 15,
    "page": 1,
    "pageSize": 20
  }
}
```

#### DELETE /api/documents/{id}

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

#### POST /api/documents/{id}/reindex

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "status": "PARSING"
  }
}
```

### 8.5 聊天接口

#### POST /api/chat/sessions

**请求体：**

```json
{
  "knowledgeBaseId": 1,
  "title": "产品相关问题"
}
```

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "knowledgeBaseId": 1,
    "title": "产品相关问题",
    "createdAt": "2025-05-10T10:00:00Z"
  }
}
```

#### GET /api/chat/sessions

**查询参数：**

| 参数            | 类型 | 必填 | 说明       |
| --------------- | ---- | ---- | ---------- |
| page            | int  | 否   | 页码       |
| pageSize        | int  | 否   | 每页数量   |
| knowledgeBaseId | long | 否   | 知识库筛选 |

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "knowledgeBaseId": 1,
        "knowledgeBaseName": "产品文档库",
        "title": "产品相关问题",
        "messageCount": 5,
        "createdAt": "2025-05-10T10:00:00Z",
        "updatedAt": "2025-05-10T11:00:00Z"
      }
    ],
    "total": 10,
    "page": 1,
    "pageSize": 20
  }
}
```

#### GET /api/chat/sessions/{id}/messages

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "role": "user",
        "content": "什么是 RAG？",
        "createdAt": "2025-05-10T10:00:00Z"
      },
      {
        "id": 2,
        "role": "assistant",
        "content": "RAG（检索增强生成）是一种结合检索和生成的 AI 技术...",
        "citations": [
          {
            "documentId": 1,
            "documentName": "产品手册.pdf",
            "chunkId": 45,
            "snippet": "RAG 是 Retrieval-Augmented Generation 的缩写...",
            "pageNumber": 12,
            "score": 0.92
          }
        ],
        "latencyMs": 2500,
        "promptTokens": 512,
        "completionTokens": 256,
        "createdAt": "2025-05-10T10:00:05Z"
      }
    ],
    "total": 2
  }
}
```

#### POST /api/chat/sessions/{id}/messages

**请求体：**

```json
{
  "content": "什么是 RAG？",
  "stream": false
}
```

**响应体（非流式）：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 2,
    "role": "assistant",
    "content": "RAG（检索增强生成）是一种结合检索和生成的 AI 技术...",
    "citations": [
      {
        "documentId": 1,
        "documentName": "产品手册.pdf",
        "chunkId": 45,
        "snippet": "RAG 是 Retrieval-Augmented Generation 的缩写...",
        "pageNumber": 12,
        "score": 0.92
      }
    ],
    "latencyMs": 2500,
    "promptTokens": 512,
    "completionTokens": 256,
    "createdAt": "2025-05-10T10:00:05Z"
  }
}
```

**流式响应（SSE）：**

```
event: message
data: {"type": "content", "content": "RAG"}

event: message
data: {"type": "content", "content": "（检索增强生成）"}

event: message
data: {"type": "citation", "citation": {"documentId": 1, "documentName": "产品手册.pdf", ...}}

event: message
data: {"type": "done", "latencyMs": 2500}
```

### 8.6 反馈接口

#### POST /api/messages/{id}/feedback

**请求体：**

```json
{
  "rating": "up",
  "comment": "回答很有帮助"
}
```

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "messageId": 2,
    "rating": "up",
    "comment": "回答很有帮助",
    "createdAt": "2025-05-10T10:05:00Z"
  }
}
```

### 8.7 日志接口

#### GET /api/logs/messages

**查询参数：**

| 参数            | 类型 | 必填 | 说明       |
| --------------- | ---- | ---- | ---------- |
| page            | int  | 否   | 页码       |
| pageSize        | int  | 否   | 每页数量   |
| knowledgeBaseId | long | 否   | 知识库筛选 |
| startDate       | date | 否   | 开始日期   |
| endDate         | date | 否   | 结束日期   |

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 2,
        "sessionId": 1,
        "knowledgeBaseId": 1,
        "knowledgeBaseName": "产品文档库",
        "question": "什么是 RAG？",
        "answer": "RAG（检索增强生成）是一种...",
        "rating": "up",
        "latencyMs": 2500,
        "promptTokens": 512,
        "completionTokens": 256,
        "createdAt": "2025-05-10T10:00:05Z"
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

#### GET /api/logs/retrievals

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "messageId": 2,
        "queryText": "什么是 RAG？",
        "retrievedChunks": [
          {
            "chunkId": 45,
            "documentName": "产品手册.pdf",
            "snippet": "RAG 是...",
            "score": 0.92
          }
        ],
        "topK": 5,
        "latencyMs": 150,
        "createdAt": "2025-05-10T10:00:01Z"
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

### 8.8 评测接口

#### POST /api/evals/run

**请求体：**

```json
{
  "knowledgeBaseId": 1,
  "name": "产品文档评测-2025Q2",
  "cases": [
    {
      "question": "什么是 RAG？",
      "expectedAnswer": "RAG 是检索增强生成...",
      "expectedSources": [
        { "documentName": "产品手册.pdf", "pageNumber": 12 }
      ]
    }
  ]
}
```

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "knowledgeBaseId": 1,
    "name": "产品文档评测-2025Q2",
    "status": "RUNNING",
    "totalCases": 10,
    "createdAt": "2025-05-10T10:00:00Z"
  }
}
```

#### GET /api/evals

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "knowledgeBaseId": 1,
        "knowledgeBaseName": "产品文档库",
        "name": "产品文档评测-2025Q2",
        "status": "COMPLETED",
        "totalCases": 10,
        "hitCount": 8,
        "hitRate": 0.80,
        "mrr": 0.75,
        "avgLatencyMs": 2300,
        "avgScore": 0.85,
        "createdAt": "2025-05-10T10:00:00Z"
      }
    ],
    "total": 5,
    "page": 1,
    "pageSize": 20
  }
}
```

#### GET /api/evals/{id}

**响应体：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "knowledgeBaseId": 1,
    "knowledgeBaseName": "产品文档库",
    "name": "产品文档评测-2025Q2",
    "status": "COMPLETED",
    "totalCases": 10,
    "hitCount": 8,
    "hitRate": 0.80,
    "mrr": 0.75,
    "avgLatencyMs": 2300,
    "avgScore": 0.85,
    "cases": [
      {
        "id": 1,
        "question": "什么是 RAG？",
        "expectedAnswer": "RAG 是检索增强生成...",
        "actualAnswer": "RAG（检索增强生成）是一种...",
        "retrievalHit": true,
        "retrievalRank": 1,
        "score": 0.92,
        "latencyMs": 2500
      }
    ],
    "createdAt": "2025-05-10T10:00:00Z",
    "finishedAt": "2025-05-10T10:05:00Z"
  }
}
```

---

## 9. RAG 方案设计

### 9.1 文本切分策略

#### 9.1.1 切分参数

| 参数           | 值             | 说明                       |
| -------------- | -------------- | -------------------------- |
| Chunk Size     | 500-800 tokens | 平衡上下文完整性和检索精度 |
| Overlap        | 100-150 tokens | 避免边界信息丢失           |
| Min Chunk Size | 100 tokens     | 过小的片段合并             |

#### 9.1.2 切分策略

```java
public interface ChunkingStrategy {
    List<Chunk> chunk(String content, ChunkingConfig config);
}

// 1. 固定长度切分（简单场景）
public class FixedSizeChunker implements ChunkingStrategy { ... }

// 2. 语义切分（推荐）
public class SemanticChunker implements ChunkingStrategy {
    // 基于段落、标题等语义边界切分
    // 优先在标题、段落边界处切分
}

// 3. 递归字符切分（通用）
public class RecursiveCharacterChunker implements ChunkingStrategy {
    // 依次尝试：段落 -> 句子 -> 词 -> 字符
}
```

#### 9.1.3 元数据提取

每个 Chunk 保存以下元数据：

```json
{
  "documentId": 1,
  "documentName": "产品手册.pdf",
  "chunkIndex": 45,
  "pageNumber": 12,
  "sectionTitle": "第三章 RAG 技术介绍",
  "charStart": 15000,
  "charEnd": 15800
}
```

### 9.2 检索策略

#### 9.2.1 向量检索（MVP）

```sql
-- 余弦相似度检索
SELECT 
    dc.id,
    dc.content,
    dc.metadata,
    1 - (dc.embedding <=> query_vector) as score
FROM document_chunks dc
WHERE dc.knowledge_base_id = :kbId
ORDER BY dc.embedding <=> query_vector
LIMIT :topK;
```

#### 9.2.2 混合检索（二期）

```java
public class HybridRetriever {
    
    public List<Chunk> retrieve(String query, int topK) {
        // 1. 向量检索
        List<Chunk> vectorResults = vectorRetriever.retrieve(query, topK * 2);
        
        // 2. BM25 关键词检索
        List<Chunk> bm25Results = bm25Retriever.retrieve(query, topK * 2);
        
        // 3. RRF 融合排序
        return rrfFuser.fuse(vectorResults, bm25Results, topK);
    }
}
```

**RRF (Reciprocal Rank Fusion) 公式：**

```
score(d) = Σ 1 / (k + rank(d))
```

其中 `k` 通常取 60。

#### 9.2.3 Rerank 重排（二期）

```java
public class RerankService {
    
    public List<Chunk> rerank(String query, List<Chunk> candidates, int topK) {
        // 调用 Rerank 模型（如 Cohere Rerank / BGE Reranker）
        List<RerankResult> results = rerankModel.rerank(query, candidates);
        
        return results.stream()
            .sorted(Comparator.comparing(RerankResult::getScore).reversed())
            .limit(topK)
            .map(r -> candidates.get(r.getIndex()))
            .collect(Collectors.toList());
    }
}
```

### 9.3 Prompt 设计

#### 9.3.1 系统提示词

```
你是一个专业的企业知识库助手。你的任务是基于提供的文档片段回答用户问题。

## 规则
1. 只能基于提供的上下文回答问题，不要使用外部知识
2. 如果上下文中没有足够信息回答问题，明确说明"根据现有文档无法回答该问题"
3. 在回答中标注引用来源，格式为 [文档名, 第X页]
4. 回答要准确、简洁、专业
5. 如果问题模糊，可以要求用户澄清

## 回答格式
- 先给出直接回答
- 然后列出引用来源
```

#### 9.3.2 用户提示词模板

```
## 上下文文档

{contexts}

---

## 用户问题

{question}

---

请基于以上上下文回答问题，并在回答中标注引用来源。
```

### 9.4 引用结构设计

```java
@Data
public class Citation {
    private Long documentId;
    private String documentName;
    private Long chunkId;
    private String snippet;      // 来源片段摘要 (前200字符)
    private Integer pageNumber;  // 页码（可选）
    private String sectionTitle; // 章节标题（可选）
    private Double score;        // 相似度分数
}
```

---

## 10. 错误处理设计

### 10.1 错误处理策略

#### 10.1.1 文档解析失败

```java
public class DocumentProcessingException extends RuntimeException {
    private final DocumentStatus status;
    private final String documentId;
}

// 处理策略
@Retryable(
    value = {DocumentParsingException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
public void processDocument(Long documentId) { ... }

// 最终失败处理
@Recover
public void recover(DocumentParsingException e, Long documentId) {
    documentService.updateStatus(documentId, DocumentStatus.FAILED, e.getMessage());
    alertService.sendAlert("文档处理失败", documentId, e);
}
```

#### 10.1.2 LLM 调用失败

```java
public class LlmService {
    
    @CircuitBreaker(name = "llm", fallbackMethod = "fallback")
    @Retryable(value = {LlmTimeoutException.class}, maxAttempts = 2)
    public String generate(String prompt) { ... }
    
    public String fallback(String prompt, Exception e) {
        log.error("LLM 调用失败", e);
        throw new ServiceException(50002, "AI 服务暂时不可用，请稍后重试");
    }
}
```

#### 10.1.3 向量入库失败

```java
@Transactional
public void indexDocument(Long documentId, List<Chunk> chunks, List<float[]> embeddings) {
    try {
        // MyBatis-Plus 批量插入
        chunkService.saveBatch(chunks);
        // 向量数据通过原生 SQL 批量写入 pgvector
        chunkMapper.batchInsertEmbeddings(chunks, embeddings);
    } catch (DataAccessException e) {
        // 标记失败，等待重试
        documentService.updateStatus(documentId, DocumentStatus.FAILED, "索引构建失败");
        throw e;
    }
}
```

### 10.2 异常分类

| 异常类型     | HTTP 状态码 | 错误码 | 处理方式               |
| ------------ | ----------- | ------ | ---------------------- |
| 参数校验异常 | 400         | 40001  | 返回字段级错误         |
| 认证异常     | 401         | 401xx  | 返回登录提示           |
| 权限异常     | 403         | 403xx  | 返回无权限提示         |
| 资源不存在   | 404         | 404xx  | 返回资源不存在提示     |
| 业务冲突     | 409         | 409xx  | 返回冲突原因           |
| LLM 服务异常 | 503         | 50002  | 返回服务不可用提示     |
| 内部错误     | 500         | 50001  | 记录日志，返回通用错误 |

---

## 11. 安全设计

### 11.1 认证与授权

#### 11.1.1 JWT 配置

| 配置项               | 值                       |
| -------------------- | ------------------------ |
| 签名算法             | HS256 / RS256            |
| Access Token 有效期  | 15 分钟                  |
| Refresh Token 有效期 | 7 天                     |
| Refresh Token 存储   | HttpOnly Cookie + Secure |

#### 11.1.2 权限控制

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

### 11.2 数据隔离

```java
// 知识库数据隔离
@PreAuthorize("@knowledgeBaseService.isOwner(#kbId, authentication.name)")
public KnowledgeBase getKnowledgeBase(Long kbId) { ... }

// 文档数据隔离 —— MyBatis-Plus LambdaQueryWrapper
public List<Document> getDocuments(Long kbId, Long userId) {
    // 验证用户是否有权访问该知识库
    if (!knowledgeBaseMapper.existsByKbIdAndUserId(kbId, userId)) {
        throw new ForbiddenException("无权访问该知识库");
    }
    return documentMapper.selectList(
        new LambdaQueryWrapper<Document>().eq(Document::getKnowledgeBaseId, kbId)
    );
}
```

### 11.3 文件上传安全

```java
public class FileUploadValidator {
    
    private static final Set<String> ALLOWED_TYPES = Set.of("PDF", "DOCX", "MD");
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    
    public void validate(MultipartFile file) {
        // 1. 文件大小检查
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("文件大小超过限制");
        }
        
        // 2. 文件类型检查（基于 Magic Number，非扩展名）
        String fileType = detectFileType(file.getInputStream());
        if (!ALLOWED_TYPES.contains(fileType)) {
            throw new ValidationException("不支持的文件类型");
        }
        
        // 3. 文件名安全处理
        String safeFileName = sanitizeFileName(file.getOriginalFilename());
    }
}
```

### 11.4 敏感数据处理

| 数据类型   | 处理方式                  |
| ---------- | ------------------------- |
| 密码       | BCrypt 哈希存储           |
| JWT Secret | 环境变量 / Secret Manager |
| API Key    | 加密存储，日志脱敏        |
| 用户邮箱   | 日志中脱敏显示            |

---

## 12. 可观测性设计

### 12.1 日志规范

#### 12.1.1 日志格式

```json
{
  "timestamp": "2025-05-10T10:00:00.000Z",
  "level": "INFO",
  "traceId": "abc123",
  "spanId": "def456",
  "service": "copilot-api",
  "class": "com.example.copilot.service.ChatService",
  "message": "Chat request processed",
  "context": {
    "userId": 1,
    "sessionId": 10,
    "knowledgeBaseId": 1,
    "latencyMs": 2500
  }
}
```

#### 12.1.2 关键日志点

| 场景         | 日志级别 | 内容                                      |
| ------------ | -------- | ----------------------------------------- |
| 用户登录     | INFO     | userId, email, ip                         |
| 文档上传     | INFO     | documentId, fileName, fileSize            |
| 文档处理完成 | INFO     | documentId, chunkCount, duration          |
| 问答请求     | INFO     | sessionId, question, latencyMs            |
| 问答请求失败 | ERROR    | sessionId, error, stackTrace              |
| LLM 调用     | DEBUG    | promptTokens, completionTokens, latencyMs |
| 检索请求     | DEBUG    | query, topK, latencyMs, hitCount          |

### 12.2 指标监控

#### 12.2.1 核心指标

| 指标名                                 | 类型      | 说明               |
| -------------------------------------- | --------- | ------------------ |
| `http_requests_total`                  | Counter   | HTTP 请求总数      |
| `http_request_duration_seconds`        | Histogram | HTTP 请求耗时分布  |
| `llm_requests_total`                   | Counter   | LLM 调用总数       |
| `llm_request_duration_seconds`         | Histogram | LLM 调用耗时       |
| `llm_tokens_total`                     | Counter   | Token 使用量       |
| `embedding_requests_total`             | Counter   | Embedding 请求总数 |
| `retrieval_duration_seconds`           | Histogram | 检索耗时           |
| `document_processing_duration_seconds` | Histogram | 文档处理耗时       |
| `active_sessions`                      | Gauge     | 活跃会话数         |

#### 12.2.2 告警规则

以下告警规则更适合系统进入多人使用阶段后接入，MVP 阶段可先以日志排查和基础指标观测为主。

| 告警名称       | 条件                    | 级别     |
| -------------- | ----------------------- | -------- |
| 高错误率       | 错误率 > 5% 持续 5 分钟 | Critical |
| LLM 响应慢     | P95 > 10s 持续 5 分钟   | Warning  |
| 文档处理积压   | 待处理文档 > 100        | Warning  |
| Token 使用异常 | 日使用量 > 阈值 2 倍    | Warning  |

### 12.3 链路追踪

如需补充链路追踪，可在后续版本接入 Zipkin 或 Jaeger：

```yaml
spring:
  sleuth:
    enabled: true
    sampler:
      probability: 0.1  # 采样率 10%
```

---

## 13. 测试策略

### 13.1 测试金字塔

```
        ┌─────────────┐
        │   E2E Test  │  10%
        ├─────────────┤
        │Integration  │  20%
        │    Test     │
        ├─────────────┤
        │  Unit Test  │  70%
        └─────────────┘
```

### 13.2 单元测试

```java
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {
    
    @Mock
    private RetrievalService retrievalService;
    
    @Mock
    private LlmService llmService;
    
    @InjectMocks
    private ChatService chatService;
    
    @Test
    void shouldReturnAnswerWithCitations() {
        // Given
        String question = "什么是 RAG？";
        List<Chunk> chunks = List.of(
            new Chunk(1L, "RAG 是检索增强生成...", 0.92)
        );
        String answer = "RAG 是一种 AI 技术...";
        
        when(retrievalService.retrieve(any(), eq(5))).thenReturn(chunks);
        when(llmService.generate(any())).thenReturn(answer);
        
        // When
        ChatResponse response = chatService.chat(1L, question);
        
        // Then
        assertThat(response.getContent()).isEqualTo(answer);
        assertThat(response.getCitations()).hasSize(1);
        verify(logService).saveMessageLog(any());
    }
}
```

### 13.3 集成测试

```java
@SpringBootTest
@Testcontainers
class DocumentIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg15")
        .withDatabaseName("test");
    
    @Autowired
    private DocumentService documentService;
    
    @Test
    void shouldProcessDocumentEndToEnd() throws Exception {
        // Given
        MultipartFile file = createTestFile("test.pdf");
        
        // When
        Document doc = documentService.upload(1L, file);
        
        // Then
        await().atMost(30, SECONDS).until(() -> 
            documentService.getStatus(doc.getId()) == DocumentStatus.READY
        );
        
        List<Chunk> chunks = chunkMapper.selectList(
            new LambdaQueryWrapper<Chunk>().eq(Chunk::getDocumentId, doc.getId())
        );
        assertThat(chunks).isNotEmpty();
    }
}
```

### 13.4 评测数据集

```json
{
  "name": "产品文档评测集",
  "version": "1.0",
  "cases": [
    {
      "id": 1,
      "question": "什么是 RAG？",
      "expectedAnswer": "RAG 是检索增强生成技术...",
      "expectedSources": [
        { "documentName": "产品手册.pdf", "pageNumber": 12 }
      ],
      "tags": ["概念", "RAG"]
    }
  ]
}
```

---

## 14. CI/CD 设计

CI/CD 不作为 MVP 的强制交付项。第一阶段重点是保证本地开发、数据库迁移、Docker 启动和基础测试可稳定运行，后续再补自动化流水线。

### 14.1 GitHub Actions 工作流

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: pgvector/pgvector:pg15
        env:
          POSTGRES_DB: test
          POSTGRES_PASSWORD: test
        ports:
          - 5432:5432
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Run tests
        run: ./mvnw test

  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Build Docker image
        run: docker build -t copilot-api:${{ github.sha }} .
      - name: Push to Registry
        run: |
          docker tag copilot-api:${{ github.sha }} registry.example.com/copilot-api:latest
          docker push registry.example.com/copilot-api:latest

  deploy:
    needs: build
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Production
        run: |
          ssh user@server "cd /app && docker-compose pull && docker-compose up -d"
```

### 14.2 环境配置

| 环境        | 配置文件                | 说明     |
| ----------- | ----------------------- | -------- |
| Development | application-dev.yml     | 本地开发 |
| Staging     | application-staging.yml | 测试环境 |
| Production  | application-prod.yml    | 生产环境 |

---

## 15. 部署架构

### 15.1 Docker Compose 部署

```yaml
version: '3.8'

services:
  api:
    build: ./server
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=postgres
      - REDIS_HOST=redis
    depends_on:
      - postgres
      - redis

  web:
    build: ./web
    ports:
      - "3000:3000"
    depends_on:
      - api

  postgres:
    image: pgvector/pgvector:pg15
    volumes:
      - postgres_data:/var/lib/postgresql/data
    environment:
      - POSTGRES_DB=copilot
      - POSTGRES_PASSWORD=secret

  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data

  minio:
    image: minio/minio
    command: server /data --console-address ":9001"
    volumes:
      - minio_data:/data
    ports:
      - "9000:9000"
      - "9001:9001"

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - api
      - web

volumes:
  postgres_data:
  redis_data:
  minio_data:
```

### 15.2 演进版部署架构

以下架构适用于后续扩展到多人协作和更高并发场景，不作为第一阶段默认部署形态：

```
                    ┌─────────────┐
                    │   用户请求   │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │    CDN      │
                    │  (静态资源)  │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │    Nginx    │
                    │  (反向代理)  │
                    └──────┬──────┘
                           │
           ┌───────────────┼───────────────┐
           │               │               │
           ▼               ▼               ▼
    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
    │  API Pod 1  │ │  API Pod 2  │ │  API Pod 3  │
    └──────┬──────┘ └──────┬──────┘ └──────┬──────┘
           │               │               │
           └───────────────┼───────────────┘
                           │
           ┌───────────────┼───────────────┐
           │               │               │
           ▼               ▼               ▼
    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
    │ PostgreSQL  │ │    Redis    │ │   MinIO     │
    │  (Primary)  │ │  (Cluster)  │ │  (Cluster)  │
    │  + Replica  │ │             │ │             │
    └─────────────┘ └─────────────┘ └─────────────┘
```

---

## 16. 工程目录结构

### 16.1 后端目录

```
server/
├── src/main/java/com/example/copilot/
│   ├── CopilotApplication.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── KnowledgeBaseController.java
│   │   ├── DocumentController.java
│   │   ├── ChatController.java
│   │   ├── LogController.java
│   │   └── EvalController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── KnowledgeBaseService.java
│   │   ├── DocumentService.java
│   │   ├── ChatService.java
│   │   ├── LogService.java
│   │   └── EvalService.java
│   ├── mapper/
│   │   ├── UserMapper.java
│   │   ├── KnowledgeBaseMapper.java
│   │   ├── DocumentMapper.java
│   │   ├── ChunkMapper.java
│   │   ├── ChatSessionMapper.java
│   │   ├── MessageMapper.java
│   │   └── EvalRunMapper.java
│   ├── entity/
│   │   ├── User.java
│   │   ├── KnowledgeBase.java
│   │   ├── Document.java
│   │   ├── DocumentChunk.java
│   │   ├── ChatSession.java
│   │   ├── Message.java
│   │   └── EvalRun.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── LoginRequest.java
│   │   │   ├── CreateKnowledgeBaseRequest.java
│   │   │   ├── ChatRequest.java
│   │   │   └── ...
│   │   └── response/
│   │       ├── ApiResponse.java
│   │       ├── PageResponse.java
│   │       ├── ChatResponse.java
│   │       └── ...
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── JwtConfig.java
│   │   ├── AsyncConfig.java
│   │   └── OpenApiConfig.java
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── UserPrincipal.java
│   ├── rag/
│   │   ├── RagService.java
│   │   ├── RetrievalService.java
│   │   ├── EmbeddingService.java
│   │   ├── ChunkingService.java
│   │   ├── PromptBuilder.java
│   │   └── parser/
│   │       ├── DocumentParser.java
│   │       ├── PdfParser.java
│   │       ├── DocxParser.java
│   │       └── MarkdownParser.java
│   ├── eval/
│   │   ├── EvalRunner.java
│   │   ├── EvalMetricsCalculator.java
│   │   └── EvalReportGenerator.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   ├── ValidationException.java
│   │   └── NotFoundException.java
│   └── common/
│       ├── Constants.java
│       ├── ErrorCode.java
│       └── utils/
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/
│       ├── V1__Create_users_table.sql
│       ├── V2__Create_knowledge_bases_table.sql
│       ├── V3__Create_documents_table.sql
│       └── ...
├── src/test/java/
│   └── com/example/copilot/
│       ├── service/
│       ├── controller/
│       └── integration/
├── Dockerfile
├── pom.xml
└── README.md
```

### 16.2 前端目录

```
web/
├── app/
│   ├── layout.tsx
│   ├── page.tsx
│   ├── login/
│   ├── knowledge-bases/
│   │   ├── page.tsx
│   │   └── [id]/
│   │       ├── page.tsx
│   │       └── chat/
│   │           └── page.tsx
│   ├── logs/
│   └── evals/
├── components/
│   ├── ui/
│   │   ├── button.tsx
│   │   ├── input.tsx
│   │   └── ...
│   ├── chat/
│   │   ├── ChatMessage.tsx
│   │   ├── ChatInput.tsx
│   │   ├── CitationCard.tsx
│   │   └── StreamingMessage.tsx
│   ├── knowledge-base/
│   │   ├── KnowledgeBaseCard.tsx
│   │   └── DocumentList.tsx
│   └── layout/
│       ├── Header.tsx
│       ├── Sidebar.tsx
│       └── Footer.tsx
├── lib/
│   ├── api/
│   │   ├── client.ts
│   │   ├── auth.ts
│   │   ├── knowledge-base.ts
│   │   └── chat.ts
│   └── utils/
├── hooks/
│   ├── useAuth.ts
│   ├── useChat.ts
│   └── useKnowledgeBase.ts
├── types/
│   ├── api.ts
│   ├── chat.ts
│   └── knowledge-base.ts
├── public/
├── tailwind.config.ts
├── next.config.js
├── package.json
└── README.md
```

---

## 17. 开发计划

以下计划按个人项目节奏设计，目标是在 3 到 4 周内完成可上线演示版本。

### 第一阶段：项目骨架（0.5-1 周）

| 任务                                | 预计时间 |
| ----------------------------------- | -------- |
| 初始化 Spring Boot 项目             | 0.5 天   |
| 初始化 Next.js 前端项目             | 0.5 天   |
| 配置 PostgreSQL + pgvector + Flyway | 1 天     |
| 搭建登录注册和 JWT 鉴权             | 1.5 天   |
| 搭建前端基础布局                    | 1 天     |
| 输出基础接口文档                    | 0.5 天   |

### 第二阶段：知识库与文档导入（1-1.5 周）

| 任务                      | 预计时间 |
| ------------------------- | -------- |
| 实现知识库 CRUD           | 2 天     |
| 实现文档上传接口          | 1 天     |
| 实现 PDF 解析             | 2 天     |
| 实现 DOCX 解析            | 1 天     |
| 实现 Markdown 解析        | 0.5 天   |
| 实现文本切分              | 1 天     |
| 实现 Embedding 和向量入库 | 2 天     |
| 实现文档状态追踪          | 0.5 天   |
| 前端知识库和文档管理页面  | 1.5 天   |

### 第三阶段：问答主流程（1-1.5 周）

| 任务                    | 预计时间 |
| ----------------------- | -------- |
| 实现聊天会话管理        | 1 天     |
| 实现向量检索            | 2 天     |
| 实现 Prompt 构建        | 1 天     |
| 实现 LLM 调用和答案生成 | 2 天     |
| 实现引用信息组装        | 1 天     |
| 实现消息历史保存        | 0.5 天   |
| 前端聊天界面            | 2 天     |

### 第四阶段：日志与评测（0.5-1 周）

| 任务               | 预计时间 |
| ------------------ | -------- |
| 实现问答日志查询   | 1 天     |
| 实现检索日志查询   | 0.5 天   |
| 实现反馈接口       | 0.5 天   |
| 实现评测任务管理   | 1 天     |
| 实现评测执行器     | 1.5 天   |
| 实现评测指标计算   | 1 天     |
| 前端日志和评测页面 | 1 天     |

### 第五阶段：部署与完善（0.5-1 周）

| 任务                    | 预计时间 |
| ----------------------- | -------- |
| Docker 化               | 1 天     |
| Docker Compose 编排     | 0.5-1 天 |
| 补充 README 和 API 文档 | 0.5-1 天 |
| 补充架构图和演示截图    | 1 天     |
| 部署上线                | 1 天     |

---

## 18. 简历价值点

该项目完成后，简历可以重点突出以下内容：

### 18.1 技术能力

- **Java 后端工程**：独立完成 Spring Boot 3 + Java 21 企业级应用设计与实现
- **AI 应用开发**：掌握 Spring AI Alibaba 框架，理解 LLM 调用、Prompt 工程和 Token 管理
- **RAG 系统设计**：实现完整的文档解析、向量检索、带引用回答闭环
- **向量检索**：使用 PostgreSQL + pgvector 构建向量检索能力，理解相似度计算和索引优化
- **工程化能力**：完成 Docker 容器化、CI/CD 流水线、可观测性建设

### 18.2 项目亮点

```
Enterprise Knowledge Copilot | Java + Spring Boot + Spring AI Alibaba + pgvector

• 设计并实现企业级 RAG 知识库问答系统，支持 PDF/DOCX/Markdown 文档解析和向量检索
• 基于 PostgreSQL + pgvector 构建向量检索引擎，实现 Top-K 相似度检索和引用回溯
• 设计文档异步处理流水线，支持大文件解析、文本切分、Embedding 生成和索引构建
• 实现基础评测体系，支持检索命中率、回答耗时和引用质量等核心指标统计
• 完成 Docker 容器化部署，并为后续监控、告警和持续集成预留扩展空间
```

---

## 19. 面试准备

面试官会通过这个项目判断你是否真的理解自己做的东西。以下是按模块分类的常见追问和回答要点。

### 19.1 RAG 相关

**Q: "Chunk 大小为什么选 500-800？如果变大或变小会怎样？"**
- 变大（1000+）→ 检索精度下降，单个 chunk 噪音多；Prompt 变长 → Token 成本升高
- 变小（200-300）→ 缺少完整语义，LLM 无法理解上下文；检索结果碎片化
- 500-800 是实践中的 sweet spot，平衡了语义完整性和检索精度

**Q: "用户问的问题在文档里找不到怎么办？"**
- 相似度阈值过滤：低于 0.75 的 chunk 丢弃
- Prompt 中明确要求："如果找不到，就说不知道，不要编造"
- 如果所有 chunk 相似度都低，直接返回"该问题超出了知识库范围"，不调用 LLM，省成本
- 日志中标记这类问题，后续可以分析是否需要补充文档

**Q: "向量检索和关键词检索各有什么优劣？什么场景该混合？"**
- 向量检索：语义理解好，同义词、"意思相近"能匹配；但事实性、精确术语查询差
- 关键词检索（BM25）：精确匹配好，专有名词、编号查询准确；但不会理解同义改写
- 混合场景：用户问"2024 年的绩效制度"时，向量检索抓语义，BM25 精确命中"2024"和"绩效"

**Q: "pgvector 的 IVFFlat 索引原理是什么？为什么不用 HNSW？"**
- IVFFlat：K-means 聚类 → 分成 N 个列表 → 检索时只扫最近的几个列表，不是全表扫描
- 需要先建索引再查（需要一定数据量做 K-means）
- HNSW 图索引在百万级以上向量时性能更好，但内存占用大，MVP 阶段用不上

**Q: "怎么评估 RAG 系统效果好不好？"**
- 三个维度：检索质量（hit rate）、回答质量（faithfulness）、用户体验（latency）
- 检索：准备标注了预期 chunk 的测试集，自动算 hit_rate@5
- 回答：用 RAGAS 框架的 faithfulness 指标，或 GPT-4 做 LLM Judge
- 项目里做了基础评测体系就是针对这个

### 19.2 后端工程相关

**Q: "Spring AI Alibaba 做了什么？为什么不用自己封装 HTTP 调用？"**
- Spring AI Alibaba 封装了通义千问/DeepSeek 的 LLM/Embedding 调用、重试、流式输出，不用自己写 HttpClient + JSON 解析
- 对比直接用百炼 SDK：Spring AI Alibaba 与 Spring Boot 生态无缝集成，依赖注入、配置管理、异常处理都是现成的
- 对比原版 Spring AI：原生支持阿里云百炼，通义千问 Chat + Embedding 一站式接入，有中文文档和免费额度
- 但框架不帮你设计 RAG 流程——检索→拼 Prompt→调 LLM→解析引用这条链路的编排逻辑是我自己写的

**Q: "文档上传后为什么要异步处理？怎么实现的？"**
- Embedding 调用和 PDF 解析需要 10-30 秒，同步等待会让 HTTP 请求超时
- Spring 的 `@Async` + 自定义线程池；前端上传后立即返回，轮询 `/documents/{id}/status`
- 失败重试：Spring Retry，3 次指数退避，全部失败后标记 FAILED + 记录错误原因

**Q: "如果一个 100MB 的 PDF 上传了怎么办？"**
- 上传层校验：文件大小限制 20MB（或 50MB）
- 类型校验：基于 Magic Number，不是扩展名（防止改后缀绕过）
- 如果真的有 100MB，拒绝上传并返回明确错误提示

**Q: "用户 A 能看到用户 B 的知识库吗？做了什么防护？"**
- 所有知识库查询 SQL 必须带 `owner_id = :currentUserId` 条件
- Service 层通过 `@PreAuthorize` + Spring Security 的 Authentication 拿到当前用户
- 单元测试专门有一个用例：用户 A 请求用户 B 的知识库 ID → 期望 403

### 19.3 系统设计相关

**Q: "如果知识库有 100 万份文档，现在的架构哪里会出问题？"**
- pgvector IVFFlat 在百万级 chunk 下检索速度下降 → 换 HNSW 索引或迁到 Milvus
- 文档解析异步队列可能积压 → 换消息队列（RabbitMQ/Kafka），增加 Worker 实例
- 单 PostgreSQL 实例扛不住 → 读写分离、分库分表
- 好的系统设计应该知道当前方案的边界，而不是借口"这是 MVP"

**Q: "如果要支持多租户，怎么改？"**
- 数据库层：每个表加 `tenant_id`，所有查询都必须带 tenant 过滤
- 或：PostgreSQL Row-Level Security，数据库层面自动过滤
- Embedding 检索也要加 `WHERE tenant_id = ?`，不能跨租户检索
- 不太建议每租户独立数据库，个人项目场景过重

**Q: "LLM 调用很慢（10 秒+），怎么优化用户体验？"**
- 流式输出（SSE）：首 token 到达就显示，不用等完整回答
- 如果 LLM 不可用：返回降级回答 ——"AI 暂时不可用，以下是相关文档片段：[检索结果]"
- 高频问题缓存：相同问题（或相似度 > 0.95）直接返回缓存答案

### 19.4 行为面试相关

**Q: "为什么做这个项目？"**
- 对 AI 应用开发感兴趣，想深入理解 RAG 不只是"调 API"
- Java 后端 + AI 结合的项目比较少见，想证明 Java 也能做 AI
- 从 0 到 1 完整实现一个产品，包括设计、开发、部署，锻炼全栈能力

**Q: "项目中遇到的最大挑战是什么？"**
- 建议准备一个具体的技术问题，比如：
  - "文档解析质量：某个 PDF 的表格内容提取出来是乱的，我用 POI 的特定 API 重新处理了表格"
  - "引用准确性：LLM 生成的引用编号和实际 chunk 对不上，我在 Prompt 中加入了具体的 chunk ID 而非序号"

**Q: "如果重新做，会怎么做？"**
- V0 阶段先把流式输出做了，体验差距很大
- 评测集应该在上传文档时就准备好，而不是做完问答再补
- 会考虑用一个轻量的 Python 微服务专门做文档解析（PDF 解析在 Python 生态更好）

---

## 20. 后续扩展方向

| 方向              | 优先级 | 说明                           |
| ----------------- | ------ | ------------------------------ |
| 多租户支持        | P1     | 企业级必备，支持租户隔离       |
| 权限分级          | P1     | 支持知识库级别的读写权限控制   |
| OCR 文档识别      | P2     | 支持扫描件 PDF 和图片文档      |
| 混合检索与 Rerank | P0     | 提升检索精度                   |
| 知识库增量更新    | P1     | 定时检测文档变更，自动更新索引 |
| Agent 工具调用    | P2     | 支持联网搜索、数据库查询等工具 |
| 多模态支持        | P2     | 支持图片、表格等多模态内容理解 |
| 知识图谱          | P3     | 构建实体关系图谱，增强推理能力 |

---

## 21. 附录

### 20.1 参考资料

- [Spring AI Alibaba 官方文档](https://java2ai.com/)
- [pgvector 官方文档](https://github.com/pgvector/pgvector)
- [RAG 最佳实践](https://www.anthropic.com/news/contextual-retrieval)
- [阿里云百炼 DashScope 文档](https://help.aliyun.com/document_detail/2712195.html)

### 20.2 术语表

| 术语      | 说明                                         |
| --------- | -------------------------------------------- |
| RAG       | Retrieval-Augmented Generation，检索增强生成 |
| Chunk     | 文档分片，将长文档切分成小块便于检索         |
| Embedding | 向量化，将文本转换为高维向量表示             |
| Top-K     | 检索返回的最相似的 K 个结果                  |
| Hit Rate  | 命中率，检索结果中包含正确答案的比例         |
| MRR       | Mean Reciprocal Rank，平均倒数排名           |
| Rerank    | 重排序，对检索结果进行二次排序优化           |

---

**文档版本**：v3.2
**最后更新**：2026-05-10
**v3.2 更新内容**：Spring AI 改为 Spring AI Alibaba；模型服务从 OpenAI API 改为阿里云百炼 DashScope；Embedding 从 text-embedding-3-small 改为通义千问 text-embedding-v2
