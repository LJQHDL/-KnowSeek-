import { Card } from "@/components/ui/card";
import type { RetrievedChunk } from "@/types/api";

export function CitationCard({ chunk, index }: { chunk: RetrievedChunk; index: number }) {
  return (
    <Card className="rounded-2xl border-white/70 bg-white/72 p-4">
      <div className="mb-2 flex items-center justify-between text-xs text-muted">
        <span>引用片段 {index + 1}</span>
        <span>
          文档 {chunk.documentId} · Chunk {chunk.chunkIndex}
        </span>
      </div>
      <p className="text-sm leading-6 text-foreground/80">{chunk.snippet}</p>
    </Card>
  );
}
