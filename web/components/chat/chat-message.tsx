import { motion } from "framer-motion";
import { Bot, User } from "lucide-react";
import { Card } from "@/components/ui/card";
import { formatDate } from "@/lib/utils";
import type { MessageItem } from "@/types/api";

export function ChatMessage({ message }: { message: MessageItem }) {
  const isAssistant = message.role === "assistant";

  return (
    <motion.div
      initial={{ opacity: 0, y: 14 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.24 }}
      className={`flex gap-3 ${isAssistant ? "justify-start" : "justify-end"}`}
    >
      {isAssistant ? (
        <span className="mt-1 flex size-9 shrink-0 items-center justify-center rounded-2xl bg-accent text-white">
          <Bot className="size-4" />
        </span>
      ) : null}
      <Card className={`max-w-[min(100%,56rem)] p-4 lg:p-5 ${isAssistant ? "flex-1 bg-white/86" : "bg-[#fff4ed] border-accent/25"}`}>
        <div className="mb-2 flex items-center gap-2 text-xs text-muted">
          {!isAssistant ? <User className="size-3.5" /> : null}
          <span>{isAssistant ? "Assistant" : "You"}</span>
          <span>·</span>
          <span>{formatDate(message.createdAt)}</span>
        </div>
        <p className="whitespace-pre-wrap break-words text-[15px] leading-8">{message.content}</p>
        {isAssistant && (
          <div className="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-[11px] text-muted">
            {message.latencyMs != null && message.latencyMs > 0 && (
              <span>{(message.latencyMs / 1000).toFixed(1)}s</span>
            )}
            {message.promptTokens != null && message.promptTokens > 0 && (
              <span>↑{message.promptTokens} tokens</span>
            )}
            {message.completionTokens != null && message.completionTokens > 0 && (
              <span>↓{message.completionTokens} tokens</span>
            )}
          </div>
        )}
      </Card>
      {!isAssistant ? (
        <span className="mt-1 flex size-9 shrink-0 items-center justify-center rounded-2xl bg-foreground text-white">
          <User className="size-4" />
        </span>
      ) : null}
    </motion.div>
  );
}
