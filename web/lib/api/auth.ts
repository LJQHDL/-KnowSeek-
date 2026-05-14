"use client";

import { api } from "@/lib/api/client";
import type { ApiResponse, AuthResponse } from "@/types/api";

export async function login(payload: { email: string; password: string }) {
  const { data } = await api.post<ApiResponse<AuthResponse>>("/api/auth/login", payload);
  return data.data;
}

export async function register(payload: { email: string; name: string; password: string }) {
  const { data } = await api.post<ApiResponse<AuthResponse>>("/api/auth/register", payload);
  return data.data;
}
