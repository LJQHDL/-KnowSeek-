"use client";

import { api } from "@/lib/api/client";
import type { ApiResponse, ChatReply, ChatSession, MessageItem } from "@/types/api";

export async function createSession(payload: { knowledgeBaseId: number; title: string }) {
  const { data } = await api.post<ApiResponse<ChatSession>>("/api/chat/sessions", payload);
  return data.data;
}

export async function listSessions() {
  const { data } = await api.get<ApiResponse<ChatSession[]>>("/api/chat/sessions");
  return data.data;
}

export async function sendMessage(sessionId: number, payload: { content: string }) {
  const { data } = await api.post<ApiResponse<ChatReply>>(`/api/chat/sessions/${sessionId}/messages`, payload);
  return data.data;
}

export async function listMessages(sessionId: number) {
  const { data } = await api.get<ApiResponse<MessageItem[]>>(`/api/chat/sessions/${sessionId}/messages`);
  return data.data;
}
