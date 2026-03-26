type KdaBadgeProps = {
  kills: number;
  deaths: number;
  assists: number;
};

export function KdaBadge({ kills, deaths, assists }: KdaBadgeProps) {
  const kda = deaths === 0 ? kills + assists : (kills + assists) / deaths;

  return (
    <div className="inline-flex items-center gap-2 border border-slate-700 bg-[#111827] px-3 py-2 text-sm font-medium text-slate-200">
      <span>
        {kills} / {deaths} / {assists}
      </span>
      <span className="text-xs text-slate-400">KDA {kda.toFixed(2)}</span>
    </div>
  );
}
