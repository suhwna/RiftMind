import { Activity, AlertTriangle, Coins, Eye, Shield, Swords } from "lucide-react";
import type { ReactNode } from "react";

type InterpretationTagChipProps = {
  tag: string;
};

type TagPresentation = {
  icon: ReactNode;
  className: string;
};

const TAG_PRESENTATIONS: Record<string, TagPresentation> = {
  캐리: {
    icon: <Swords className="h-3.5 w-3.5" />,
    className: "border-amber-500/35 bg-amber-500/10 text-amber-200",
  },
  고전: {
    icon: <AlertTriangle className="h-3.5 w-3.5" />,
    className: "border-rose-500/35 bg-rose-500/10 text-rose-200",
  },
  "시야 기여": {
    icon: <Eye className="h-3.5 w-3.5" />,
    className: "border-sky-500/35 bg-sky-500/10 text-sky-200",
  },
  "성장 우위": {
    icon: <Coins className="h-3.5 w-3.5" />,
    className: "border-emerald-500/35 bg-emerald-500/10 text-emerald-200",
  },
  "교전 기여": {
    icon: <Activity className="h-3.5 w-3.5" />,
    className: "border-violet-500/35 bg-violet-500/10 text-violet-200",
  },
  안정적: {
    icon: <Shield className="h-3.5 w-3.5" />,
    className: "border-slate-500/35 bg-slate-500/10 text-slate-200",
  },
};

/**
 * 경기 해석 태그를 아이콘과 색상으로 표현하는 공용 칩입니다.
 */
export function InterpretationTagChip({ tag }: InterpretationTagChipProps) {
  const presentation = TAG_PRESENTATIONS[tag] ?? {
    icon: <Activity className="h-3.5 w-3.5" />,
    className: "border-slate-600 bg-[#111827] text-slate-200",
  };

  return (
    <span
      className={[
        "inline-flex items-center gap-1.5 border px-2 py-1 text-[11px] font-semibold uppercase tracking-[0.12em]",
        presentation.className,
      ].join(" ")}
    >
      {presentation.icon}
      {tag}
    </span>
  );
}
