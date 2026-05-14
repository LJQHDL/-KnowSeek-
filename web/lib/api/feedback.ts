"use client";

import { api } from "@/lib/api/client";
import type { ApiResponse } from "@/types/api";

export interface FeedbackResult {
  id: number;
  messageId: number;
  userId: number;
  rating: "up" | "down";
  comment: string | null;
  createdAt: string;
}

export async function submitFeedback(messageId: number, payload: { rating: "up" | "down"; comment?: string }) {
  const { data } = await api.post<ApiResponse<FeedbackResult>>(`/api/messages/${messageId}/feedback`, payload);
  return data.data;
}
