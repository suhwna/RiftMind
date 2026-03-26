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
        "border border-slate-800 bg-[#0f1724] p-5",
        className ?? "",
      ].join(" ")}
    >
      {title || description || action ? (
        <div className="mb-4 flex items-start justify-between gap-4 border-b border-slate-800 pb-4">
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
