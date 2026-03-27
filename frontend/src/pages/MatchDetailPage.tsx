import { useQuery } from "@tanstack/react-query";
import { ArrowLeft, Shield, Sparkles, Swords } from "lucide-react";
import type { ReactNode } from "react";
import { useMemo } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { InterpretationTagChip } from "../components/InterpretationTagChip";
import { SectionCard } from "../components/SectionCard";
import { getChampionPortraitUrl } from "../lib/assets";
import { getMatchDetail } from "../lib/api";
import type { MatchParticipantResponse } from "../types/api";

const POSITION_ORDER: Record<string, number> = {
  TOP: 1,
  JUNGLE: 2,
  MIDDLE: 3,
  BOTTOM: 4,
  UTILITY: 5,
};

export function MatchDetailPage() {
  const { matchId = "" } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const focusPuuid = searchParams.get("puuid");

  const matchDetailQuery = useQuery({
    queryKey: ["match-detail", matchId, focusPuuid],
    queryFn: () => getMatchDetail(matchId, focusPuuid),
    enabled: Boolean(matchId),
  });

  const detail = matchDetailQuery.data;
  const blueTeam = useMemo(
    () => sortParticipants(detail?.participants.filter((participant) => participant.teamId === 100) ?? []),
    [detail],
  );
  const redTeam = useMemo(
    () => sortParticipants(detail?.participants.filter((participant) => participant.teamId === 200) ?? []),
    [detail],
  );
  const hasTeamSides = blueTeam.length > 0 || redTeam.length > 0;
  const blueTeamWon = blueTeam.some((participant) => participant.win);
  const orderedTeams = useMemo(() => {
    if (!hasTeamSides) {
      return [];
    }

    const bluePanel = {
      title: blueTeamWon ? "승리 팀" : "패배 팀",
      sideLabel: "블루 진영",
      side: "blue" as const,
      participants: blueTeam,
      isWinningTeam: blueTeamWon,
    };
    const redPanel = {
      title: blueTeamWon ? "패배 팀" : "승리 팀",
      sideLabel: "레드 진영",
      side: "red" as const,
      participants: redTeam,
      isWinningTeam: !blueTeamWon,
    };

    return blueTeamWon ? [bluePanel, redPanel] : [redPanel, bluePanel];
  }, [blueTeam, redTeam, blueTeamWon, hasTeamSides]);

  return (
    <div className="space-y-4">
      <SectionCard
        title={detail ? `${detail.queueNameKo} · ${detail.gameDurationText}` : "매치 상세"}
        description={detail ? `${new Date(detail.gameCreation).toLocaleString("ko-KR")} · ${detail.gameVersion}` : ""}
      >
        <div className="mb-3">
          <button
            type="button"
            onClick={() => navigate(-1)}
            className="inline-flex items-center gap-2 border border-slate-700 bg-[#111827] px-3 py-2 text-sm text-slate-300 transition hover:border-slate-600 hover:text-white"
          >
            <ArrowLeft className="h-4 w-4" />
            뒤로가기
          </button>
        </div>
        {matchDetailQuery.isLoading ? <p className="text-sm text-slate-400">매치 상세를 불러오는 중입니다...</p> : null}
        {matchDetailQuery.isError ? (
          <p className="rounded-2xl bg-rose-950/40 px-4 py-3 text-sm text-rose-200">
            {(matchDetailQuery.error as Error).message}
          </p>
        ) : null}

        {detail ? (
          <div className="mt-2 grid gap-3 md:grid-cols-3">
            <SummaryTile icon={<Sparkles className="h-4 w-4" />} label="게임 길이" value={detail.gameDurationText} />
            <SummaryTile icon={<Swords className="h-4 w-4" />} label="게임 모드" value={detail.queueNameKo} />
            <SummaryTile icon={<Shield className="h-4 w-4" />} label="패치 버전" value={detail.gameVersion} />
          </div>
        ) : null}

        {detail && (detail.focusStrengths.length > 0 || detail.focusWeaknesses.length > 0) ? (
          <div className="mt-4 grid gap-3 md:grid-cols-2">
            <InsightPanel
              title="좋은 점"
              accentClassName="text-emerald-300"
              dotClassName="bg-emerald-400"
              items={detail.focusStrengths}
            />
            <InsightPanel
              title="아쉬운 점"
              accentClassName="text-amber-300"
              dotClassName="bg-amber-400"
              items={detail.focusWeaknesses}
            />
          </div>
        ) : null}
      </SectionCard>

      {detail ? (
        hasTeamSides ? (
          <div className="grid gap-4 lg:grid-cols-2">
            {orderedTeams.map((team) => (
              <TeamPanel
                key={`${team.side}-${team.title}`}
                title={team.title}
                sideLabel={team.sideLabel}
                side={team.side}
                participants={team.participants}
                focusPuuid={focusPuuid}
                isWinningTeam={team.isWinningTeam}
              />
            ))}
          </div>
        ) : (
          <div className="grid gap-3 lg:grid-cols-2">
            {detail.participants.map((participant) => (
              <ParticipantPanel
                key={`${participant.puuid}-${participant.championName}`}
                participant={participant}
                focusPuuid={focusPuuid}
              />
            ))}
          </div>
        )
      ) : null}
    </div>
  );
}

type TeamPanelProps = {
  title: string;
  sideLabel: string;
  side: "blue" | "red";
  participants: MatchParticipantResponse[];
  focusPuuid: string | null;
  isWinningTeam: boolean;
};

function TeamPanel({ title, sideLabel, side, participants, focusPuuid, isWinningTeam }: TeamPanelProps) {
  const accentClasses = isWinningTeam
    ? {
        accent: "bg-emerald-400",
        label: "text-emerald-300",
      }
    : {
        accent: "bg-rose-400",
        label: "text-rose-300",
      };

  return (
    <div className="border border-slate-800 bg-[#0b1220]">
      <div className="flex items-center justify-between gap-3 border-b border-slate-800 bg-[#0f1724] px-4 py-3">
        <div>
          <div className="flex items-center gap-2">
            <span className={`h-2 w-2 ${accentClasses.accent}`} />
            <p className={`text-[11px] font-semibold uppercase tracking-[0.22em] ${accentClasses.label}`}>{title}</p>
          </div>
          <p className="mt-1 text-sm text-slate-400">{sideLabel} · {participants.length}명</p>
        </div>
      </div>
      <div className="divide-y divide-slate-800">
          {participants.map((participant) => (
            <ParticipantPanel
              key={`${participant.puuid}-${participant.championName}`}
              participant={participant}
              focusPuuid={focusPuuid}
              compact
            />
          ))}
        </div>
    </div>
  );
}

type ParticipantPanelProps = {
  participant: MatchParticipantResponse;
  focusPuuid: string | null;
  compact?: boolean;
};

function ParticipantPanel({ participant, focusPuuid, compact = false }: ParticipantPanelProps) {
  const isFocus = participant.puuid === focusPuuid;
  const totalCs = participant.totalMinionsKilled + participant.neutralMinionsKilled;
  const focusPanelClass = compact
    ? isFocus
      ? "border border-tide/50 bg-[#122033] shadow-[inset_0_0_0_1px_rgba(107,203,255,0.18)]"
      : "border border-transparent bg-transparent"
    : isFocus
      ? "border-tide/50 bg-[#122033] ring-1 ring-tide/35"
      : "";

  return (
    <div className={compact ? focusPanelClass : ""}>
      <SectionCard
        title=""
        description=""
        className={
          compact ? "border-0 bg-transparent shadow-none" : focusPanelClass
        }
      >
        <div className="mb-3 flex items-start justify-between gap-3">
          <div className="flex min-w-0 items-start gap-3">
            <img
              src={getChampionPortraitUrl(participant.championKey, participant.championName)}
              alt={participant.championNameKo}
              className="h-12 w-12 shrink-0 border border-slate-700 bg-[#111827] object-cover"
            />
            <div className="min-w-0">
              <h3 className={`truncate text-base font-semibold tracking-[-0.04em] ${isFocus ? "text-tide" : "text-white"}`}>
                {participant.championNameKo}
              </h3>
              <p className="mt-1 truncate text-sm text-slate-400">{participant.summonerName}</p>
              {isFocus ? (
                <p className="mt-1 text-[11px] font-semibold uppercase tracking-[0.18em] text-tide">
                  내 플레이
                </p>
              ) : null}
              {participant.teamPositionKo ? <p className="mt-1 text-sm text-slate-400">{participant.teamPositionKo}</p> : null}
            </div>
          </div>
        </div>

          <div className="grid gap-2 sm:grid-cols-2 xl:grid-cols-4">
            <CompactStat title="KDA" value={`${participant.kills} / ${participant.deaths} / ${participant.assists}`} />
            <CompactStat title="딜량" value={participant.totalDamageDealtToChampions.toLocaleString()} />
            <CompactStat title="골드 / CS" value={`${participant.goldEarned.toLocaleString()} / ${totalCs.toLocaleString()}`} />
            <CompactStat
            title="시야 / 와드"
              value={`${participant.visionScore} / ${participant.wardsPlaced}-${participant.wardsKilled}`}
            />
          </div>

          {participant.interpretationTags.length > 0 ? (
            <div className="mt-3 flex flex-wrap gap-2">
              {participant.interpretationTags.map((tag) => (
                <InterpretationTagChip
                  key={`${participant.puuid}-${participant.championName}-${tag}`}
                  tag={tag}
                />
              ))}
            </div>
          ) : null}

          <div className="mt-3 space-y-3">
          <div className="border border-slate-800 bg-[#0b1220] p-3">
              <IconRow
                label="스펠"
                names={participant.summonerSpellNames}
                iconUrls={participant.summonerSpellIconUrls}
                descriptions={participant.summonerSpellDescriptions}
              />
            </div>
            <div className="border border-slate-800 bg-[#0b1220] p-3">
              <IconRow
                label="룬"
              names={[
                ...(participant.primaryRuneName ? [participant.primaryRuneName] : []),
                ...(participant.secondaryRuneName ? [participant.secondaryRuneName] : []),
              ]}
                iconUrls={[
                  ...(participant.primaryRuneIconUrl ? [participant.primaryRuneIconUrl] : []),
                  ...(participant.secondaryRuneIconUrl ? [participant.secondaryRuneIconUrl] : []),
                ]}
                descriptions={[
                  ...(participant.primaryRuneDescription ? [participant.primaryRuneDescription] : []),
                  ...(participant.secondaryRuneDescription ? [participant.secondaryRuneDescription] : []),
                ]}
              />
            </div>
            <div className="border border-slate-800 bg-[#0b1220] p-3">
              <IconRow
                label="아이템"
                names={participant.itemNames}
                iconUrls={participant.itemIconUrls}
                descriptions={participant.itemDescriptions}
              />
            </div>
          </div>
        </SectionCard>
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
    <div className="border border-slate-800 bg-[#0b1220] p-4">
      <div className="flex items-center gap-2 text-tide">
        {icon}
        <p className="text-xs font-semibold uppercase tracking-[0.16em]">{label}</p>
      </div>
      <p className="mt-2 text-lg font-semibold tracking-[-0.03em] text-white">{value}</p>
    </div>
  );
}

type InsightPanelProps = {
  title: string;
  accentClassName: string;
  dotClassName: string;
  items: string[];
};

function InsightPanel({ title, accentClassName, dotClassName, items }: InsightPanelProps) {
  return (
    <div className="border border-slate-800 bg-[#0b1220] p-4">
      <p className={`text-xs font-semibold uppercase tracking-[0.16em] ${accentClassName}`}>{title}</p>
      {items.length > 0 ? (
        <ul className="mt-3 space-y-2 text-sm text-slate-300">
          {items.map((item) => (
            <li key={item} className="flex gap-2">
              <span className={`mt-[7px] h-1.5 w-1.5 shrink-0 ${dotClassName}`} />
              <span>{item}</span>
            </li>
          ))}
        </ul>
      ) : (
        <p className="mt-3 text-sm text-slate-500">뚜렷하게 두드러진 내용은 없었습니다.</p>
      )}
    </div>
  );
}

type CompactStatProps = {
  title: string;
  value: string;
  strong?: boolean;
};

function CompactStat({ title, value, strong = false }: CompactStatProps) {
  return (
    <div className="border border-slate-800 bg-[#0b1220] p-3 text-sm text-slate-300">
      <p className="text-xs font-semibold uppercase tracking-[0.16em] text-tide">{title}</p>
      <p className={`mt-2 ${strong ? "text-base font-semibold text-white" : "text-sm text-slate-200"}`}>{value}</p>
    </div>
  );
}

type IconRowProps = {
  names: string[];
  iconUrls: string[];
  descriptions?: string[];
  label?: string;
};

function IconRow({ names, iconUrls, descriptions = [], label }: IconRowProps) {
  const items = names
    .map((name, index) => ({
      name,
      iconUrl: iconUrls[index],
      description: descriptions[index],
    }))
    .filter((item) => item.name);

  if (items.length === 0) {
    return label ? <p>{label} -</p> : <p>-</p>;
  }

  return (
    <div className="space-y-2 overflow-visible">
      {label ? <p className="text-xs font-semibold uppercase tracking-[0.16em] text-tide">{label}</p> : null}
      <div className="flex flex-wrap gap-2 overflow-visible">
        {items.map((item, index) => (
          <div
            key={`${label ?? "icon-row"}-${index}-${item.name}`}
            className="group relative overflow-visible"
          >
            <div className="flex h-10 w-10 items-center justify-center border border-slate-700 bg-[#111827]">
              {item.iconUrl ? (
                <img
                  src={item.iconUrl}
                  alt={item.name}
                  className="h-8 w-8 object-cover"
                />
              ) : (
                <span className="text-[10px] text-slate-400">-</span>
              )}
            </div>
            <div className="pointer-events-none absolute bottom-full left-1/2 z-30 mb-2 w-max max-w-[200px] -translate-x-1/2 translate-y-1 border border-slate-600 bg-[#020617] px-2.5 py-1.5 text-xs text-slate-100 opacity-0 shadow-[0_10px_30px_rgba(2,6,23,0.55)] transition duration-150 group-hover:translate-y-0 group-hover:opacity-100">
              <div className="relative">
                <p className="font-semibold text-white">{item.name}</p>
                {item.description ? <p className="mt-1 whitespace-pre-line text-slate-300">{item.description}</p> : null}
                <span className="absolute left-1/2 top-full h-2 w-2 -translate-x-1/2 -translate-y-1 rotate-45 border-b border-r border-slate-600 bg-[#020617]" />
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function sortParticipants(participants: MatchParticipantResponse[]): MatchParticipantResponse[] {
  return [...participants].sort((left, right) => {
    const leftOrder = left.teamPosition ? (POSITION_ORDER[left.teamPosition] ?? 99) : 99;
    const rightOrder = right.teamPosition ? (POSITION_ORDER[right.teamPosition] ?? 99) : 99;

    if (leftOrder !== rightOrder) {
      return leftOrder - rightOrder;
    }

    return left.championNameKo.localeCompare(right.championNameKo, "ko");
  });
}
