import type { PropsWithChildren, ReactNode } from "react";

type SectionCardProps = PropsWithChildren<{
  title: string;
  description?: string;
  action?: ReactNode;
  className?: string;
}>;

export function SectionCard({ title, description, action, className, children }: SectionCardProps) {
  return (
    <section
      className={[
        "rounded-[24px] border border-white/8 bg-[#111827] p-6 shadow-card",
        className ?? "",
      ].join(" ")}
    >
      {title || description || action ? (
        <div className="mb-5 flex items-start justify-between gap-4">
          <div>
            {title ? <h2 className="text-lg font-semibold tracking-[-0.03em] text-white">{title}</h2> : null}
            {description ? <p className="mt-1 text-sm leading-6 text-slate-400">{description}</p> : null}
          </div>
          {action}
        </div>
      ) : null}
      {children}
    </section>
  );
}
