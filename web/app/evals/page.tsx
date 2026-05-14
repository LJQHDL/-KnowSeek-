"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Header } from "@/components/layout/header";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { createEvalRun, listEvalRuns, getEvalRunDetail, type EvalCaseInput, type EvalRunItem } from "@/lib/api/evals";
import { useAppStore } from "@/hooks/use-app-store";
import { formatDate } from "@/lib/utils";
import { ChevronDown, ChevronUp, Plus, X } from "lucide-react";

export default function EvalsPage() {
  const [showCreate, setShowCreate] = useState(false);
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const queryClient = useQueryClient();
  const addToast = useAppStore((s) => s.addToast);

  const { data: runs, isLoading } = useQuery({
    queryKey: ["eval-runs"],
    queryFn: listEvalRuns,
  });

  const createMutation = useMutation({
    mutationFn: createEvalRun,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["eval-runs"] });
      setShowCreate(false);
      addToast("评测已开始运行", "success");
    },
    onError: () => addToast("评测创建失败", "error"),
  });

  const handleToggleDetail = (id: number) => {
    setExpandedId(expandedId === id ? null : id);
  };

  return (
    <div className="min-h-screen">
      <Header />
      <main className="mx-auto max-w-7xl px-5 py-10 lg:px-8">
        <div className="mb-6 flex items-center justify-between">
          <h1 className="text-2xl font-semibold tracking-[-0.04em]">评测工作台</h1>
          <Button onClick={() => setShowCreate(true)}>
            <Plus className="mr-1.5 size-4" />
            新建评测
          </Button>
        </div>

        {showCreate && (
          <CreateEvalModal
            onClose={() => setShowCreate(false)}
            onSubmit={(payload) => createMutation.mutate(payload)}
            loading={createMutation.isPending}
          />
        )}

        <Card className="overflow-hidden">
          {isLoading ? (
            <div className="px-5 py-12 text-center text-sm text-muted">加载中...</div>
          ) : !runs || runs.length === 0 ? (
            <div className="px-5 py-12 text-center text-sm text-muted">暂无评测记录，点击上方按钮创建</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b bg-muted/30 text-left text-xs uppercase text-muted">
                    <th className="px-5 py-3 font-medium">ID</th>
                    <th className="px-5 py-3 font-medium">名称</th>
                    <th className="px-5 py-3 font-medium">状态</th>
                    <th className="px-5 py-3 font-medium">Hit Rate</th>
                    <th className="px-5 py-3 font-medium">MRR</th>
                    <th className="px-5 py-3 font-medium">平均延迟</th>
                    <th className="px-5 py-3 font-medium">时间</th>
                    <th className="px-5 py-3 font-medium" />
                  </tr>
                </thead>
                <tbody>
                  {runs.map((run) => (
                    <RunRow
                      key={run.id}
                      run={run}
                      isExpanded={expandedId === run.id}
                      onToggle={() => handleToggleDetail(run.id)}
                    />
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Card>
      </main>
    </div>
  );
}

function RunRow({ run, isExpanded, onToggle }: { run: EvalRunItem; isExpanded: boolean; onToggle: () => void }) {
  return (
    <>
      <tr className="border-b last:border-0 hover:bg-muted/20">
        <td className="px-5 py-3 tabular-nums text-muted">{run.id}</td>
        <td className="px-5 py-3 font-medium">{run.name}</td>
        <td className="px-5 py-3">
          <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${
            run.status === "COMPLETED" ? "bg-green-100 text-green-700" :
            run.status === "RUNNING" ? "bg-blue-100 text-blue-700" :
            run.status === "FAILED" ? "bg-red-100 text-red-700" :
            "bg-muted text-muted"
          }`}>
            {run.status}
          </span>
        </td>
        <td className="px-5 py-3 tabular-nums">
          {run.hitRate != null ? `${(run.hitRate * 100).toFixed(1)}%` : "-"}
        </td>
        <td className="px-5 py-3 tabular-nums">
          {run.mrr != null ? run.mrr.toFixed(3) : "-"}
        </td>
        <td className="px-5 py-3 tabular-nums text-muted">
          {run.avgLatencyMs != null ? `${(run.avgLatencyMs / 1000).toFixed(1)}s` : "-"}
        </td>
        <td className="px-5 py-3 text-muted whitespace-nowrap">{formatDate(run.createdAt)}</td>
        <td className="px-5 py-3">
          {run.status === "COMPLETED" && (
            <button type="button" onClick={onToggle} className="rounded p-1 hover:bg-muted/50">
              {isExpanded ? <ChevronUp className="size-4" /> : <ChevronDown className="size-4" />}
            </button>
          )}
        </td>
      </tr>
      {isExpanded && (
        <tr>
          <td colSpan={8} className="bg-muted/10 px-5 py-4">
            <RunDetail runId={run.id} />
          </td>
        </tr>
      )}
    </>
  );
}

function RunDetail({ runId }: { runId: number }) {
  const { data: run } = useQuery({
    queryKey: ["eval-run-detail", runId],
    queryFn: () => getEvalRunDetail(runId),
  });

  if (!run) return <div className="text-sm text-muted">加载中...</div>;

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const cases = (run as any).cases;
  if (!cases || cases.length === 0) return <div className="text-sm text-muted">无用例数据</div>;

  return (
    <div className="space-y-3">
      <p className="text-xs font-medium text-muted uppercase">评测用例 ({cases.length})</p>
      {cases.map((c: any, i: number) => (
        <Card key={c.id ?? i} className="p-4 text-sm">
          <div className="mb-2 flex items-center gap-2">
            <span className="text-xs text-muted">#{i + 1}</span>
            <span className="font-medium">{c.question}</span>
          </div>
          <div className="grid grid-cols-4 gap-3 text-xs text-muted">
            <div>
              <span className="block font-medium text-foreground">Hit</span>
              {c.retrievalHit != null ? (c.retrievalHit ? "yes" : "no") : "-"}
            </div>
            <div>
              <span className="block font-medium text-foreground">Rank</span>
              {c.retrievalRank != null ? c.retrievalRank : "-"}
            </div>
            <div>
              <span className="block font-medium text-foreground">Score</span>
              {c.score != null ? (c.score * 100).toFixed(0) + "%" : "-"}
            </div>
            <div>
              <span className="block font-medium text-foreground">延迟</span>
              {c.latencyMs != null ? (c.latencyMs / 1000).toFixed(1) + "s" : "-"}
            </div>
          </div>
          {c.actualAnswer && (
            <div className="mt-2 max-h-32 overflow-y-auto rounded bg-white p-2 text-xs whitespace-pre-wrap">
              {c.actualAnswer}
            </div>
          )}
          {c.errorMessage && (
            <div className="mt-1 text-red-600 text-xs">{c.errorMessage}</div>
          )}
        </Card>
      ))}
    </div>
  );
}

function CreateEvalModal({
  onClose,
  onSubmit,
  loading,
}: {
  onClose: () => void;
  onSubmit: (payload: { name: string; knowledgeBaseId: number; cases: EvalCaseInput[] }) => void;
  loading: boolean;
}) {
  const [name, setName] = useState("");
  const [kbId, setKbId] = useState("");
  const [casesText, setCasesText] = useState(`[
  {
    "question": "What is RAG?",
    "expectedAnswer": "RAG stands for Retrieval-Augmented Generation..."
  }
]`);
  const [parseError, setParseError] = useState("");

  const handleSubmit = () => {
    if (!name.trim()) { setParseError("请输入评测名称"); return; }
    const kbIdNum = Number(kbId);
    if (!kbIdNum) { setParseError("请输入有效的知识库 ID"); return; }
    try {
      const cases = JSON.parse(casesText);
      if (!Array.isArray(cases) || cases.length === 0) {
        setParseError("用例必须是非空数组");
        return;
      }
      setParseError("");
      onSubmit({ name: name.trim(), knowledgeBaseId: kbIdNum, cases });
    } catch {
      setParseError("JSON 格式错误，请检查");
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-full max-w-2xl rounded-2xl bg-white p-6 shadow-xl">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold">新建评测</h2>
          <button type="button" onClick={onClose} className="rounded p-1 hover:bg-muted/50">
            <X className="size-5" />
          </button>
        </div>

        <div className="space-y-4">
          <div>
            <label className="mb-1 block text-sm font-medium">评测名称</label>
            <Input value={name} onChange={(e) => setName(e.target.value)} placeholder="如：V0 基线评测" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium">知识库 ID</label>
            <Input value={kbId} onChange={(e) => setKbId(e.target.value)} placeholder="输入数字 ID" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium">评测用例 (JSON)</label>
            <Textarea
              value={casesText}
              onChange={(e) => setCasesText(e.target.value)}
              className="min-h-[180px] font-mono text-xs"
            />
          </div>
          {parseError && <p className="text-sm text-red-600">{parseError}</p>}
        </div>

        <div className="mt-5 flex justify-end gap-3">
          <Button variant="secondary" onClick={onClose}>取消</Button>
          <Button onClick={handleSubmit} disabled={loading}>
            {loading ? "运行中..." : "开始评测"}
          </Button>
        </div>
      </div>
    </div>
  );
}
