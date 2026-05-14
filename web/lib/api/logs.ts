"use client";

import { api } from "@/lib/api/client";
import type { ApiResponse } from "@/types/api";

export interface LogMessageItem {
  id: number;
  sessionId: number;
  knowledgeBaseId: number | null;
  knowledgeBaseName: string | null;
  role: string;
  content: string;
  citationsJson: string | null;
  latencyMs: number | null;
  promptTokens: number | null;
  completionTokens: number | null;
  createdAt: string;
}

export interface LogRetrievalItem {
  id: number;
  messageId: number;
  queryText: string;
  retrievedChunksJson: string | null;
  topK: number;
  latencyMs: number | null;
  createdAt: string;
}

export interface PaginatedResult<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}

export async function fetchMessageLogs(params: {
  page?: number;
  pageSize?: number;
  knowledgeBaseId?: number;
  startDate?: string;
  endDate?: string;
}) {
  const { data } = await api.get<ApiResponse<PaginatedResult<LogMessageItem>>>("/api/logs/messages", { params });
  return data.data;
}

export async function fetchRetrievalLogs(params: { page?: number; pageSize?: number }) {
  const { data } = await api.get<ApiResponse<PaginatedResult<LogRetrievalItem>>>("/api/logs/retrievals", { params });
  return data.data;
}
