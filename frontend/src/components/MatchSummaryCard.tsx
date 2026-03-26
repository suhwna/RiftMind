import { Link } from "react-router-dom";
import { getChampionPortraitUrl } from "../lib/assets";
import type { SummonerMatchSummaryResponse } from "../types/api";
import { InterpretationTagChip } from "./InterpretationTagChip";
import { KdaBadge } from "./KdaBadge";

type MatchSummaryCardProps = {
  puuid: string;
  match: SummonerMatchSummaryResponse;
};

export function MatchSummaryCard({ puuid, match }: MatchSummaryCardProps) {
  const interpretationTags = match.interpretationTags ?? [];

  return (
    <Link
      to={`/matches/${match.matchId}?puuid=${puuid}`}
      className="group block border border-slate-800 bg-[#0c1420] transition hover:border-slate-700 hover:bg-[#101928]"
    >
      <div className="px-4 py-3">
        <div className="flex items-start justify-between gap-4">
          <div className="flex min-w-0 flex-1 items-start gap-3">
            <img
              src={getChampionPortraitUrl(match.championKey, match.championName)}
              alt={match.championNameKo}
              className="h-12 w-12 shrink-0 border border-slate-700 bg-[#111827] object-cover"
            />
            <div className="min-w-0 flex-1">
              <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-tide">{match.queueNameKo}</p>
              <h3 className="mt-1 truncate text-[18px] font-semibold tracking-[-0.04em] text-white">{match.championNameKo}</h3>
              <div className="mt-1 flex flex-wrap items-center gap-2 text-sm text-slate-400">
                {match.teamPositionKo ? (
                  <>
                    <span>{match.teamPositionKo}</span>
                    <span className="h-3 w-px bg-slate-700" />
                  </>
                ) : null}
                <span>{new Date(match.gameCreation).toLocaleString("ko-KR")}</span>
              </div>
            </div>
          </div>

          <div className="flex shrink-0 flex-col items-end gap-2">
            <span
              className={`border px-2.5 py-1 text-sm font-semibold ${
                match.win
                  ? "border-emerald-900 bg-emerald-950/30 text-emerald-200"
                  : "border-rose-900 bg-rose-950/30 text-rose-200"
              }`}
            >
              {match.win ? "승리" : "패배"}
            </span>
            <div className="text-right text-[11px] font-medium uppercase tracking-[0.16em] text-slate-500">Details</div>
          </div>
        </div>

        <div className="mt-3 flex flex-col gap-2 border-t border-slate-800 pt-3 lg:flex-row lg:items-center lg:justify-between">
          <KdaBadge kills={match.kills} deaths={match.deaths} assists={match.assists} />
          {interpretationTags.length > 0 ? (
            <div className="flex flex-wrap gap-2 lg:max-w-[60%] lg:justify-end">
              {interpretationTags.map((tag) => (
                <InterpretationTagChip key={tag} tag={tag} />
              ))}
            </div>
          ) : null}
        </div>
      </div>
    </Link>
  );
}
