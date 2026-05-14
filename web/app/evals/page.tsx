import { Header } from "@/components/layout/header";
import { Card } from "@/components/ui/card";

export default function EvalsPage() {
  return (
    <div className="min-h-screen">
      <Header />
      <main className="mx-auto max-w-7xl px-5 py-10 lg:px-8">
        <Card className="space-y-3">
          <p className="text-sm uppercase tracking-[0.22em] text-muted">Evaluations</p>
          <h1 className="text-3xl font-semibold tracking-[-0.04em]">评测工作台预留</h1>
          <p className="max-w-2xl text-sm leading-6 text-muted">
            前端骨架已经为后续评测页预留了入口。等后端补齐评测接口后，可以把 hit rate、latency、answer quality 做成趋势化可视面板。
          </p>
        </Card>
      </main>
    </div>
  );
}
