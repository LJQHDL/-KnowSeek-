"use client";

import * as React from "react";
import { cn } from "@/lib/utils";

type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "secondary" | "ghost";
};

export function Button({ className, variant = "primary", ...props }: ButtonProps) {
  return (
    <button
      className={cn(
        "inline-flex items-center justify-center rounded-full px-4 py-2.5 text-sm font-medium transition duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-transparent disabled:cursor-not-allowed disabled:opacity-60",
        variant === "primary" &&
          "bg-accent text-white shadow-soft hover:bg-accentStrong focus:ring-accent",
        variant === "secondary" &&
          "border border-line bg-white/70 text-foreground hover:bg-white focus:ring-accent",
        variant === "ghost" && "text-foreground hover:bg-black/5 focus:ring-accent",
        className
      )}
      {...props}
    />
  );
}
