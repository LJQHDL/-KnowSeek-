import Link from "next/link";
import { ArrowRight, Database, MessageSquareText, Sparkles, Upload } from "lucide-react";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";

const highlights = [
  {
    icon: Upload,
    title: "文档进入系统后自动处理",
    description: "上传 PDF、DOCX、Markdown 后，系统会自动解析、切分、Embedding 并建立可检索上下文。"
  },
  {
    icon: MessageSquareText,
    title: "问答链路可追踪",
    description: "每次回答都附带结构化引用片段与检索日志，为评测和可信度优化打基础。"
  },
  {
    icon: Database,
    title: "从 V0 到生产形态自然演进",
    description: "当前骨架已经覆盖认证、知识库、文档、会话和检索，可继续扩展前端工作台与评测面板。"
  }
];

export default function HomePage() {
  return (
    <div className="min-h-screen">
      <Header />
      <main className="mx-auto max-w-7xl px-5 pb-20 pt-10 lg:px-8 lg:pt-16">
        <section className="grid items-center gap-8 lg:grid-cols-[1.1fr_0.9fr]">
          <div className="space-y-7">
            <div className="inline-flex items-center gap-2 rounded-full border border-white/60 bg-white/70 px-4 py-2 text-sm text-muted shadow-soft">
              <Sparkles className="size-4 text-accent" />
              Premium AI knowledge workspace for modern teams
            </div>
            <div className="space-y-5">
              <h1 className="max-w-3xl text-5xl font-semibold leading-[1.05] tracking-[-0.04em] text-foreground sm:text-6xl">
                把企业文档变成一个
                <span className="block text-accent">能检索、能解释、能持续优化的 AI 工作台</span>
              </h1>
              <p className="max-w-2xl text-lg leading-8 text-muted">
                这个前端骨架不是简单后台模板，而是围绕知识库、上传、会话、引用和评测体验构建的产品界面。它为后续真正的 RAG、模型调用和运营分析预留了足够清晰的结构。
              </p>
            </div>
            <div className="flex flex-wrap gap-3">
              <Link href="/knowledge-bases">
                <Button className="h-12 px-5">
                  打开知识库工作台
                  <ArrowRight className="ml-2 size-4" />
                </Button>
              </Link>
              <Link href="/login">
                <Button variant="secondary" className="h-12 px-5">
                  登录并联调接口
                </Button>
              </Link>
            </div>
          </div>

          <Card className="overflow-hidden p-0">
            <div className="border-b border-line bg-white/80 p-5">
              <div className="flex items-center gap-3">
                <div className="flex gap-2">
                  <span className="size-3 rounded-full bg-[#f97316]" />
                  <span className="size-3 rounded-full bg-[#facc15]" />
                  <span className="size-3 rounded-full bg-[#22c55e]" />
                </div>
                <span className="text-sm text-muted">Copilot workspace preview</span>
              </div>
            </div>
            <div className="grid gap-4 bg-[linear-gradient(180deg,rgba(255,255,255,0.72),rgba(255,255,255,0.48))] p-5">
              {highlights.map((item) => {
                const Icon = item.icon;
                return (
                  <div key={item.title} className="rounded-3xl border border-white/60 bg-white/70 p-5">
                    <div className="mb-4 flex size-11 items-center justify-center rounded-2xl bg-accent/10 text-accent">
                      <Icon className="size-5" />
                    </div>
                    <h3 className="mb-2 text-base font-semibold">{item.title}</h3>
                    <p className="text-sm leading-6 text-muted">{item.description}</p>
                  </div>
                );
              })}
            </div>
          </Card>
        </section>
      </main>
    </div>
  );
}
