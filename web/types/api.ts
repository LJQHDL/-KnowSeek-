export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface AuthResponse {
  userId: number;
  email: string;
  name: string;
  token: string;
}

export interface KnowledgeBase {
  id: number;
  name: string;
  description: string;
  createdAt: string;
}

export interface DocumentItem {
  id: number;
  knowledgeBaseId: number;
  fileName: string;
  fileType: string;
  status: string;
  errorMessage: string | null;
  createdAt: string;
}

export interface ChatSession {
  id: number;
  knowledgeBaseId: number;
  title: string;
  createdAt: string;
}

export interface MessageItem {
  id: number;
  sessionId: number;
  role: "user" | "assistant";
  content: string;
  citationsJson: string | null;
  latencyMs: number | null;
  promptTokens: number | null;
  completionTokens: number | null;
  createdAt: string;
}

export interface RetrievedChunk {
  chunkId: number;
  documentId: number;
  chunkIndex: number;
  snippet: string;
}

export interface ChatReply {
  userMessage: MessageItem;
  assistantMessage: MessageItem;
  retrievedChunks: RetrievedChunk[];
}
