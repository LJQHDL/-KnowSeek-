"use client";

import { useState } from "react";
import { ArrowUp } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";

export function ChatInput({
  onSubmit,
  disabled
}: {
  onSubmit: (value: string) => Promise<void> | void;
  disabled?: boolean;
}) {
  const [value, setValue] = useState("");

  async function handleSubmit() {
    const trimmed = value.trim();
    if (!trimmed || disabled) {
      return;
    }
    await onSubmit(trimmed);
    setValue("");
  }

  return (
    <div className="rounded-[28px] border border-white/60 bg-card p-4 shadow-card backdrop-blur-xl">
      <Textarea
        value={value}
        onChange={(event) => setValue(event.target.value)}
        placeholder="输入一个与你的知识库相关的问题，例如：'产品手册里关于权限控制的约束是什么？'"
        className="min-h-[120px] border-0 bg-transparent p-0 shadow-none focus:ring-0"
      />
      <div className="mt-4 flex items-center justify-between gap-4">
        <p className="text-xs text-muted">系统会优先基于知识库片段生成回答，并返回可追踪的引用片段。</p>
        <Button onClick={handleSubmit} disabled={disabled}>
          发送问题
          <ArrowUp className="ml-2 size-4" />
        </Button>
      </div>
    </div>
  );
}
