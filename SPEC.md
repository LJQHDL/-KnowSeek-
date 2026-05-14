# SPEC: Enterprise Knowledge Copilot

## 1. Objective

### 1.1 What

An enterprise knowledge-base Q&A system powered by RAG (Retrieval-Augmented Generation). Users upload documents (PDF, DOCX, Markdown), the system parses/chunks/embeds them into a pgvector-backed index, and answers natural-language questions with cited, traceable responses drawn from the uploaded content.

### 1.2 Who

| Role | Permissions |
|------|-------------|
| Normal user | Upload documents, ask questions against knowledge bases, view session history, submit feedback |
| Admin | All user permissions + view Q&A logs, view retrieval logs, run evaluations, analyze system performance |

### 1.3 Why (Interview Talking Points)

- Demonstrates Java backend engineering with Spring Boot 3 + Java 21
- Demonstrates AI application development with Spring AI Alibaba + DeepSeek
- Demonstrates RAG system design: parsing → chunking → embedding → retrieval → generation → citation
- Demonstrates full-stack capability with Next.js frontend
- Breaks the "AI = Python only" stereotype

### 1.4 Performance Targets (MVP, Single Instance)

| Metric | Target |
|--------|--------|
| Q&A response time | P95 < 8s |
| Document processing throughput | 3-5 docs/min |
| Concurrent users | 10-20 |
| Knowledge base capacity | 100-1000 documents |

### 1.5 Delivery Phases

**V0 — Demo Core (Week 1-3, MUST complete)**
Auth → Knowledge bases → Document upload/processing → Q&A with citations → Multi-turn sessions. This is the minimal demo loop.

**V1 — Full MVP (Week 4-6, best effort)**
Document delete/re-index, DOCX/Markdown support, session management, feedback (up/down), Q&A logs, retrieval logs, evaluation, file type validation, rate limiting.

**V2 — Bonus (time permitting)**
Streaming output (SSE), hybrid search (BM25 + vector), rerank, Docker Compose one-click deploy, multi-KB joint search.

### 1.6 Explicit Non-Goals

- Social features (comments, sharing, collaboration)
- RBAC / complex permission models
- Real-time notifications (WebSocket)
- Mobile adaptation
- i18n
- Collaborative editing

---

## 2. Commands (API Design)

### 2.1 Conventions

- Base path: `/api/`
- Success response:
  ```json
  { "code": 0, "message": "success", "data": { ... } }
  ```
- Paginated response:
  ```json
  { "code": 0, "message": "success", "data": { "items": [...], "total": 100, "page": 1, "pageSize": 20 } }
  ```
- Error response:
  ```json
  { "code": 40001, "message": "description", "data": { "errors": [...] } }
  ```
- Authentication: JWT Bearer token in `Authorization` header
- Refresh token: HttpOnly cookie

### 2.2 Error Codes

| Range | Category | Examples |
|-------|----------|----------|
| 0 | Success | 0 |
| 400xx | Parameter error | 40001 validation failed, 40002 unsupported file type |
| 401xx | Auth error | 40101 not logged in, 40102 token expired |
| 403xx | Forbidden | 40301 no permission |
| 404xx | Not found | 40401 knowledge base not found |
| 409xx | Conflict | 40901 email already registered |
| 500xx | Server error | 50001 internal error, 50002 LLM unavailable |

### 2.3 Auth

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/auth/register | No | Register with email + password + name |
| POST | /api/auth/login | No | Login, returns access token + refresh token |
| POST | /api/auth/refresh | No | Refresh access token |
| GET | /api/auth/me | Yes | Get current user info |

### 2.4 Knowledge Bases

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /api/knowledge-bases | Yes | List user's KBs (paginated, filterable by keyword) |
| POST | /api/knowledge-bases | Yes | Create KB (name + description) |
| GET | /api/knowledge-bases/{id} | Yes | Get KB detail with document list |
| DELETE | /api/knowledge-bases/{id} | Yes | Delete KB and all associated data |

### 2.5 Documents

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/knowledge-bases/{id}/documents | Yes | Upload document (multipart, max 20MB) |
| GET | /api/knowledge-bases/{id}/documents | Yes | List documents (paginated, filterable by status/keyword) |
| DELETE | /api/documents/{id} | Yes | Delete document and its chunks |
| POST | /api/documents/{id}/reindex | Yes | Re-process document (V1) |

**Supported formats:** PDF, DOCX, Markdown  
**Size limit:** 20MB  
**Validation:** Magic number check (not file extension)

### 2.6 Chat

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/chat/sessions | Yes | Create chat session (kbId + optional title) |
| GET | /api/chat/sessions | Yes | List user's sessions (paginated, filterable by kbId) |
| GET | /api/chat/sessions/{id}/messages | Yes | Get message history for a session |
| POST | /api/chat/sessions/{id}/messages | Yes | Send a message and get AI reply |

**Message request:**
```json
{ "content": "What is RAG?", "stream": false }
```

**Message response (assistant):**
```json
{
  "id": 2,
  "role": "assistant",
  "content": "RAG is...",
  "citations": [{ "documentId": 1, "documentName": "manual.pdf", "chunkId": 45, "snippet": "...", "pageNumber": 12, "score": 0.92 }],
  "latencyMs": 2500,
  "promptTokens": 512,
  "completionTokens": 256,
  "createdAt": "2025-05-10T10:00:05Z"
}
```

### 2.7 Feedback (V1)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/messages/{id}/feedback | Yes | Submit feedback (rating: up/down, optional comment) |

### 2.8 Logs (V1, Admin)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /api/logs/messages | Admin | Query Q&A logs (paginated, filterable by KB/date range) |
| GET | /api/logs/retrievals | Admin | Query retrieval logs (paginated) |

### 2.9 Evaluations (V1)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/evals/run | Admin | Trigger evaluation run with test cases |
| GET | /api/evals | Admin | List evaluation runs (paginated) |
| GET | /api/evals/{id} | Admin | Get evaluation detail with per-case results |

### 2.10 Streaming (V2)

SSE endpoint extension to `POST /api/chat/sessions/{id}/messages` with `stream: true`:
```
event: message
data: {"type": "content", "content": "RAG"}

event: message
data: {"type": "citation", "citation": {...}}

event: message
data: {"type": "done", "latencyMs": 2500}
```

---

## 3. Project Structure

### 3.1 Repository Layout

```
personalworks/
├── SPEC.md                     # This file — single source of truth
├── DESIGN.md                   # Detailed design doc with interview prep
├── DEV_PROGRESS.md             # Development log
├── AGENTS.md                   # Agent guidelines
├── apifox-collection.json      # API collection for Apifox import
├── server/                     # Java 21 + Spring Boot backend
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/java/com/example/copilot/
│       │   ├── CopilotApplication.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   ├── KnowledgeBaseController.java
│       │   │   ├── DocumentController.java
│       │   │   └── ChatController.java
│       │   ├── service/
│       │   │   ├── AuthService.java
│       │   │   ├── KnowledgeBaseService.java
│       │   │   ├── DocumentService.java
│       │   │   ├── DocumentProcessingService.java
│       │   │   ├── ChatService.java
│       │   │   ├── EmbeddingService.java
│       │   │   └── LlmAnswerService.java
│       │   ├── mapper/
│       │   │   ├── UserMapper.java
│       │   │   ├── KnowledgeBaseMapper.java
│       │   │   ├── DocumentMapper.java
│       │   │   ├── DocumentChunkMapper.java
│       │   │   ├── ChatSessionMapper.java
│       │   │   ├── MessageMapper.java
│       │   │   └── RetrievalLogMapper.java
│       │   ├── entity/
│       │   │   ├── User.java
│       │   │   ├── KnowledgeBase.java
│       │   │   ├── Document.java
│       │   │   ├── DocumentChunk.java
│       │   │   ├── ChatSession.java
│       │   │   ├── Message.java
│       │   │   └── RetrievalLog.java
│       │   ├── dto/
│       │   │   ├── request/
│       │   │   │   ├── LoginRequest.java
│       │   │   │   ├── RegisterRequest.java
│       │   │   │   ├── CreateKnowledgeBaseRequest.java
│       │   │   │   ├── CreateChatSessionRequest.java
│       │   │   │   └── CreateMessageRequest.java
│       │   │   └── response/
│       │   │       ├── ApiResponse.java (in common/)
│       │   │       ├── AuthResponse.java
│       │   │       ├── KnowledgeBaseResponse.java
│       │   │       ├── DocumentResponse.java
│       │   │       ├── ChatSessionResponse.java
│       │   │       ├── ChatReplyResponse.java
│       │   │       ├── MessageResponse.java
│       │   │       ├── RetrievalResult.java
│       │   │       └── RetrievedChunkResponse.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   ├── AsyncConfig.java
│       │   │   ├── AiProperties.java
│       │   │   ├── MybatisPlusConfig.java
│       │   │   └── OpenApiConfig.java
│       │   ├── security/
│       │   │   ├── JwtTokenProvider.java
│       │   │   ├── JwtAuthenticationFilter.java
│       │   │   ├── CustomUserDetailsService.java
│       │   │   ├── UserPrincipal.java
│       │   │   ├── RestAuthenticationEntryPoint.java
│       │   │   └── RestAccessDeniedHandler.java
│       │   ├── rag/
│       │   │   ├── ChunkingService.java
│       │   │   ├── RetrievalService.java
│       │   │   └── parser/
│       │   │       ├── DocumentParser.java (interface)
│       │   │       ├── DocumentParserFactory.java
│       │   │       ├── PdfDocumentParser.java
│       │   │       ├── DocxDocumentParser.java
│       │   │       ├── MarkdownDocumentParser.java
│       │   │       └── ParsedDocument.java
│       │   ├── exception/
│       │   │   ├── GlobalExceptionHandler.java
│       │   │   ├── BusinessException.java
│       │   │   ├── NotFoundException.java
│       │   │   └── ForbiddenException.java
│       │   ├── logging/
│       │   │   ├── RequestTraceFilter.java
│       │   │   ├── LoggingAspect.java
│       │   │   ├── MybatisSqlLoggingInterceptor.java
│       │   │   └── LoggingUtils.java
│       │   └── common/
│       │       ├── ApiResponse.java
│       │       └── ErrorCode.java
│       ├── main/resources/
│       │   ├── application.yml
│       │   ├── application-dev.yml
│       │   ├── logback-spring.xml
│       │   └── db/migration/
│       │       ├── V1__init_v0_tables.sql
│       │       ├── V2__add_documents_and_chat_tables.sql
│       │       ├── V3__add_document_chunks_table.sql
│       │       ├── V4__add_retrieval_logs_table.sql
│       │       ├── V5__enable_pgvector_and_add_embeddings.sql
│       │       └── V6__fix_embedding_dimension_to_1024.sql
│       └── test/java/com/example/copilot/
│           ├── service/
│           ├── controller/
│           └── integration/
├── web/                         # Next.js 14 + TypeScript frontend
│   ├── package.json
│   ├── next.config.js
│   ├── tailwind.config.ts
│   ├── tsconfig.json
│   ├── .env.example
│   └── app/
│       ├── layout.tsx
│       ├── globals.css
│       ├── page.tsx             # Home / dashboard
│       ├── login/
│       │   └── page.tsx
│       ├── knowledge-bases/
│       │   ├── page.tsx         # KB list
│       │   └── [id]/
│       │       ├── page.tsx     # KB detail + documents
│       │       └── chat/
│       │           └── page.tsx # Chat interface
│       ├── logs/
│       │   └── page.tsx         # (placeholder)
│       └── evals/
│           └── page.tsx         # (placeholder)
│   ├── components/
│   │   ├── ui/                  # shadcn/ui primitives (button, input, textarea, card)
│   │   ├── chat/
│   │   │   ├── chat-message.tsx
│   │   │   ├── chat-input.tsx
│   │   │   └── citation-card.tsx
│   │   ├── knowledge-base/
│   │   │   ├── knowledge-base-card.tsx
│   │   │   └── document-list.tsx
│   │   ├── layout/
│   │   │   └── header.tsx
│   │   └── providers.tsx
│   ├── lib/
│   │   ├── api/
│   │   │   ├── client.ts        # Axios instance with JWT interceptor
│   │   │   ├── auth.ts
│   │   │   ├── knowledge-base.ts
│   │   │   └── chat.ts
│   │   ├── query-client.ts
│   │   └── utils.ts
│   ├── hooks/
│   │   └── use-app-store.ts     # Zustand store
│   └── types/
│       └── api.ts
└── image/                       # Design mockups / screenshots
```

### 3.2 Backend Package Conventions

| Package | Responsibility |
|---------|---------------|
| `controller/` | HTTP endpoint handlers, request validation, response formatting |
| `service/` | Business logic, transaction orchestration |
| `mapper/` | MyBatis-Plus data access (extend `BaseMapper<T>`) |
| `entity/` | Persistence entities mapped to tables |
| `dto/request/` | Incoming request bodies |
| `dto/response/` | Outgoing response structures |
| `config/` | Spring @Configuration classes |
| `security/` | JWT, filters, auth entry/deny handlers |
| `rag/` | Chunking, retrieval, document parsing |
| `rag/parser/` | Document format parsers (strategy pattern) |
| `exception/` | Custom exceptions + global handler |
| `logging/` | Request tracing, SQL logging, AOP method logging |
| `common/` | Shared constants, error codes, ApiResponse wrapper |

### 3.3 Frontend Conventions

| Directory | Responsibility |
|-----------|---------------|
| `app/` | Next.js App Router pages — file-system routing |
| `components/ui/` | shadcn/ui primitives (Button, Input, Textarea, Card) |
| `components/chat/` | Chat-specific components |
| `components/knowledge-base/` | KB-specific components |
| `components/layout/` | Header, sidebar, shared layout |
| `lib/api/` | API client functions — one file per resource |
| `hooks/` | Custom React hooks, Zustand store |
| `types/` | Shared TypeScript type definitions |

---

## 4. Code Style

### 4.1 General

- **Java:** 4-space indentation, PascalCase classes, camelCase methods/fields
- **TypeScript/JSON/YAML:** 2-space indentation
- **SQL:** UPPERCASE keywords, lowercase identifiers
- **Flyway migrations:** `V{seq}__{description}.sql` (double underscore after version number)

### 4.2 Backend (Java)

**Architecture: Controller → Service → Mapper pattern.** No business logic in controllers. Services own transactions. Mappers are data access only.

**Constructor injection** over field injection:
```java
@RequiredArgsConstructor
public class ChatService {
    private final RetrievalService retrievalService;
    private final LlmAnswerService llmAnswerService;
}
```

**Lombok sparingly:** Use `@Data` for entities/DTOs, `@RequiredArgsConstructor` for DI, but avoid `@Builder` unless truly needed (verbose, hides required fields).

**Exception handling:**
- Throw typed exceptions (`NotFoundException`, `ForbiddenException`, `BusinessException`)
- Never return null from a controller — throw instead
- `GlobalExceptionHandler` catches everything and maps to uniform JSON error responses

**Async processing:**
- `@Async("documentProcessingExecutor")` for document pipeline
- Custom `ThreadPoolTaskExecutor` configured in `AsyncConfig`
- Async methods throw exceptions normally — caller handles via status polling

**Model resolution (multi-ChatModel):**
- When both DashScope and DeepSeek starters are present, resolve the correct `ChatModel` bean by class name
- Fall back to retrieval-summary if no model is available (graceful degradation, never 500)

**Embedding batching:**
- DashScope enforces max 10 inputs per batch — hard cap in `EmbeddingService`
- Batch failures affect only that batch (empty vectors), not the whole document

### 4.3 Frontend (TypeScript + React)

**Components:** Functional components only. PascalCase filenames. Default export for page components, named exports for everything else.

**State management:**
- Server state → React Query (`@tanstack/react-query`)
- Client state → Zustand (`useAppStore`)

**API client:**
- Centralized Axios instance in `lib/api/client.ts` with JWT interceptor
- Resource-specific functions in `lib/api/auth.ts`, `knowledge-base.ts`, `chat.ts`

**Styling:** Tailwind CSS utility classes. No CSS modules. No inline styles.

**Type safety:**
- TypeScript strict mode
- API response types defined in `types/api.ts`
- Never use `any` — use `unknown` and narrow

### 4.4 Database

**PostgreSQL 15+ with pgvector extension.** All tables use `BIGSERIAL` primary keys. Soft deletes via `deleted_at` column where needed.

**Vector column:** `embedding vector(1024)` (DashScope text-embedding-v2 dimension).

**Naming:**
- Tables: `snake_case` pluralized (`knowledge_bases`, `chat_sessions`)
- Columns: `snake_case` (`owner_id`, `created_at`)
- Indexes: `idx_{table}_{column}` or `idx_{table}_{purpose}`

**Data isolation:** Every query against user-owned resources must include `owner_id = :currentUserId`. Enforced at service layer, not just controller.

---

## 5. Testing Strategy

### 5.1 Pyramid

```
        ┌─────────┐
        │   E2E   │  10%
        ├─────────┤
        │Integrat.│  20%
        ├─────────┤
        │  Unit   │  70%
        └─────────┘
```

### 5.2 Unit Tests (JUnit 5 + Mockito)

- **Location:** `server/src/test/java/com/example/copilot/`
- **Naming:** `{ClassUnderTest}Test.java`
- **Mock all external boundaries:** mappers, external APIs (LLM, embedding), file system
- **Do NOT mock:** simple value objects, DTOs, entities

**Priority areas for coverage:**
1. Auth: token generation, validation, password hashing
2. Document pipeline: status transitions, parser selection, chunking logic
3. Retrieval: vector search SQL assembly, fallback to text search
4. Chat: message ordering, session ownership
5. Evaluation: metric calculation (hit rate, MRR)

**Example:**
```java
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {
    @Mock private RetrievalService retrievalService;
    @Mock private LlmAnswerService llmAnswerService;
    @InjectMocks private ChatService chatService;

    @Test
    void shouldReturnAnswerWithCitations() {
        when(retrievalService.retrieve(any(), anyInt())).thenReturn(chunks);
        when(llmAnswerService.generate(any())).thenReturn(answer);

        var response = chatService.chat(sessionId, "What is RAG?");

        assertThat(response.getCitations()).isNotEmpty();
    }
}
```

### 5.3 Integration Tests (Testcontainers + Spring Boot)

- **Use `pgvector/pgvector:pg15` Testcontainer** for database tests
- Test the full document pipeline: upload → parse → chunk → embed → index → READY
- Test retrieval quality against real pgvector
- Test auth flows end-to-end (register → login → use token)

### 5.4 Evaluation Dataset

JSON-based test cases for measuring RAG quality:
```json
{
  "cases": [{
    "id": 1,
    "question": "What is RAG?",
    "expectedSources": [{ "documentName": "manual.pdf", "pageNumber": 12 }],
    "expectedAnswer": "RAG is Retrieval-Augmented Generation..."
  }]
}
```

Metrics tracked: Hit Rate, MRR, Answer Score, Avg Latency.

### 5.5 What NOT to Test

- Framework wiring (Spring context loading, Next.js routing)
- Generated code (getters/setters, Lombok)
- External services (LLM API, Embedding API) — mock these
- UI pixel perfection

---

## 6. Boundaries

### 6.1 What to Always Do

- **Auth first:** Every protected endpoint requires a valid JWT
- **Data isolation:** All queries for user-owned resources filter by current user ID
- **Graceful degradation:** LLM unavailable → return retrieval summary, never 500
- **File validation:** Magic number check, not extension-based; 20MB max
- **Status tracking:** Every document has explicit status (UPLOADED → PARSING → INDEXING → READY/FAILED)
- **Citations always:** Every assistant response includes structured citation data (documentId, chunkId, snippet, score)
- **UTF-8 everywhere:** Server responses, log output, file I/O all enforce UTF-8
- **Flyway for all schema changes:** Never modify tables manually
- **Secrets in env vars:** API keys, JWT secret, DB password — never in files

### 6.2 What to Ask First About

- Adding new dependencies (weigh maintenance burden vs. benefit)
- Changing the LLM provider or embedding model
- Schema changes that affect existing migrations (add a new migration, don't edit old ones)
- Switching from pgvector to an external vector database
- Adding a new document format parser (follow the existing strategy pattern)
- Any change that touches the auth/JWT flow
- Deployment to a public URL (needs CORS review, HTTPS, rate limiting)

### 6.3 What to Never Do

- **Commit secrets:** API keys, passwords, tokens in code or config files
- **Skip data isolation:** Query that returns another user's data
- **Return raw exceptions to the client:** Always go through GlobalExceptionHandler
- **Block the upload thread:** Document processing must be async
- **Edit old Flyway migrations:** Always add a new one
- **Use `any` in TypeScript:** Use `unknown` and narrow
- **Add features outside the current phase scope:** V0 features only until V0 is complete
- **Roll your own auth:** Use Spring Security + JWT, not custom session management
- **Call LLM without retrieval context:** Every answer must be grounded in retrieved chunks
- **Store plaintext passwords:** BCrypt only

### 6.4 Technology Constraints

| Constraint | Reason |
|------------|--------|
| Java 21 (not 17, not 22) | LTS, virtual threads for LLM I/O |
| Spring Boot 3.2+ | Required by Spring AI Alibaba |
| PostgreSQL + pgvector (not MySQL + external vector DB) | Zero additional services for vector search in MVP |
| MyBatis-Plus (not JPA) | Chinese market standard, complex query ergonomics |
| DeepSeek for chat (not DashScope qwen) | DashScope chat had URL error in testing; DeepSeek works reliably |
| DashScope for embedding (not OpenAI) | Chinese-optimized (text-embedding-v2), student free tier |
| Next.js 14 App Router (not Pages Router) | React-recommended pattern; SSR support |
| shadcn/ui + Tailwind (not Ant Design) | Modern AI-product aesthetic; code lives in project |
| Knife4j (not plain Springdoc) | Chinese market standard API docs |

### 6.5 Known Risks

| Risk | Mitigation |
|------|------------|
| DashScope embedding batch limit (max 10) | Hard-cap in EmbeddingService; batch failures don't crash pipeline |
| pgvector IVFFlat degrades at 1M+ chunks | Acceptable for MVP (100-1000 docs); migration path to HNSW or Milvus documented |
| DeepSeek API rate limits or downtime | Graceful degradation to retrieval summary; circuit breaker in plan |
| PDF parsing quality varies by document | Accept for V0; DOCX/Markdown as alternatives |
| Single PostgreSQL instance is SPOF | Accept for MVP; Docker Compose with volume mounts for recovery |

### 6.6 Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `DB_HOST` | Yes | PostgreSQL host |
| `DB_PORT` | Yes | PostgreSQL port |
| `DB_NAME` | Yes | Database name |
| `DB_USER` | Yes | Database user |
| `DB_PASSWORD` | Yes | Database password |
| `JWT_SECRET` | Yes | JWT signing secret (min 256-bit) |
| `DEEPSEEK_API_KEY` | For chat | DeepSeek API key |
| `DEEPSEEK_CHAT_MODEL` | No | Model name (default: `deepseek-chat`) |
| `DASH_SCOPE_API_KEY` | For embedding | Alibaba Cloud DashScope API key |
| `FILE_STORAGE_PATH` | No | Upload directory (default: `./uploads`) |
| `NEXT_PUBLIC_API_URL` | No | Backend URL for frontend (default: `http://localhost:8080`) |

---

## 7. Data Model

### 7.1 Entity Relationship

```
users 1──N knowledge_bases 1──N documents 1──N document_chunks
  │                    │
  │                    └──────────N chat_sessions 1──N messages 1──N retrieval_logs
  │                                        │
  └────────────────────────────────────────┼──N answer_feedback
                                           │
                              eval_runs 1──N eval_cases
```

### 7.2 Core Tables

**users:** id, email (unique), password_hash (bcrypt), name, role (user/admin), created_at, updated_at, deleted_at

**knowledge_bases:** id, owner_id (FK→users), name, description, document_count, chunk_count, created_at, updated_at, deleted_at

**documents:** id, knowledge_base_id (FK), file_name, file_type (PDF/DOCX/MD), file_size, storage_path, status (UPLOADED/PARSING/INDEXING/READY/FAILED), error_message, chunk_count, created_at, updated_at, deleted_at

**document_chunks:** id, document_id (FK), knowledge_base_id (FK), chunk_index, content, token_count, embedding vector(1024), metadata (JSONB), created_at  
*Index: ivfflat on embedding with vector_cosine_ops*

**chat_sessions:** id, user_id (FK), knowledge_base_id (FK), title, message_count, created_at, updated_at

**messages:** id, session_id (FK), role (user/assistant), content, citations (JSONB), latency_ms, prompt_tokens, completion_tokens, model, created_at

**retrieval_logs:** id, message_id (FK), query_text, query_embedding vector(1024), retrieved_chunks (JSONB), top_k, latency_ms, created_at

**answer_feedback:** id, message_id (FK), user_id (FK), rating (up/down), comment, created_at  
*Unique constraint: (message_id, user_id)*

**eval_runs:** id, knowledge_base_id (FK), name, status (PENDING/RUNNING/COMPLETED/FAILED), total_cases, hit_count, hit_rate, mrr, avg_latency_ms, avg_score, started_at, finished_at, created_at

**eval_cases:** id, eval_run_id (FK), question, expected_answer, expected_sources (JSONB), actual_answer, actual_sources (JSONB), retrieval_hit, retrieval_rank, score, latency_ms, error_message

---

## 8. RAG Pipeline

### 8.1 Document Import Flow

```
User uploads file
  → Save to local storage (MinIO for production)
  → Create document record (status: UPLOADED)
  → [Async] Parse document (PDFBox/POI/raw read)
  → Clean and preprocess text
  → Chunk (500-800 token segments, 100-150 token overlap)
  → Batch embed via DashScope (max 10 per batch)
  → Insert chunks + embeddings into pgvector
  → Update document status: READY (or FAILED on error)
```

### 8.2 Q&A Flow

```
User sends message
  → Save user message
  → Generate query embedding (DashScope)
  → Vector search: cosine similarity via pgvector <=> operator, top-K = 5
  → Build prompt: system prompt + retrieved chunks as context + user question
  → Call DeepSeek Chat API
  → Parse response, assemble citations
  → Save assistant message + retrieval log
  → Return answer with citations
```

### 8.3 Chunking Strategy

| Parameter | Value | Rationale |
|-----------|-------|-----------|
| Chunk size | 500-800 tokens | Balance context completeness vs. retrieval precision |
| Overlap | 100-150 tokens | Prevent boundary information loss |
| Min chunk size | 100 tokens | Merge fragments that are too small |
| Strategy | Recursive character splitter | Paragraph → sentence → word → char priority |

### 8.4 Retrieval

**Primary:** Cosine similarity via pgvector `<=>` operator with ivfflat index  
**Fallback:** Text-based LIKE matching (when embedding is unavailable)  
**Snippet cap:** 1200 characters per chunk returned to LLM  
**Similarity threshold:** Consider filtering below 0.75 if answer quality degrades

### 8.5 Prompt Design

**System:** "You are a professional enterprise knowledge base assistant. Answer ONLY based on provided context. If context lacks information, say 'Unable to answer based on available documents.' Cite sources in format [Document Name, Page X]."

**User:** Context documents (formatted) + separator + user question + "Please answer based on the above context and cite your sources."

### 8.6 Citation Structure

```java
record Citation(
    Long documentId,
    String documentName,
    Long chunkId,
    String snippet,      // First 200 chars of source
    Integer pageNumber,   // If available
    Double score          // Similarity score
) {}
```

---

## 9. Current Implementation Status

### 9.1 Done (V0 Core)

- [x] User registration and login with JWT
- [x] Knowledge base CRUD
- [x] Document upload with async processing (PDF, DOCX, Markdown)
- [x] Text chunking and embedding generation (DashScope)
- [x] pgvector storage with ivfflat index
- [x] Vector-priority retrieval with text fallback
- [x] Chat session management and message persistence
- [x] LLM answer generation (DeepSeek) with graceful degradation
- [x] Citation assembly and retrieval log recording
- [x] Frontend skeleton: login, KB list/detail, chat interface
- [x] Structured logging (request trace, AOP, SQL)
- [x] File size limit handling (20MB)
- [x] Embedding batch size guard (max 10)
- [x] Embedding dimension fix (1024)
- [x] Multi-ChatModel bean resolution
- [x] Frontend upload status polling
- [x] KB and document deletion
- [x] Chat layout with citation sidebar
- [x] API collection export (Apifox)

### 9.2 In Progress / Needs Verification

- [ ] End-to-end document: upload → READY stability
- [ ] Vector retrieval accuracy with real data
- [ ] DeepSeek answer quality with actual documents
- [ ] Frontend-backend full integration polish
- [ ] Error state handling on all frontend pages

### 9.3 Not Yet Started (V1)

- [ ] Document re-index endpoint
- [ ] Session title auto-generation
- [ ] Feedback (up/down + comment)
- [ ] Admin log pages (Q&A logs, retrieval logs)
- [ ] Evaluation engine (run management, metrics)
- [ ] Rate limiting
- [ ] Magic number file validation

### 9.4 Not Yet Started (V2)

- [ ] Streaming output (SSE)
- [ ] Hybrid search (BM25 + vector)
- [ ] Rerank
- [ ] Docker Compose
- [ ] Multi-KB joint search
- [ ] Redis caching

---

## 10. Development Commands

### Backend
```bash
cd server
./mvnw spring-boot:run      # Start dev server
./mvnw test                  # Run tests
./mvnw clean package         # Build artifact
```

### Frontend
```bash
cd web
npm install                  # Install dependencies
npm run dev                  # Start dev server
npm run build                # Production build
```

### Database
```bash
# Flyway migrations run automatically on server startup
# Manual check:
psql -U postgres -d copilot -c "\dt"
psql -U postgres -d copilot -c "\d document_chunks"
```

### Docker (planned)
```bash
docker compose up -d         # Start all services
docker compose down          # Stop all services
```
