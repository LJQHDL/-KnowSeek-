import Link from "next/link";
import { ArrowRight, Database, Trash2 } from "lucide-react";
import { motion } from "framer-motion";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { formatDate } from "@/lib/utils";
import type { KnowledgeBase } from "@/types/api";

export function KnowledgeBaseCard({ kb, onDelete, deleting }: { kb: KnowledgeBase; onDelete: (id: number) => void; deleting?: boolean }) {
  return (
    <motion.div whileHover={{ y: -4 }} transition={{ duration: 0.18 }}>
      <Card className="group h-full transition duration-200 hover:border-accent/35 hover:bg-white/90">
        <div className="mb-5 flex items-start justify-between gap-4">
          <span className="flex size-11 items-center justify-center rounded-2xl bg-accent/10 text-accent">
            <Database className="size-5" />
          </span>
          <div className="flex items-center gap-2">
            <span className="rounded-full bg-black/5 px-3 py-1 text-xs text-muted">{formatDate(kb.createdAt)}</span>
            <Button
              type="button"
              variant="secondary"
              className="px-3"
              disabled={deleting}
              onClick={() => {
                if (window.confirm(`确认删除知识库“${kb.name}”吗？该操作会同时删除其下文档和聊天数据。`)) {
                  onDelete(kb.id);
                }
              }}
            >
              <Trash2 className="size-4" />
            </Button>
          </div>
        </div>
        <Link href={`/knowledge-bases/${kb.id}`} className="block">
          <div className="space-y-2">
            <h3 className="text-lg font-semibold">{kb.name}</h3>
            <p className="line-clamp-2 text-sm leading-6 text-muted">{kb.description || "还没有为这个知识库填写说明。"}</p>
          </div>
          <div className="mt-6 flex items-center gap-2 text-sm font-medium text-accent">
            打开知识库
            <ArrowRight className="size-4 transition group-hover:translate-x-1" />
          </div>
        </Link>
      </Card>
    </motion.div>
  );
}
