"use client";

import { api } from "@/lib/api/client";
import type { ApiResponse } from "@/types/api";

export interface EvalCaseItem {
  id: number;
  question: string;
  expectedAnswer: string | null;
  expectedSources: string | null;
  actualAnswer: string | null;
  actualSources: string | null;
  retrievalHit: boolean | null;
  retrievalRank: number | null;
  score: number | null;
  latencyMs: number | null;
  errorMessage: string | null;
}

export interface EvalRunItem {
  id: number;
  knowledgeBaseId: number;
  name: string;
  status: string;
  totalCases: number;
  hitCount: number;
  hitRate: number | null;
  mrr: number | null;
  avgLatencyMs: number | null;
  avgScore: number | null;
  startedAt: string | null;
  finishedAt: string | null;
  createdAt: string;
}

export interface EvalRunDetail extends EvalRunItem {
  cases: EvalCaseItem[];
}

export interface EvalCaseInput {
  question: string;
  expectedAnswer?: string;
  expectedSources?: string;
}

export async function createEvalRun(payload: {
  name: string;
  knowledgeBaseId: number;
  cases: EvalCaseInput[];
}) {
  const { data } = await api.post<ApiResponse<EvalRunItem>>("/api/evals/run", payload);
  return data.data;
}

export async function listEvalRuns() {
  const { data } = await api.get<ApiResponse<EvalRunItem[]>>("/api/evals");
  return data.data;
}

export async function getEvalRunDetail(id: number) {
  const { data } = await api.get<ApiResponse<EvalRunItem>>(`/api/evals/${id}`);
  return data.data;
}
