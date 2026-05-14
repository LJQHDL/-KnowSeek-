"use client";

import { ChangeEvent, useMemo, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { ArrowRight } from "lucide-react";
import { deleteDocument, getKnowledgeBase, listDocuments, uploadDocument } from "@/lib/api/knowledge-base";
import { useAppStore } from "@/hooks/use-app-store";
import { Header } from "@/components/layout/header";
import { DocumentList } from "@/components/knowledge-base/document-list";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import type { DocumentItem } from "@/types/api";

export default function KnowledgeBaseDetailPage() {
  const params = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const addToast = useAppStore((s) => s.addToast);
  const knowledgeBaseId = Number(params.id);
  const [uploading, setUploading] = useState(false);
  const [deletingDocumentId, setDeletingDocumentId] = useState<number | null>(null);

  const { data: kb } = useQuery({
    queryKey: ["knowledge-base", knowledgeBaseId],
    queryFn: () => getKnowledgeBase(knowledgeBaseId)
  });

  const { data: documents = [] } = useQuery({
    queryKey: ["documents", knowledgeBaseId],
    queryFn: () => listDocuments(knowledgeBaseId),
    refetchInterval: (query) => {
      const current = (query.state.data as DocumentItem[] | undefined) ?? [];
      const hasProcessing = current.some((document) =>
        ["UPLOADED", "PARSING", "INDEXING"].includes(document.status)
      );
      return hasProcessing ? 2000 : false;
    }
  });

  const hasProcessingDocuments = useMemo(
    () => documents.some((document) => ["UPLOADED", "PARSING", "INDEXING"].includes(document.status)),
    [documents]
  );

  async function handleUpload(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }
    setUploading(true);
    try {
      const uploaded = await uploadDocument(knowledgeBaseId, file);
      queryClient.setQueryData<DocumentItem[]>(["documents", knowledgeBaseId], (current = []) => [uploaded, ...current]);
      await queryClient.invalidateQueries({ queryKey: ["documents", knowledgeBaseId] });
    } catch {
      addToast("上传文档失败，请检查文件大小和格式。");
    } finally {
      setUploading(false);
      event.target.value = "";
    }
  }

  async function handleDeleteDocument(documentId: number) {
    setDeletingDocumentId(documentId);
    try {
      await deleteDocument(documentId);
      await queryClient.invalidateQueries({ queryKey: ["documents", knowledgeBaseId] });
    } catch {
      addToast("删除文档失败。");
    } finally {
      setDeletingDocumentId(null);
    }
  }

  return (
    <div className="min-h-screen">
      <Header />
      <main className="mx-auto max-w-7xl px-5 py-10 lg:px-8">
        <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
          <div className="space-y-2">
            <p className="text-sm uppercase tracking-[0.22em] text-muted">Knowledge Base Detail</p>
            <h1 className="text-4xl font-semibold tracking-[-0.04em]">{kb?.name ?? "加载中..."}</h1>
            <p className="max-w-2xl text-sm leading-6 text-muted">
              {kb?.description || "在这里上传文档、观察状态流转，并进入会话页面验证检索和回答链路。"}
            </p>
          </div>
          <Link href={`/knowledge-bases/${knowledgeBaseId}/chat`}>
            <Button>
              进入问答页面
              <ArrowRight className="ml-2 size-4" />
            </Button>
          </Link>
        </div>

        <div className="grid gap-8 xl:grid-cols-[0.8fr_1.2fr]">
          <Card className="space-y-4">
            <div className="space-y-2">
              <h2 className="text-xl font-semibold">上传新文档</h2>
              <p className="text-sm leading-6 text-muted">
                当前后端会在上传后自动触发解析、切分、embedding 和状态流转。这里保留极简上传入口，方便快速验证链路。
              </p>
            </div>
            <label className="flex cursor-pointer flex-col items-center justify-center rounded-[28px] border border-dashed border-line bg-white/60 px-6 py-10 text-center transition hover:border-accent/40 hover:bg-white/85">
              <span className="text-base font-medium">{uploading ? "上传中..." : "选择一个文档上传"}</span>
              <span className="mt-2 text-sm text-muted">支持 PDF / DOCX / Markdown</span>
              <input type="file" className="hidden" onChange={handleUpload} accept=".pdf,.docx,.md,.markdown" />
            </label>
          </Card>

          <section className="space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-semibold">文档状态</h2>
              <p className="text-sm text-muted">
                {documents.length} 个文档{hasProcessingDocuments ? " · 正在处理..." : ""}
              </p>
            </div>
            <DocumentList documents={documents} onDelete={handleDeleteDocument} deletingId={deletingDocumentId} />
          </section>
        </div>
      </main>
    </div>
  );
}
