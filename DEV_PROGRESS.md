# 开发过程记录

## 1. 项目初始化

- 明确项目目标为企业知识库 AI Copilot，按 `DESIGN.md` 的 `V0 -> V1 -> V2` 分阶段推进。
- 先落地后端最小可运行骨架，避免一开始就陷入前端或模型接入细节。

## 2. 后端基础骨架

- 创建 `server/` Spring Boot 项目。
- 接入核心依赖：
  - Spring Web
  - Spring Security
  - MyBatis-Plus
  - Flyway
  - PostgreSQL
  - JWT
  - Knife4j
- 建立基础包结构：
  - `controller/`
  - `service/`
  - `mapper/`
  - `entity/`
  - `dto/`
  - `config/`
  - `security/`
  - `exception/`
  - `rag/`

## 3. V0 认证与知识库

- 编写统一返回结构 `ApiResponse` 和错误码 `ErrorCode`。
- 增加全局异常处理 `GlobalExceptionHandler`。
- 实现 JWT 鉴权：
  - `JwtTokenProvider`
  - `JwtAuthenticationFilter`
  - `CustomUserDetailsService`
- 实现认证接口：
  - `POST /api/auth/register`
  - `POST /api/auth/login`
- 实现知识库接口：
  - `POST /api/knowledge-bases`
  - `GET /api/knowledge-bases`
  - `GET /api/knowledge-bases/{id}`
- 修复安全异常默认返回空响应的问题，未登录和无权限场景统一返回 JSON。

## 4. 数据库迁移

- `V1__init_v0_tables.sql`
  - `users`
  - `knowledge_bases`
- `V2__add_documents_and_chat_tables.sql`
  - `documents`
  - `chat_sessions`
  - `messages`
- `V3__add_document_chunks_table.sql`
  - `document_chunks`

## 5. 文档与会话持久化

- 新增文档接口：
  - 上传文档
  - 查询文档列表
  - 查询文档状态
- 新增会话接口：
  - 创建会话
  - 查询会话列表
  - 写入用户消息
  - 查询消息列表
- 当前阶段先完成“可保存、可查询”，为后续 RAG 接入打底。

## 6. 文档处理链路

- 接入异步配置 `AsyncConfig`。
- 实现文档解析器：
  - PDF：`PDFBox`
  - DOCX：`POI`
  - Markdown：文件直读
- 实现 `DocumentParserFactory` 统一选择解析器。
- 实现 `ChunkingService` 对解析文本做基础切分。
- 上传文档后自动触发异步处理，状态流转为：
  - `UPLOADED`
  - `PARSING`
  - `INDEXING`
  - `READY / FAILED`
- 切分结果写入 `document_chunks`。

## 7. 聊天主链路升级

- 新增 `RetrievalService`，先基于 `document_chunks` 做轻量级文本片段检索。
- 升级消息接口逻辑：
  - 先保存用户消息
  - 再检索知识库片段
  - 再生成一条临时 assistant 回复
  - 最后把 assistant 消息写入 `messages`
- 当前 assistant 回复还是占位实现，本质上是“检索结果摘要”，用于先打通链路。
- 后续会把这部分替换为真实的 LLM 生成回答。

## 8. 检索日志与引用结构

- 新增 `retrieval_logs` 表，记录每次 assistant 回答对应的检索过程。
- 检索结果从“纯文本列表”升级为结构化片段：
  - `chunkId`
  - `documentId`
  - `chunkIndex`
  - `snippet`
- assistant 消息中的 `citationsJson` 现在会保存结构化引用信息。
- 每次问答完成后会额外落一条检索日志，便于后续做日志页和评测。

## 9. DashScope 初版接入骨架

- 接入 `Spring AI Alibaba DashScope` 依赖。
- 新增 `AiProperties` 管理 AI 开关、系统提示词、检索 TopK 等配置。
- 新增 `LlmAnswerService`：
  - 从 Spring 容器获取 `ChatModel`
  - 组装系统提示词和用户问题
  - 传入检索片段生成回答
- 设计为“可降级”模式：
  - 如果未配置 API Key
  - 或模型 Bean 不可用
  - 或调用异常
  - 则自动回退到当前临时检索摘要回答
- 这样你后续只需要补百炼配置即可切换到真实模型调用。

## 10. Embedding 与 pgvector 初版接入

- 新增 `V5__enable_pgvector_and_add_embeddings.sql`：
  - 启用 `pgvector` 扩展
  - 为 `document_chunks` 增加 `embedding` 列
  - 建立向量索引
- 新增 `EmbeddingService`：
  - 调用 DashScope EmbeddingModel
  - 提供 `String -> float[]` 和批量 embedding 能力
  - 提供 pgvector 字面量转换工具
- 文档异步处理流程升级为：
  - 解析文档
  - 切分 chunk
  - 为 chunk 生成 embedding
  - 将 embedding 与 chunk 一起写入数据库
- 检索服务升级为：
  - 优先走 pgvector 向量相似度检索
  - 若 embedding 不可用或向量 SQL 失败，则回退到原有文本匹配

## 11. Web 前端骨架

- 新建 `web/` 前端工程骨架，技术栈按设计文档使用：
  - Next.js App Router
  - TypeScript
  - Tailwind CSS
  - React Query
  - Zustand
  - Framer Motion
- 搭建页面结构：
  - 首页
  - 登录页
  - 知识库列表页
  - 知识库详情页
  - 问答页
  - 日志页占位
  - 评测页占位
- 搭建 API 客户端：
  - auth
  - knowledge-base
  - chat
- 搭建核心 UI 组件：
  - Header
  - Button / Input / Textarea / Card
  - KnowledgeBaseCard
  - DocumentList
  - ChatMessage
  - ChatInput
  - CitationCard
- 视觉方向不是通用后台模板，而是偏产品化的 AI 工作台风格。
- 安装前端依赖并修复首轮类型问题，当前 `next build` 已通过。
- 新增 `.env.example`，默认前端请求后端地址为 `http://localhost:8080`。

## 12. 接口联调支持

- 生成根目录 `apifox-collection.json`，可直接导入 Apifox。
- 已覆盖认证、知识库、文档、会话相关接口。
- 修复缺失请求体时返回 500 的问题，统一改为 400 JSON 错误响应。

## 13. 当前状态

当前后端已经具备：

- 用户注册登录
- JWT 鉴权
- 知识库管理
- 文档上传与异步解析切分
- 会话与消息持久化
- 基于文档分片的临时检索回复
- 结构化引用和检索日志落库
- DashScope 初版代码接入骨架
- DashScope Embedding + pgvector 初版接入
- 前端基础工作台骨架

当前尚未完成：

- 生产级 embedding 管理与重建
- 更精确的向量检索排序与召回优化
- 真正跑通验证过的真实 AI 回答生成
- 引用回溯
- 前端与所有后端接口的完整联调
- 更成熟的前端状态与错误处理

## 14. 下一步计划

- 实现最小 RAG 闭环：
  - 验证文档上传后 embedding 是否成功写入
  - 验证向量检索是否优先工作
  - 打磨真实模型回答质量与引用格式
- 后续再补日志页、评测页和前端联调细节。

## 15. 当前阶段状态（2026-05-10）

### 已完成

- 后端 Spring Boot 工程已搭建完成，可正常编译和启动。
- 已完成认证、知识库、文档、会话、消息的基础接口。
- 已接入 JWT 鉴权、统一错误返回、基础 CORS 配置。
- 已完成 Flyway 迁移：
  - `users`
  - `knowledge_bases`
  - `documents`
  - `chat_sessions`
  - `messages`
  - `document_chunks`
  - `retrieval_logs`
- 已完成文档上传后的异步处理链路：
  - 文件保存
  - 文档解析
  - chunk 切分
  - embedding 生成
  - 状态流转
- 已接入 `Spring AI Alibaba + DashScope` 初版代码。
- 已接入 `EmbeddingModel + pgvector` 初版代码，检索支持向量优先、文本回退。
- 已完成前端 `web/` 骨架：
  - 登录页
  - 知识库列表页
  - 知识库详情页
  - 聊天页
  - 日志页占位
  - 评测页占位
- 前端依赖已安装，`next build` 已通过。

### 当前可验证能力

- 注册 / 登录
- 创建知识库 / 查询知识库
- 前端页面可启动
- 文档上传接口已具备真正异步处理结构
- 聊天接口已具备：
  - 用户消息落库
  - assistant 消息落库
  - 检索片段返回
  - citationsJson 保存
  - retrieval_logs 保存

### 当前未完全验证

- 文档上传后从 `UPLOADED` 到 `READY` 的完整稳定性
- DashScope ChatModel 实际回答质量
- DashScope Embedding 是否稳定写入 `document_chunks.embedding`
- pgvector 检索是否稳定优先命中
- 前端与全部后端接口的完整联调

### 当前主要风险点

- 文档上传链路虽然已改成真正异步，但仍需要继续观察上传稳定性和状态流转。
- embedding / 向量检索虽然代码已接入，但还缺少实际数据验证。
- 前端目前是高质量骨架，不是最终联调完成版，错误态和细节还需要继续打磨。

### 明天建议优先做

1. 重启后端后重新验证文档上传，确认状态能走到 `READY`。
2. 检查 `document_chunks` 是否成功写入内容和 `embedding`。
3. 验证聊天接口是否真正使用了向量检索和 DashScope 回答。
4. 修前后端联调中暴露出来的真实问题。
5. 再考虑补日志页和评测页接口。

## 16. 联调修复记录（2026-05-11）

### 16.1 后端日志系统集成

- 新增 `RequestTraceFilter`，记录请求方法、路径、Query 参数、Header、响应状态、响应体、耗时与 `traceId`。
- 新增 `LoggingAspect`，覆盖 `controller` 与 `service` 方法调用日志，输出入参、返回值和执行耗时。
- 增强 `GlobalExceptionHandler`：
  - 区分业务异常与系统异常
  - 记录请求路径、方法、参数与完整堆栈
- 新增 `MybatisSqlLoggingInterceptor`，输出 SQL、参数与执行耗时。
- 新增 `logback-spring.xml`，优化本地开发彩色日志格式。
- 更新 `application-dev.yml` 日志级别，提升联调时对 `controller/service/mapper/logging` 的可见性。
- 补充 `spring-boot-starter-aop` 依赖，日志系统已通过 `mvn -q -DskipTests compile` 验证。

### 16.2 上传文件大小限制修复

- 联调时发现上传文档接口 `POST /api/knowledge-bases/{id}/documents` 在 4MB 级文件下失败。
- 通过请求日志与异常日志确认根因是 Spring Multipart 默认 `1MB` 限制，异常为 `MaxUploadSizeExceededException`。
- 在 `application.yml` 中显式增加：
  - `spring.servlet.multipart.max-file-size=20MB`
  - `spring.servlet.multipart.max-request-size=25MB`
- 在 `GlobalExceptionHandler` 中新增超大文件异常处理，统一返回明确错误响应，而不是默认 500。

### 16.3 DashScope Embedding 批量上限修复

- 文档上传进入异步处理后，日志暴露 DashScope Embedding 接口存在单次输入文本数上限问题。
- 现已调整 `EmbeddingService`：
  - 增加 `app.ai.embedding-batch-size` 配置项
  - 批量 embedding 改为按固定大小分批调用
  - 某一批 embedding 失败时仅该批降级为空向量，不直接让整篇文档处理链路崩溃
- 该修复目标是先保证文档处理链路可继续执行，再进一步验证 `document_chunks.embedding` 写入完整性。

### 16.4 DashScope 批次上限二次校正

- 实际联调再次返回 `HTTP 400 InvalidParameter`，错误信息明确指出 `input.contents` 批次大小不能超过 `10`。
- 已将 embedding 默认批大小从 `25` 下调到 `10`。
- 已在 `EmbeddingService` 中增加硬性上限兜底：即使外部配置超过 `10`，运行时也会自动截断到 `10`，避免再次因配置过大导致整批调用失败。

### 16.5 pgvector 入库类型修复

- 文档处理继续向前推进后，`document_chunks` 插入阶段报错：PostgreSQL `vector` 列不能直接接收 `varchar` 参数。
- 原因是 MyBatis-Plus 默认 `insert` 语句没有为 `embedding` 字段附加 `CAST(... AS vector)`。
- 已在 `DocumentChunkMapper` 中新增显式插入方法 `insertWithEmbedding`，对 `embedding` 字段使用 `CAST(#{embedding} AS vector)`。
- 已在 `DocumentProcessingService` 中区分：
  - 有 embedding 时走 `insertWithEmbedding`
  - 无 embedding 时继续走默认 `insert`
- 该修复用于保证 pgvector 列可以正常写入，同时保留 embedding 失败时的无向量降级路径。

### 16.6 embedding 维度与表结构修复

- 向量入库继续联调后，数据库报错 `expected 1536 dimensions, not 1024`。
- 已确认当前 DashScope 返回的 embedding 维度是 `1024`，而现有 `document_chunks.embedding` 列在早期迁移中被定义为 `vector(1536)`。
- 已新增 Flyway 迁移 `V6__fix_embedding_dimension_to_1024.sql`：
  - 先删除旧的 ivfflat 索引
  - 再把 `embedding` 列类型调整为 `vector(1024)`
  - 最后重建向量索引
- 该修复用于保证运行时 embedding 结果与数据库表结构一致，避免后续所有向量写入都失败。

### 16.7 聊天模型降级与 UTF-8 编码修复

- 聊天接口联调时，DashScope Chat 调用返回 `HTTP 400 InvalidParameter`，错误信息为 `url error, please check url`。
- 结合当前默认模型配置，先将默认聊天模型从 `qwen3.5-plus` 调整为更稳妥的 `qwen-plus`，降低默认配置导致调用失败的概率。
- 在 `LlmAnswerService` 中补充了明确的降级日志，便于区分“LLM 真实回答成功”与“已回退到检索摘要回答”。
- 另外，联调中发现日志输出的中文响应体出现乱码。
- 已增加服务端 `UTF-8` 强制编码配置，并修正日志工具对 JSON / 文本响应体的字符集解析逻辑，避免响应日志把 UTF-8 内容按错误字符集解码。

### 16.8 按官方文档切换聊天模型到 DeepSeek

- 参考 Spring AI Alibaba 官方 DeepSeek Chat 文档，将聊天模型从 DashScope 切换为 DeepSeek。
- 在 `pom.xml` 中新增官方依赖：`org.springframework.ai:spring-ai-starter-model-deepseek`。
- 在 `application.yml` 中按官方配置增加：
  - `spring.ai.model.chat=deepseek`
  - `spring.ai.deepseek.api-key=${DEEPSEEK_API_KEY}`
  - `spring.ai.deepseek.chat.options.model=${DEEPSEEK_CHAT_MODEL:deepseek-chat}`
- 保留 DashScope 的 `api-key` 配置用于 embedding，避免影响当前向量化链路。
- 同时将 fallback 文案从“模型服务未启用”调整为“模型回答不可用”，使前端提示与真实降级原因一致。

### 16.9 多 ChatModel Bean 兼容修复

- 切换到 DeepSeek 后，项目同时保留了 DashScope starter 与 DeepSeek starter，Spring 容器中可能同时存在多个 `ChatModel` Bean。
- 原先 `LlmAnswerService` 直接使用 `chatModelProvider.getIfAvailable()`，在多实现并存时可能抛出 `NoUniqueBeanDefinitionException`，导致聊天接口直接返回 500，连 fallback 都无法执行。
- 已调整 `LlmAnswerService` 的模型解析逻辑：
  - 先遍历所有 `ChatModel` 候选
- 优先选择类名中包含 `deepseek` 的实现
- 若未找到，再回退到第一个候选
- 该修复用于确保在 DashScope embedding 保留、DeepSeek chat 接入的混合场景下，聊天接口仍能稳定走到正确模型或降级路径。

### 16.10 检索片段截断与前端上传状态修复

- 联调中发现：数据库中的 chunk 已包含“Spring 的组成”，但聊天回答仍提示“未找到相关描述”。
- 已定位到 `RetrievalService` 在返回检索片段时将 chunk 内容截断为 `180` 个字符，导致真正答案可能位于被截断部分，模型无法看到完整上下文。
- 已将检索片段的截断上限提升到 `1200` 字符，减少关键信息在送给模型前被裁掉的问题。
- 前端知识库详情页上传文档后原先只做一次 `invalidate`，没有轮询文档状态，因此页面会一直停留在上传态，刷新后才看到 `READY`。
- 已在前端上传页增加：
  - 上传成功后将新文档立即插入列表
  - 当文档状态为 `UPLOADED / PARSING / INDEXING` 时每 2 秒自动轮询文档列表
  - 页面文案显示“正在处理...”
- 同时移除了上传请求中手动设置的 `multipart/form-data` 头，交给浏览器自动生成 boundary，避免潜在的 multipart 请求格式问题。

### 16.11 聊天布局与删除功能补充

- 根据前端参考图，聊天页原布局中引用侧栏占比过大、主消息区展示宽度不足，影响回答阅读体验。
- 已调整聊天页栅格比例，放大主回复区域，收窄右侧引用区，并为引用区增加独立滚动，避免长引用挤压主对话。
- 已同步增大消息卡片可用宽度和文本显示空间，减少 assistant 回复被挤成窄列的问题。
- 后端新增删除能力：
  - `DELETE /api/knowledge-bases/{id}` 删除知识库
  - `DELETE /api/documents/{id}` 删除文档
- 删除知识库时会同时清理其下已上传文件；删除文档时会同步删除落盘文件。
- 前端新增删除入口：
  - 知识库总览页可删除知识库
  - 知识库详情页可删除单个文档

### 16.12 前端样式丢失排查记录

- 联调过程中出现过一次“前端样式全部丢失”的现象。
- 检查结果显示：
  - `app/layout.tsx` 仍正常引入 `globals.css`
  - `globals.css` 中的 Tailwind 指令未丢失
  - `tailwind.config.ts` 与 `postcss.config.js` 配置正常
  - `npm run build` 可正常通过
- 由此判断问题不在代码本身，更可能是 Next.js 开发环境的 `.next` 缓存或热更新状态异常。
- 最终通过清理前端 `.next` 目录并重启 `next dev` 恢复样式显示。
- 该问题已确认属于开发态缓存问题，不是本次样式代码修改导致的持续性缺陷。

## 17. SPEC 提炼（2026-05-13）

- 基于 `DESIGN.md` 创建 `SPEC.md`（804 行），按六核心区域重新组织：
  - Objective（目标/用户/阶段/非目标）
  - Commands（完整 API 设计 + 错误码）
  - Project Structure（前后端目录树 + 职责说明）
  - Code Style（Java/TypeScript/SQL 规范）
  - Testing Strategy（测试金字塔 + 评测数据集）
  - Boundaries（always/ask/never + 技术约束 + 风险 + 环境变量）
- 额外章节：Data Model、RAG Pipeline、Current Status、Development Commands
- `SPEC.md` 为项目单一事实来源，`DESIGN.md` 保留作为面试准备和详细设计参考。

## 18. Phase 1 — V0 Bug 修复（2026-05-13）

### 18.1 AuthService UserPrincipal 排查

- 探索代理报告 `AuthService.register()` 中 `UserPrincipal` 在 `userMapper.insert()` 之前构造，导致 `user.getId()` 为 null。
- 经核实代码，`userMapper.insert(user)` 在第 52 行，`new UserPrincipal(user.getId(), ...)` 在第 55 行，顺序正确。
- MyBatis-Plus `@TableId(type = IdType.AUTO)` 会在 insert 后通过 JDBC `getGeneratedKeys()` 将自增 ID 回填到 entity。
- **结论：误报，无需修复。**

### 18.2 ChatService topK 配置化

- 问题：`ChatService.createUserMessage()` 第 88 行硬编码 `retrieveTopChunks(sessionId, request.content(), 3)`，忽略了 `AiProperties.getRetrievalTopK()` 的配置值。
- 修复：在 `ChatService` 中注入 `AiProperties`，将硬编码 `3` 替换为 `aiProperties.getRetrievalTopK()`。
- 影响文件：`server/src/main/java/com/example/copilot/service/ChatService.java`

### 18.3 LLM Token 用量统计

- 问题：`ChatService` 第 96-97 行将 `promptTokens` 和 `completionTokens` 硬编码为 `0`，Spring AI `ChatResponse.getMetadata()` 中的真实 token 数被丢弃。
- 修复：
  - 新增 `LlmAnswerResult` record（content + promptTokens + completionTokens）
  - `LlmAnswerService.generateAnswer()` 返回类型从 `String` 改为 `LlmAnswerResult`
  - 从 `ChatResponse.getMetadata().getUsage()` 中提取 prompt/completion tokens
  - `ChatService` 使用 `llmResult.promptTokens()` / `llmResult.completionTokens()` 落库
- 影响文件：
  - `server/src/main/java/.../dto/response/LlmAnswerResult.java`（新建）
  - `server/src/main/java/.../service/LlmAnswerService.java`
  - `server/src/main/java/.../service/ChatService.java`

## 19. Phase 2 — V0 体验打磨（2026-05-13）

### 19.1 前端 Header 鉴权状态

- 问题：Header 始终展示静态导航链接，不反映登录状态。
- 修复：
  - `use-app-store.ts` 新增 `userId` 字段，`setAuth(token, userId, userName)`
  - `header.tsx` 读取 store 中的 `token` / `userName`：
    - 已登录：显示用户名 + 工作台链接 + 退出按钮
    - 未登录：显示"进入工作台" + "开始使用"
  - 退出时调用 `clearAuth()` 并跳转 `/login`
- 影响文件：
  - `web/hooks/use-app-store.ts`
  - `web/components/layout/header.tsx`
  - `web/app/login/page.tsx`（`setAuth` 调用增加 userId 参数）

### 19.2 前端 401 拦截器

- 问题：后端返回 401 时前端不做处理，用户体验差。
- 修复：在 `lib/api/client.ts` 中增加 Axios response interceptor：
  - `response.status === 401` → 清除 localStorage 中的 token/userId/name → 跳转 `/login`
- 影响文件：`web/lib/api/client.ts`

### 19.3 前端 Toast 通知系统

- 问题：上传/创建/删除等操作的失败没有用户可见的错误提示。
- 修复：
  - 新增 `components/ui/toast.tsx`：Toast 组件（支持 error/success 两种类型，4 秒自动消失，带动画）
  - `use-app-store.ts` 新增 `toasts`、`addToast(message, type)`、`removeToast(id)`
  - `app/layout.tsx` 加入全局 `ToastContainer`
  - 在 KB 列表页、KB 详情页、聊天页的 mutation catch 块中接入 toast
- 影响文件：
  - `web/components/ui/toast.tsx`（新建）
  - `web/hooks/use-app-store.ts`
  - `web/app/layout.tsx`
  - `web/app/knowledge-bases/page.tsx`
  - `web/app/knowledge-bases/[id]/page.tsx`
  - `web/app/knowledge-bases/[id]/chat/page.tsx`

### 19.4 聊天消息元数据展示

- 问题：`MessageItem` 的 `latencyMs`、`promptTokens`、`completionTokens` 字段从未渲染。
- 修复：在 `chat-message.tsx` 中，assistant 消息底部增加元数据行：
  - 延迟：`X.Xs`
  - 输入 tokens：`↑N tokens`
  - 输出 tokens：`↓N tokens`
  - 仅当值 > 0 时显示
- 影响文件：`web/components/chat/chat-message.tsx`

### 19.5 引用 JSON 序列化改用 Jackson

- 问题：`ChatService.buildCitationsJson()` 和 `buildRetrievedChunksJson()` 使用手动字符串拼接构建 JSON，不处理特殊字符转义，脆弱且不可维护。
- 修复：
  - 在 `ChatService` 中注入 Spring 的 `ObjectMapper`
  - 两个方法改为 `objectMapper.writeValueAsString(retrievedChunks)`，异常时返回 `"[]"`
  - 同时将 `generateAssistantReply()` 中已废弃的 `StringJoiner` 替换为 `StringBuilder`
- 影响文件：`server/src/main/java/.../service/ChatService.java`

### 19.6 编译验证

- 后端 `mvn -q -DskipTests compile` 通过（无错误）
- 前端 `npx tsc --noEmit` 通过（0 类型错误）
- 前端 `npm run build` 通过（所有页面成功构建）

## 20. 当前状态（2026-05-13）

### 已完成（V0 + 打磨）

- 用户注册登录 + JWT 鉴权
- 知识库 CRUD + 前端管理页面
- 文档上传 + 异步解析/切分/embedding/pgvector 入库 + 状态流转
- 会话与消息持久化 + 聊天界面
- RAG 流水线：向量检索优先 + 文本回退 + DeepSeek LLM 生成
- 结构化引用（Jackson 序列化）+ 检索日志落库
- 前端完整骨架：登录、KB 列表/详情、聊天、日志占位、评测占位
- 日志基础设施（RequestTraceFilter、LoggingAspect、SQL 日志）
- **Phase 1 Bug 修复**：topK 配置化、token 用量真实落库
- **Phase 2 体验打磨**：Header 鉴权状态、401 拦截器、Toast 通知、消息元数据、Jackson 序列化

### 待完成（Phase 3-10，按计划推进）

- Phase 3：V1 数据库迁移（answer_feedback / eval_runs / eval_cases 表 + 实体 + Mapper）
- Phase 4：反馈模块（FeedbackService + Controller + 前端点赞/点踩 UI）
- Phase 5：管理员日志模块（LogService + LogController + 前端日志页）
- Phase 6：评测模块（EvaluationService + EvalRunner + 前端评测工作台）
- Phase 7：V1 收尾（文档 reindex、auth refresh/me、类型补全）
- Phase 8：RAG 检索质量提升（中文分词、chunk overlap、Markdown 清洗、错误日志）
- Phase 9：测试补齐（单元测试 + 集成测试 + 评测数据集）
- Phase 10：V2 流式输出（SSE 端点 + 前端 token-by-token 渲染）

### 编译状态

- 后端：`mvn -q -DskipTests compile` ✅
- 前端 TypeScript：`npx tsc --noEmit` ✅
- 前端构建：`npm run build` ✅
