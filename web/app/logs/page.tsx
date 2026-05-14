"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Header } from "@/components/layout/header";
import { Card } from "@/components/ui/card";
import { fetchMessageLogs, fetchRetrievalLogs, type LogMessageItem, type LogRetrievalItem } from "@/lib/api/logs";
import { formatDate } from "@/lib/utils";
import { ChevronLeft, ChevronRight } from "lucide-react";

type Tab = "messages" | "retrievals";

export default function LogsPage() {
  const [tab, setTab] = useState<Tab>("messages");
  const [page, setPage] = useState(1);
  const pageSize = 20;

  const messagesQuery = useQuery({
    queryKey: ["logs-messages", page],
    queryFn: () => fetchMessageLogs({ page, pageSize }),
    enabled: tab === "messages",
  });

  const retrievalsQuery = useQuery({
    queryKey: ["logs-retrievals", page],
    queryFn: () => fetchRetrievalLogs({ page, pageSize }),
    enabled: tab === "retrievals",
  });

  const data = tab === "messages" ? messagesQuery.data : retrievalsQuery.data;
  const total = data?.total ?? 0;
  const totalPages = Math.max(1, Math.ceil(total / pageSize));

  return (
    <div className="min-h-screen">
      <Header />
      <main className="mx-auto max-w-7xl px-5 py-10 lg:px-8">
        <h1 className="mb-6 text-2xl font-semibold tracking-[-0.04em]">日志</h1>

        <div className="mb-6 flex gap-2">
          {(["messages", "retrievals"] as Tab[]).map((t) => (
            <button
              key={t}
              type="button"
              onClick={() => { setTab(t); setPage(1); }}
              className={`rounded-lg px-4 py-2 text-sm font-medium transition-colors ${
                tab === t ? "bg-foreground text-white" : "bg-muted/60 text-muted hover:bg-muted"
              }`}
            >
              {t === "messages" ? "Q&A 日志" : "检索日志"}
            </button>
          ))}
        </div>

        <Card className="overflow-hidden">
          {tab === "messages" ? (
            <MessagesTable items={(data?.items as LogMessageItem[]) ?? []} isLoading={messagesQuery.isLoading} />
          ) : (
            <RetrievalsTable items={(data?.items as LogRetrievalItem[]) ?? []} isLoading={retrievalsQuery.isLoading} />
          )}

          <div className="flex items-center justify-between border-t px-5 py-3">
            <span className="text-sm text-muted">
              共 {total} 条，第 {page} / {totalPages} 页
            </span>
            <div className="flex gap-1">
              <button
                type="button"
                disabled={page <= 1}
                onClick={() => setPage((p) => p - 1)}
                className="rounded p-1.5 text-muted hover:bg-muted/50 disabled:opacity-30"
              >
                <ChevronLeft className="size-4" />
              </button>
              <button
                type="button"
                disabled={page >= totalPages}
                onClick={() => setPage((p) => p + 1)}
                className="rounded p-1.5 text-muted hover:bg-muted/50 disabled:opacity-30"
              >
                <ChevronRight className="size-4" />
              </button>
            </div>
          </div>
        </Card>
      </main>
    </div>
  );
}

function MessagesTable({ items, isLoading }: { items: LogMessageItem[]; isLoading: boolean }) {
  if (isLoading) {
    return <div className="px-5 py-12 text-center text-sm text-muted">加载中...</div>;
  }
  if (items.length === 0) {
    return <div className="px-5 py-12 text-center text-sm text-muted">暂无数据</div>;
  }
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b bg-muted/30 text-left text-xs uppercase text-muted">
            <th className="px-5 py-3 font-medium">ID</th>
            <th className="px-5 py-3 font-medium">角色</th>
            <th className="px-5 py-3 font-medium">知识库</th>
            <th className="px-5 py-3 font-medium">内容</th>
            <th className="px-5 py-3 font-medium">延迟</th>
            <th className="px-5 py-3 font-medium">Tokens</th>
            <th className="px-5 py-3 font-medium">时间</th>
          </tr>
        </thead>
        <tbody>
          {items.map((m) => (
            <tr key={m.id} className="border-b last:border-0 hover:bg-muted/20">
              <td className="px-5 py-3 tabular-nums text-muted">{m.id}</td>
              <td className="px-5 py-3">
                <span className={m.role === "assistant" ? "text-accent" : ""}>
                  {m.role === "assistant" ? "AI" : "用户"}
                </span>
              </td>
              <td className="px-5 py-3 text-muted">{m.knowledgeBaseName ?? "-"}</td>
              <td className="max-w-xs truncate px-5 py-3">{m.content}</td>
              <td className="px-5 py-3 tabular-nums text-muted">
                {m.latencyMs != null ? `${(m.latencyMs / 1000).toFixed(1)}s` : "-"}
              </td>
              <td className="px-5 py-3 tabular-nums text-muted">
                {m.promptTokens != null ? `↑${m.promptTokens} ↓${m.completionTokens}` : "-"}
              </td>
              <td className="px-5 py-3 text-muted whitespace-nowrap">{formatDate(m.createdAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function RetrievalsTable({ items, isLoading }: { items: LogRetrievalItem[]; isLoading: boolean }) {
  if (isLoading) {
    return <div className="px-5 py-12 text-center text-sm text-muted">加载中...</div>;
  }
  if (items.length === 0) {
    return <div className="px-5 py-12 text-center text-sm text-muted">暂无数据</div>;
  }
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b bg-muted/30 text-left text-xs uppercase text-muted">
            <th className="px-5 py-3 font-medium">ID</th>
            <th className="px-5 py-3 font-medium">Message ID</th>
            <th className="px-5 py-3 font-medium">查询文本</th>
            <th className="px-5 py-3 font-medium">TopK</th>
            <th className="px-5 py-3 font-medium">延迟</th>
            <th className="px-5 py-3 font-medium">时间</th>
          </tr>
        </thead>
        <tbody>
          {items.map((r) => (
            <tr key={r.id} className="border-b last:border-0 hover:bg-muted/20">
              <td className="px-5 py-3 tabular-nums text-muted">{r.id}</td>
              <td className="px-5 py-3 tabular-nums text-muted">{r.messageId}</td>
              <td className="max-w-xs truncate px-5 py-3">{r.queryText}</td>
              <td className="px-5 py-3 tabular-nums text-muted">{r.topK}</td>
              <td className="px-5 py-3 tabular-nums text-muted">
                {r.latencyMs != null ? `${r.latencyMs}ms` : "-"}
              </td>
              <td className="px-5 py-3 text-muted whitespace-nowrap">{formatDate(r.createdAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
