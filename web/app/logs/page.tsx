import { Header } from "@/components/layout/header";
import { Card } from "@/components/ui/card";

export default function LogsPage() {
  return (
    <div className="min-h-screen">
      <Header />
      <main className="mx-auto max-w-7xl px-5 py-10 lg:px-8">
        <Card className="space-y-3">
          <p className="text-sm uppercase tracking-[0.22em] text-muted">Logs</p>
          <h1 className="text-3xl font-semibold tracking-[-0.04em]">日志与可观测性页面预留</h1>
          <p className="max-w-2xl text-sm leading-6 text-muted">
            当前后端已经开始记录 `retrieval_logs` 和 `citationsJson`。等你补上日志查询接口后，这里可以做成真正的调试面板。
          </p>
        </Card>
      </main>
    </div>
  );
}
