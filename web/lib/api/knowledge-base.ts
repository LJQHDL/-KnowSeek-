"use client";

import { api } from "@/lib/api/client";
import type { ApiResponse, DocumentItem, KnowledgeBase } from "@/types/api";

export async function listKnowledgeBases() {
  const { data } = await api.get<ApiResponse<KnowledgeBase[]>>("/api/knowledge-bases");
  return data.data;
}

export async function createKnowledgeBase(payload: { name: string; description: string }) {
  const { data } = await api.post<ApiResponse<KnowledgeBase>>("/api/knowledge-bases", payload);
  return data.data;
}

export async function deleteKnowledgeBase(id: number) {
  await api.delete(`/api/knowledge-bases/${id}`);
}

export async function getKnowledgeBase(id: number) {
  const { data } = await api.get<ApiResponse<KnowledgeBase>>(`/api/knowledge-bases/${id}`);
  return data.data;
}

export async function listDocuments(id: number) {
  const { data } = await api.get<ApiResponse<DocumentItem[]>>(`/api/knowledge-bases/${id}/documents`);
  return data.data;
}

export async function uploadDocument(id: number, file: File) {
  const form = new FormData();
  form.append("file", file);
  const { data } = await api.post<ApiResponse<DocumentItem>>(`/api/knowledge-bases/${id}/documents`, form);
  return data.data;
}

export async function deleteDocument(id: number) {
  await api.delete(`/api/documents/${id}`);
}

export async function reindexDocument(id: number) {
  await api.post(`/api/documents/${id}/reindex`);
}
