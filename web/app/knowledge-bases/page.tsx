"use client";

import { FormEvent, useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { createKnowledgeBase, deleteKnowledgeBase, listKnowledgeBases } from "@/lib/api/knowledge-base";
import { useAppStore } from "@/hooks/use-app-store";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { KnowledgeBaseCard } from "@/components/knowledge-base/knowledge-base-card";

export default function KnowledgeBasesPage() {
  const queryClient = useQueryClient();
  const addToast = useAppStore((s) => s.addToast);
  const [submitting, setSubmitting] = useState(false);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const { data = [], isLoading } = useQuery({
    queryKey: ["knowledge-bases"],
    queryFn: listKnowledgeBases
  });

  async function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    const form = event.currentTarget;
    const formData = new FormData(form);
    try {
      await createKnowledgeBase({
        name: String(formData.get("name") ?? ""),
        description: String(formData.get("description") ?? "")
      });
      form.reset();
      await queryClient.invalidateQueries({ queryKey: ["knowledge-bases"] });
    } catch {
      addToast("创建知识库失败，请检查后端服务是否启动。");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(id: number) {
    setDeletingId(id);
    try {
      await deleteKnowledgeBase(id);
      await queryClient.invalidateQueries({ queryKey: ["knowledge-bases"] });
    } catch {
      addToast("删除知识库失败。");
    } finally {
      setDeletingId(null);
    }
  }

  return (
    <div className="min-h-screen">
      <Header />
      <main className="mx-auto max-w-7xl px-5 py-10 lg:px-8">
        <div className="grid gap-8 xl:grid-cols-[0.8fr_1.2fr]">
          <Card className="h-fit space-y-5">
            <div className="space-y-2">
              <p className="text-sm uppercase tracking-[0.22em] text-muted">Create Knowledge Base</p>
              <h1 className="text-3xl font-semibold tracking-[-0.04em]">先定义知识边界，再构建问答体验</h1>
              <p className="text-sm leading-6 text-muted">一个清晰的知识库结构，会让后续上传、检索、引用和评测都更容易解释。</p>
            </div>

            <form className="space-y-4" onSubmit={handleCreate}>
              <Input name="name" placeholder="例如：产品手册库" required />
              <Textarea name="description" placeholder="描述这个知识库的范围、用途和预期提问类型。" />
              <Button className="w-full" disabled={submitting}>
                {submitting ? "创建中..." : "创建知识库"}
              </Button>
            </form>
          </Card>

          <section className="space-y-5">
            <div className="flex items-end justify-between gap-4">
              <div>
                <p className="text-sm uppercase tracking-[0.22em] text-muted">Workspace</p>
                <h2 className="text-3xl font-semibold tracking-[-0.04em]">知识库总览</h2>
              </div>
              <p className="text-sm text-muted">{data.length} 个知识库</p>
            </div>

            {isLoading ? (
              <Card className="p-8 text-center text-muted">正在加载知识库列表...</Card>
            ) : data.length === 0 ? (
              <Card className="p-10 text-center">
                <p className="font-medium">还没有知识库</p>
                <p className="mt-2 text-sm text-muted">从左侧创建第一个知识库，然后继续上传文档并开始联调问答。</p>
              </Card>
            ) : (
              <div className="grid gap-5 md:grid-cols-2">
                {data.map((kb) => (
                  <KnowledgeBaseCard
                    key={kb.id}
                    kb={kb}
                    deleting={deletingId === kb.id}
                    onDelete={handleDelete}
                  />
                ))}
              </div>
            )}
          </section>
        </div>
      </main>
    </div>
  );
}
