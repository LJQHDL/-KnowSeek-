import { FileText, LoaderCircle, Trash2, TriangleAlert } from "lucide-react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { formatDate } from "@/lib/utils";
import type { DocumentItem } from "@/types/api";

const statusTone: Record<string, string> = {
  READY: "bg-emerald-100 text-emerald-700",
  INDEXING: "bg-amber-100 text-amber-700",
  PARSING: "bg-sky-100 text-sky-700",
  UPLOADED: "bg-stone-200 text-stone-700",
  FAILED: "bg-red-100 text-red-700"
};

export function DocumentList({
  documents,
  onDelete,
  deletingId
}: {
  documents: DocumentItem[];
  onDelete?: (id: number) => void;
  deletingId?: number | null;
}) {
  if (documents.length === 0) {
    return (
      <Card className="border-dashed">
        <div className="flex flex-col items-center justify-center gap-3 py-10 text-center">
          <FileText className="size-8 text-muted" />
          <div className="space-y-1">
            <p className="font-medium">还没有文档</p>
            <p className="text-sm text-muted">先上传一份 PDF、DOCX 或 Markdown，系统会自动解析并建立索引。</p>
          </div>
        </div>
      </Card>
    );
  }

  return (
    <div className="space-y-4">
      {documents.map((document) => (
        <Card key={document.id} className="flex items-center justify-between gap-4">
          <div className="min-w-0 space-y-1">
            <div className="flex items-center gap-2">
              <p className="truncate font-medium">{document.fileName}</p>
              <span className={`rounded-full px-2.5 py-1 text-xs font-medium ${statusTone[document.status] ?? "bg-black/5 text-muted"}`}>
                {document.status}
              </span>
            </div>
            <p className="text-sm text-muted">
              {document.fileType} · {formatDate(document.createdAt)}
            </p>
            {document.errorMessage ? (
              <p className="flex items-center gap-2 text-sm text-danger">
                <TriangleAlert className="size-4" />
                {document.errorMessage}
              </p>
            ) : null}
          </div>
          <div className="flex items-center gap-2">
            {document.status === "INDEXING" || document.status === "PARSING" ? (
              <LoaderCircle className="size-5 animate-spin text-accent" />
            ) : null}
            {onDelete ? (
              <Button
                type="button"
                variant="secondary"
                className="px-3"
                disabled={deletingId === document.id}
                onClick={() => {
                  if (window.confirm(`确认删除文档“${document.fileName}”吗？`)) {
                    onDelete(document.id);
                  }
                }}
              >
                <Trash2 className="size-4" />
              </Button>
            ) : null}
          </div>
        </Card>
      ))}
    </div>
  );
}
