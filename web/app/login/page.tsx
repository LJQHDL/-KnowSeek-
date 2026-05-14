"use client";

import { FormEvent, useState } from "react";
import { motion } from "framer-motion";
import { useRouter } from "next/navigation";
import { login, register } from "@/lib/api/auth";
import { useAppStore } from "@/hooks/use-app-store";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";

export default function LoginPage() {
  const router = useRouter();
  const setAuth = useAppStore((state) => state.setAuth);
  const [mode, setMode] = useState<"login" | "register">("login");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setLoading(true);

    const formData = new FormData(event.currentTarget);
    const email = String(formData.get("email") ?? "");
    const password = String(formData.get("password") ?? "");
    const name = String(formData.get("name") ?? "");

    try {
      const response =
        mode === "login"
          ? await login({ email, password })
          : await register({ email, password, name });
      setAuth(response.token, response.userId, response.name);
      router.push("/knowledge-bases");
    } catch (err) {
      setError("登录失败，请检查服务是否启动，以及账号信息是否正确。");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center px-5 py-12">
      <motion.div initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} className="w-full max-w-md">
        <Card className="space-y-6">
          <div className="space-y-2 text-center">
            <p className="text-sm uppercase tracking-[0.25em] text-muted">Copilot Access</p>
            <h1 className="text-3xl font-semibold tracking-[-0.04em]">
              {mode === "login" ? "进入知识库工作台" : "创建一个新的工作空间账号"}
            </h1>
            <p className="text-sm leading-6 text-muted">保持极简登录流程，把注意力留给文档上传、问答和引用调试。</p>
          </div>

          <form className="space-y-4" onSubmit={handleSubmit}>
            {mode === "register" ? <Input name="name" placeholder="你的昵称" required /> : null}
            <Input name="email" type="email" placeholder="邮箱地址" required />
            <Input name="password" type="password" placeholder="密码" required />
            {error ? <p className="text-sm text-danger">{error}</p> : null}
            <Button type="submit" className="h-12 w-full" disabled={loading}>
              {loading ? "提交中..." : mode === "login" ? "登录" : "注册"}
            </Button>
          </form>

          <button
            className="w-full text-sm text-muted transition hover:text-foreground"
            onClick={() => setMode(mode === "login" ? "register" : "login")}
            type="button"
          >
            {mode === "login" ? "没有账号？切换到注册" : "已有账号？返回登录"}
          </button>
        </Card>
      </motion.div>
    </main>
  );
}
