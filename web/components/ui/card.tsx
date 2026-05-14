import { cn } from "@/lib/utils";

export function Card({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn(
        "rounded-[28px] border border-white/60 bg-card p-6 shadow-card backdrop-blur-xl",
        className
      )}
      {...props}
    />
  );
}
