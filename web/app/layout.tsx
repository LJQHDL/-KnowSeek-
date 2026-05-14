import type { Metadata } from "next";
import { Providers } from "@/components/providers";
import { ToastContainer } from "@/components/ui/toast";
import "@/app/globals.css";

export const metadata: Metadata = {
  title: "Enterprise Knowledge Copilot",
  description: "A polished frontend shell for the AI knowledge copilot project."
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN">
      <body>
        <Providers>
          {children}
          <ToastContainer />
        </Providers>
      </body>
    </html>
  );
}
