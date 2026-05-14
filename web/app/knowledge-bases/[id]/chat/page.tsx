"use client";

import { useMemo, useState } from "react";
import { useParams } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createSession, listMessages, listSessions, sendMessage } from "@/lib/api/chat";
import { useAppStore } from "@/hooks/use-app-store";
import { Header } from "@/components/layout/header";
import { ChatInput } from "@/components/chat/chat-input";
import { ChatMessage } from "@/components/chat/chat-message";
import { CitationCard } from "@/components/chat/citation-card";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";

export default function ChatPage() {
  const params = useParams<{ id: string }>();
  const knowledgeBaseId = Number(params.id);
  const queryClient = useQueryClient();
  const addToast = useAppStore((s) => s.addToast);
  const [activeSessionId, setActiveSessionId] = useState<number | null>(null);
  const [latestCitations, setLatestCitations] = useState<Array<{ chunkId: number; documentId: number; chunkIndex: number; snippet: string }>>([]);

  const sessionsQuery = useQuery({
    queryKey: ["chat-sessions"],
    queryFn: listSessions
  });

  const activeResolvedSessionId = activeSessionId ?? sessionsQuery.data?.find((item) => item.knowledgeBaseId === knowledgeBaseId)?.id ?? null;

  const messagesQuery = useQuery({
    queryKey: ["messages", activeResolvedSessionId],
    queryFn: () => listMessages(activeResolvedSessionId!),
    enabled: Boolean(activeResolvedSessionId)
  });

  const createSessionMutation = useMutation({
    mutationFn: () => createSession({ knowledgeBaseId, title: "第一次知识库会话" }),
    onSuccess: async (session) => {
      setActiveSessionId(session.id);
      await queryClient.invalidateQueries({ queryKey: ["chat-sessions"] });
    }
  });

  const sendMutation = useMutation({
    mutationFn: async (content: string) => {
      let sessionId = activeResolvedSessionId;
      if (!sessionId) {
        const session = await createSessionMutation.mutateAsync();
        sessionId = session.id;
      }
      return sendMessage(sessionId, { content });
    },
    onSuccess: async (reply) => {
      setLatestCitations(reply.retrievedChunks);
      await queryClient.invalidateQueries({ queryKey: ["messages", activeResolvedSessionId] });
    },
    onError: () => {
      addToast("发送消息失败，请检查后端服务和 AI 配置。");
    }
  });

  const sessionOptions = useMemo(
    () => (sessionsQuery.data ?? []).filter((session) => session.knowledgeBaseId === knowledgeBaseId),
    [sessionsQuery.data, knowledgeBaseId]
  );

  return (
    <div className="min-h-screen">
      <Header />
      <main className="mx-auto max-w-7xl px-5 py-10 lg:px-8">
        <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
          <div>
            <p className="text-sm uppercase tracking-[0.22em] text-muted">Chat Workspace</p>
            <h1 className="text-4xl font-semibold tracking-[-0.04em]">对知识库发问，并观察引用如何返回</h1>
          </div>
          <div className="flex items-center gap-3">
            {sessionOptions.map((session) => (
              <Button
                key={session.id}
                variant={session.id === activeResolvedSessionId ? "primary" : "secondary"}
                onClick={() => setActiveSessionId(session.id)}
              >
                {session.title}
              </Button>
            ))}
          </div>
        </div>

        <div className="grid items-start gap-8 xl:grid-cols-[minmax(0,1.55fr)_minmax(320px,0.75fr)]">
          <section className="space-y-6">
            <Card className="flex min-h-[560px] flex-col p-5 lg:h-[calc(100vh-16rem)] lg:min-h-[640px] lg:p-6">
              <div className="flex-1 space-y-5 overflow-y-auto pr-1">
                {messagesQuery.data?.length ? (
                  messagesQuery.data.map((message) => <ChatMessage key={message.id} message={message} />)
                ) : (
                  <div className="flex min-h-[320px] items-center justify-center text-center">
                    <div className="space-y-2">
                      <p className="text-lg font-medium">开始一次真正的联调问答</p>
                      <p className="max-w-md text-sm leading-6 text-muted">
                        发送一个问题后，前端会展示消息流，后端会尝试检索知识库片段，并返回结构化引用结果。
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </Card>

            <ChatInput
              onSubmit={async (value) => {
                await sendMutation.mutateAsync(value);
              }}
              disabled={sendMutation.isPending}
            />
          </section>

          <section className="space-y-4 xl:sticky xl:top-24">
            <Card className="space-y-3 p-5">
              <h2 className="text-xl font-semibold">引用侧栏</h2>
              <p className="text-sm leading-6 text-muted">
                这里展示最近一次回答所使用的引用片段，后续可以自然扩展为 citation 跳转、证据高亮和调试面板。
              </p>
            </Card>

            {latestCitations.length === 0 ? (
              <Card className="p-8 text-center text-sm text-muted">发送一条消息后，这里会出现检索到的知识库片段。</Card>
            ) : (
              <div className="space-y-3 xl:max-h-[calc(100vh-16rem)] xl:overflow-y-auto xl:pr-2">
                {latestCitations.map((chunk, index) => (
                  <CitationCard key={`${chunk.chunkId}-${index}`} chunk={chunk} index={index} />
                ))}
              </div>
            )}
          </section>
        </div>
      </main>
    </div>
  );
}
