"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { Sparkles, LogOut } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useAppStore } from "@/hooks/use-app-store";

export function Header() {
  const router = useRouter();
  const token = useAppStore((s) => s.token);
  const userName = useAppStore((s) => s.userName);
  const clearAuth = useAppStore((s) => s.clearAuth);

  function handleLogout() {
    clearAuth();
    router.push("/login");
  }

  return (
    <header className="sticky top-0 z-20 border-b border-white/50 bg-[#fbf8f4]/70 backdrop-blur-xl">
      <div className="mx-auto flex w-full max-w-7xl items-center justify-between px-5 py-4 lg:px-8">
        <Link href="/" className="flex items-center gap-3">
          <span className="flex size-10 items-center justify-center rounded-2xl bg-accent text-white shadow-soft">
            <Sparkles className="size-5" />
          </span>
          <div>
            <div className="text-sm font-semibold tracking-wide">Enterprise Knowledge Copilot</div>
            <div className="text-xs text-muted">Knowledge interface for AI-native teams</div>
          </div>
        </Link>
        <div className="flex items-center gap-3">
          {token ? (
            <>
              <span className="text-sm text-muted">{userName}</span>
              <Link href="/knowledge-bases">
                <Button variant="ghost">工作台</Button>
              </Link>
              <Button variant="ghost" onClick={handleLogout}>
                <LogOut className="size-4 mr-1" />
                退出
              </Button>
            </>
          ) : (
            <>
              <Link href="/knowledge-bases">
                <Button variant="ghost">进入工作台</Button>
              </Link>
              <Link href="/login">
                <Button>开始使用</Button>
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
