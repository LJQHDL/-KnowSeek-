"use client";

import { create } from "zustand";

interface Toast {
  id: string;
  message: string;
  type: "error" | "success";
}

interface AppStoreState {
  token: string | null;
  userId: number | null;
  userName: string | null;
  toasts: Toast[];
  setAuth: (token: string, userId: number, userName: string) => void;
  clearAuth: () => void;
  addToast: (message: string, type?: "error" | "success") => void;
  removeToast: (id: string) => void;
}

export const useAppStore = create<AppStoreState>((set) => ({
  token: null,
  userId: null,
  userName: null,
  toasts: [],
  setAuth: (token, userId, userName) => {
    if (typeof window !== "undefined") {
      window.localStorage.setItem("copilot_token", token);
      window.localStorage.setItem("copilot_user_id", String(userId));
      window.localStorage.setItem("copilot_user_name", userName);
    }
    set({ token, userId, userName });
  },
  clearAuth: () => {
    if (typeof window !== "undefined") {
      window.localStorage.removeItem("copilot_token");
      window.localStorage.removeItem("copilot_user_id");
      window.localStorage.removeItem("copilot_user_name");
    }
    set({ token: null, userId: null, userName: null });
  },
  addToast: (message, type = "error") => {
    const id = Math.random().toString(36).slice(2);
    set((state) => ({ toasts: [...state.toasts, { id, message, type }] }));
    setTimeout(() => {
      set((state) => ({ toasts: state.toasts.filter((t) => t.id !== id) }));
    }, 4000);
  },
  removeToast: (id) => {
    set((state) => ({ toasts: state.toasts.filter((t) => t.id !== id) }));
  }
}));
