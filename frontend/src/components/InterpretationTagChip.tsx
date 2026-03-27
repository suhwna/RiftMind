import { Activity, AlertTriangle, Coins, Eye, Shield, Swords } from "lucide-react";

type InterpretationTagChipProps = {
  tag: string;
};

type TagPresentation = {
  icon: typeof Swords;
  className: string;
  description: string;
};

const TAG_PRESENTATIONS: Record<string, TagPresentation> = {
  캐리: {
    icon: Swords,
    className: "border-amber-500/35 bg-amber-500/10 text-amber-200",
    description: "같은 경기 10명 중에서 KDA, 딜량, 킬 영향력이 상위권일 때 붙습니다.",
  },
  고전: {
    icon: AlertTriangle,
    className: "border-rose-500/35 bg-rose-500/10 text-rose-200",
    description: "같은 경기 10명 비교에서 데스 부담이 크고 KDA가 낮은 편일 때 붙습니다.",
  },
  "시야 기여": {
    icon: Eye,
    className: "border-sky-500/35 bg-sky-500/10 text-sky-200",
    description: "같은 경기 10명 중 시야 점수와 와드 기여가 상위권일 때 붙습니다.",
  },
  "성장 우위": {
    icon: Coins,
    className: "border-emerald-500/35 bg-emerald-500/10 text-emerald-200",
    description: "같은 경기 10명 기준으로 골드와 CS가 상위권일 때 붙습니다.",
  },
  "교전 기여": {
    icon: Activity,
    className: "border-violet-500/35 bg-violet-500/10 text-violet-200",
    description: "같은 경기 10명 비교에서 킬 관여나 챔피언 피해량이 높은 편일 때 붙습니다.",
  },
  안정적: {
    icon: Shield,
    className: "border-slate-500/35 bg-slate-500/10 text-slate-200",
    description: "같은 경기 10명과 비교해 데스 관리가 좋고 KDA가 무너지지 않을 때 붙습니다.",
  },
};

/**
 * 경기 해석 태그를 아이콘 칩으로 표시하고, hover 시 기준 설명을 표시합니다.
 */
export function InterpretationTagChip({ tag }: InterpretationTagChipProps) {
  const presentation = TAG_PRESENTATIONS[tag] ?? {
    icon: Activity,
    className: "border-slate-600 bg-[#111827] text-slate-200",
    description: "같은 경기 10명 기준 비교로 계산된 태그입니다.",
  };
  const Icon = presentation.icon;

  return (
    <div className="group/tag relative inline-flex">
      <span
        className={[
          "inline-flex items-center gap-1.5 border px-2 py-1 text-[11px] font-semibold uppercase tracking-[0.12em]",
          "transition hover:brightness-110",
          presentation.className,
        ].join(" ")}
      >
        <Icon className="h-3.5 w-3.5" />
        {tag}
      </span>

      <div className="pointer-events-none absolute left-0 top-full z-20 mt-2 w-56 translate-y-1 border border-slate-700 bg-[#020617] p-3 text-xs text-slate-200 opacity-0 shadow-[0_14px_30px_rgba(2,6,23,0.5)] transition duration-150 group-hover/tag:translate-y-0 group-hover/tag:opacity-100">
        <p className="text-[10px] font-semibold uppercase tracking-[0.18em] text-tide">태그 기준</p>
        <p className="mt-2 font-semibold text-white">{tag}</p>
        <p className="mt-1 leading-5 text-slate-300">{presentation.description}</p>
      </div>
    </div>
  );
}
