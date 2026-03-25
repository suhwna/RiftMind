import { Link } from "react-router-dom";
import { getChampionPortraitUrl } from "../lib/assets";
import type { SummonerMatchSummaryResponse } from "../types/api";
import { KdaBadge } from "./KdaBadge";

type MatchSummaryCardProps = {
  puuid: string;
  match: SummonerMatchSummaryResponse;
};

export function MatchSummaryCard({ puuid, match }: MatchSummaryCardProps) {
  return (
    <Link
      to={`/matches/${match.matchId}?puuid=${puuid}`}
      className="group block rounded-[20px] border border-white/8 bg-[#0b1020] p-5 transition hover:border-tide/40 hover:bg-[#0d1422]"
    >
      <div className="flex items-start justify-between gap-4">
        <div className="flex min-w-0 flex-1 items-start gap-4">
          <img
            src={getChampionPortraitUrl(match.championKey)}
            alt={match.championNameKo}
            className="h-14 w-14 shrink-0 rounded-2xl border border-white/8 bg-[#131a28] object-cover"
          />
          <div className="min-w-0 flex-1">
            <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-tide">{match.queueNameKo}</p>
            <h3 className="mt-2 truncate text-[22px] font-semibold tracking-[-0.04em] text-white">{match.championNameKo}</h3>
            <p className="mt-2 text-sm text-slate-400">{new Date(match.gameCreation).toLocaleString("ko-KR")}</p>
          </div>
        </div>

        <span
          className={`rounded-full px-3 py-1 text-sm font-semibold ${
            match.win ? "bg-emerald-950/40 text-emerald-200" : "bg-rose-950/40 text-rose-200"
          }`}
        >
          {match.win ? "승리" : "패배"}
        </span>
      </div>

      <div className="mt-5 flex flex-wrap items-center gap-2">
        <span className="rounded-full border border-white/8 bg-white/5 px-3 py-1 text-sm text-slate-300">
          {match.teamPositionKo ?? "포지션 미상"}
        </span>
      </div>

      <div className="mt-5">
        <KdaBadge kills={match.kills} deaths={match.deaths} assists={match.assists} />
      </div>
    </Link>
  );
}
