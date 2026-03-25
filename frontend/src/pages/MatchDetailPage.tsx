 import { useQuery } from "@tanstack/react-query";
import { Shield, Sparkles, Swords } from "lucide-react";
import type { ReactNode } from "react";
import { useParams, useSearchParams } from "react-router-dom";
import { SectionCard } from "../components/SectionCard";
import { getChampionPortraitUrl } from "../lib/assets";
import { getMatchDetail } from "../lib/api";

export function MatchDetailPage() {
  const { matchId = "" } = useParams();
  const [searchParams] = useSearchParams();
  const focusPuuid = searchParams.get("puuid");

  const matchDetailQuery = useQuery({
    queryKey: ["match-detail", matchId],
    queryFn: () => getMatchDetail(matchId),
    enabled: Boolean(matchId),
  });

  const detail = matchDetailQuery.data;

  return (
    <div className="space-y-6">
      <SectionCard
        title={detail ? `${detail.queueNameKo} · ${detail.gameDurationText}` : "매치 상세"}
        description={detail ? `${new Date(detail.gameCreation).toLocaleString("ko-KR")} · ${detail.gameVersion}` : ""}
      >
        {matchDetailQuery.isLoading ? <p className="text-sm text-slate-400">매치 상세를 불러오는 중입니다...</p> : null}
        {matchDetailQuery.isError ? (
          <p className="rounded-2xl bg-rose-950/40 px-4 py-3 text-sm text-rose-200">
            {(matchDetailQuery.error as Error).message}
          </p>
        ) : null}

        {detail ? (
          <div className="mt-2 grid gap-4 md:grid-cols-3">
            <SummaryTile icon={<Sparkles className="h-4 w-4" />} label="게임 길이" value={detail.gameDurationText} />
            <SummaryTile icon={<Swords className="h-4 w-4" />} label="게임 모드" value={detail.queueNameKo} />
            <SummaryTile icon={<Shield className="h-4 w-4" />} label="패치 버전" value={detail.gameVersion} />
          </div>
        ) : null}
      </SectionCard>

      {detail ? (
        <div className="grid gap-4 lg:grid-cols-2">
          {detail.participants.map((participant) => {
            const isFocus = participant.puuid === focusPuuid;
            const totalCs = participant.totalMinionsKilled + participant.neutralMinionsKilled;

            return (
              <SectionCard
                key={`${participant.puuid}-${participant.championName}`}
                title=""
                description=""
                className={isFocus ? "border-tide/40 ring-1 ring-tide/30" : ""}
              >
                <div className="mb-5 flex items-start justify-between gap-4">
                  <div className="flex min-w-0 items-start gap-4">
                    <img
                      src={getChampionPortraitUrl(participant.championKey)}
                      alt={participant.championNameKo}
                      className="h-16 w-16 shrink-0 rounded-[18px] border border-white/8 bg-[#131a28] object-cover"
                    />
                    <div className="min-w-0">
                      <h3 className="truncate text-xl font-semibold tracking-[-0.04em] text-white">
                        {participant.championNameKo} · {participant.summonerName}
                      </h3>
                      <p className="mt-2 text-sm text-slate-400">
                        {participant.teamPositionKo ?? "포지션 미상"} · {participant.win ? "승리" : "패배"}
                      </p>
                    </div>
                  </div>
                </div>

                <div className="grid gap-4 md:grid-cols-2">
                  <InfoBox title="전투">
                    <p className="text-base font-semibold text-white">
                      {participant.kills} / {participant.deaths} / {participant.assists}
                    </p>
                    <p className="mt-2">챔피언 대상 피해량 {participant.totalDamageDealtToChampions.toLocaleString()}</p>
                    <p>받은 피해량 {participant.totalDamageTaken.toLocaleString()}</p>
                  </InfoBox>

                  <InfoBox title="성장">
                    <p>레벨 {participant.champLevel}</p>
                    <p>골드 {participant.goldEarned.toLocaleString()}</p>
                    <p>총 CS {totalCs.toLocaleString()}</p>
                  </InfoBox>

                  <InfoBox title="시야">
                    <p>시야 점수 {participant.visionScore}</p>
                    <p>와드 설치 {participant.wardsPlaced}</p>
                    <p>와드 제거 {participant.wardsKilled}</p>
                  </InfoBox>

                  <InfoBox title="세팅">
                    <p>스펠 {participant.summonerSpellNames.filter(Boolean).join(" / ") || "-"}</p>
                    <p>주 룬 {participant.primaryRuneName ?? "-"}</p>
                    <p>보조 룬 {participant.secondaryRuneName ?? "-"}</p>
                  </InfoBox>
                </div>

                <div className="mt-4 rounded-[18px] border border-white/8 bg-[#0b1020] p-4">
                  <p className="text-xs font-semibold uppercase tracking-[0.16em] text-tide">아이템</p>
                  <div className="mt-3 flex flex-wrap gap-2">
                    {participant.itemNames.filter(Boolean).map((itemName) => (
                      <span
                        key={`${participant.puuid}-${itemName}`}
                        className="rounded-full border border-white/8 bg-white/5 px-3 py-1 text-sm text-slate-300"
                      >
                        {itemName}
                      </span>
                    ))}
                  </div>
                </div>
              </SectionCard>
            );
          })}
        </div>
      ) : null}
    </div>
  );
}

type SummaryTileProps = {
  icon: ReactNode;
  label: string;
  value: string;
};

function SummaryTile({ icon, label, value }: SummaryTileProps) {
  return (
    <div className="rounded-[18px] border border-white/8 bg-[#0b1020] p-5">
      <div className="flex items-center gap-2 text-tide">
        {icon}
        <p className="text-xs font-semibold uppercase tracking-[0.16em]">{label}</p>
      </div>
      <p className="mt-3 text-xl font-semibold tracking-[-0.03em] text-white">{value}</p>
    </div>
  );
}

type InfoBoxProps = {
  title: string;
  children: ReactNode;
};

function InfoBox({ title, children }: InfoBoxProps) {
  return (
    <div className="rounded-[18px] border border-white/8 bg-[#0b1020] p-4 text-sm text-slate-300">
      <p className="text-xs font-semibold uppercase tracking-[0.16em] text-tide">{title}</p>
      <div className="mt-3 space-y-1.5">{children}</div>
    </div>
  );
}
