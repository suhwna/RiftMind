import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ChevronDown, ChevronUp, Filter, RefreshCw } from "lucide-react";
import type { ReactNode } from "react";
import { useEffect, useMemo, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import { MatchSummaryCard } from "../components/MatchSummaryCard";
import { SectionCard } from "../components/SectionCard";
import { getRecentMatches, getSearchFilterOptions, getSummonerByPuuid, searchMatches, syncSummoner } from "../lib/api";
import type { SearchMatchResponse, SummonerMatchSummaryResponse } from "../types/api";

const INITIAL_MATCH_COUNT = 8;
const MATCH_LOAD_STEP = 4;
const MAX_MATCH_COUNT = 20;
const SEARCH_PAGE_SIZE = 8;

type DashboardFilterState = {
  championNames: string[];
  teamPositions: string[];
  queueIds: string[];
  win: string;
  minKda: string;
};

const initialFilterState: DashboardFilterState = {
  championNames: [],
  teamPositions: [],
  queueIds: [],
  win: "",
  minKda: "",
};

export function SummonerDashboardPage() {
  const { puuid = "" } = useParams();
  const queryClient = useQueryClient();
  const loadMoreRef = useRef<HTMLDivElement | null>(null);
  const [requestedCount, setRequestedCount] = useState(INITIAL_MATCH_COUNT);
  const [filters, setFilters] = useState<DashboardFilterState>(initialFilterState);
  const [isFilterOpen, setIsFilterOpen] = useState(false);

  const profileQuery = useQuery({
    queryKey: ["summoner-profile", puuid],
    queryFn: () => getSummonerByPuuid(puuid),
    enabled: Boolean(puuid),
  });

  const matchesQuery = useQuery({
    queryKey: ["recent-matches", puuid, requestedCount],
    queryFn: () => getRecentMatches(puuid, requestedCount),
    enabled: Boolean(puuid),
  });

  const filterOptionsQuery = useQuery({
    queryKey: ["search-filter-options", puuid],
    queryFn: () => getSearchFilterOptions(puuid),
    enabled: Boolean(puuid),
  });

  const hasActiveFilters = useMemo(
    () =>
      filters.championNames.length > 0 ||
      filters.teamPositions.length > 0 ||
      filters.queueIds.length > 0 ||
      filters.win.trim().length > 0 ||
      filters.minKda.trim().length > 0,
    [filters],
  );

  const searchQueryString = useMemo(() => {
    const nextSearchParams = new URLSearchParams();
    nextSearchParams.set("puuid", puuid);

    filters.championNames.forEach((value) => nextSearchParams.append("championNames", value));
    filters.teamPositions.forEach((value) => nextSearchParams.append("teamPositions", value));
    filters.queueIds.forEach((value) => nextSearchParams.append("queueIds", value));

    if (filters.win.trim()) {
      nextSearchParams.set("win", filters.win.trim());
    }

    if (filters.minKda.trim()) {
      nextSearchParams.set("minKda", filters.minKda.trim());
    }

    return nextSearchParams.toString();
  }, [filters, puuid]);

  const filteredMatchesQuery = useInfiniteQuery({
    queryKey: ["dashboard-filtered-matches", searchQueryString],
    queryFn: ({ pageParam }) => {
      const nextSearchParams = new URLSearchParams(searchQueryString);
      nextSearchParams.set("page", String(pageParam));
      nextSearchParams.set("size", String(SEARCH_PAGE_SIZE));
      return searchMatches(nextSearchParams.toString());
    },
    enabled: Boolean(puuid) && hasActiveFilters,
    initialPageParam: 0,
    getNextPageParam: (lastPage, allPages) => {
      const loadedCount = allPages.flatMap((page) => page.matches).length;
      return loadedCount < lastPage.total ? allPages.length : undefined;
    },
  });

  const matches = matchesQuery.data?.matches ?? [];
  const filteredMatches = filteredMatchesQuery.data?.pages.flatMap((page) => page.matches) ?? [];
  const displayedMatches = hasActiveFilters ? filteredMatches.map(toSummaryMatch) : matches;
  const displayedTotal = hasActiveFilters ? filteredMatchesQuery.data?.pages[0]?.total ?? 0 : matches.length;
  const wins = displayedMatches.filter((match) => match.win).length;
  const hasMoreMatches = hasActiveFilters
    ? Boolean(filteredMatchesQuery.hasNextPage)
    : requestedCount < MAX_MATCH_COUNT && matches.length >= requestedCount;
  const isLoadingMatches = hasActiveFilters ? filteredMatchesQuery.isLoading : matchesQuery.isLoading;
  const isFetchingMoreMatches = hasActiveFilters
    ? filteredMatchesQuery.isFetchingNextPage
    : matchesQuery.isFetching && !matchesQuery.isLoading;
  const matchesError = hasActiveFilters ? filteredMatchesQuery.error : matchesQuery.error;

  const syncMutation = useMutation({
    mutationFn: syncSummoner,
    onSuccess: async () => {
      setRequestedCount(INITIAL_MATCH_COUNT);
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["summoner-profile", puuid] }),
        queryClient.invalidateQueries({ queryKey: ["recent-matches", puuid] }),
        queryClient.invalidateQueries({ queryKey: ["search-filter-options", puuid] }),
        queryClient.invalidateQueries({ queryKey: ["dashboard-filtered-matches"] }),
      ]);
    },
  });

  useEffect(() => {
    if (!puuid) {
      return;
    }
    setRequestedCount(INITIAL_MATCH_COUNT);
    setFilters(initialFilterState);
    setIsFilterOpen(false);
  }, [puuid]);

  useEffect(() => {
    if (hasActiveFilters) {
      setIsFilterOpen(true);
    }
  }, [hasActiveFilters]);

  useEffect(() => {
    if (!loadMoreRef.current || !hasMoreMatches || isFetchingMoreMatches) {
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        const [entry] = entries;

        if (!entry?.isIntersecting) {
          return;
        }

        if (hasActiveFilters) {
          void filteredMatchesQuery.fetchNextPage();
          return;
        }

        setRequestedCount((current) => Math.min(MAX_MATCH_COUNT, current + MATCH_LOAD_STEP));
      },
      {
        rootMargin: "220px 0px",
      },
    );

    observer.observe(loadMoreRef.current);

    return () => observer.disconnect();
  }, [filteredMatchesQuery, hasActiveFilters, hasMoreMatches, isFetchingMoreMatches]);

  const handleRefresh = () => {
    if (!profileQuery.data) {
      return;
    }

    syncMutation.mutate({
      gameName: profileQuery.data.gameName,
      tagLine: profileQuery.data.tagLine,
      matchCount: MAX_MATCH_COUNT,
    });
  };

  const toggleSelection = (field: "championNames" | "teamPositions" | "queueIds", value: string) => {
    setFilters((current) => {
      const values = current[field];
      const nextValues = values.includes(value) ? values.filter((item) => item !== value) : [...values, value];

      return {
        ...current,
        [field]: nextValues,
      };
    });
  };

  const clearFilters = () => {
    setFilters(initialFilterState);
  };

  return (
    <div className="space-y-6">
      <SectionCard
        title=""
        action={
          <button
            type="button"
            onClick={handleRefresh}
            disabled={!profileQuery.data || syncMutation.isPending}
            className="inline-flex items-center gap-2 rounded-full bg-tide px-4 py-2 text-sm font-medium text-ink transition hover:bg-brass disabled:cursor-not-allowed disabled:opacity-60"
          >
            <RefreshCw className={`h-4 w-4 ${syncMutation.isPending ? "animate-spin" : ""}`} />
            {syncMutation.isPending ? "동기화 중..." : "동기화"}
          </button>
        }
      >
        <div className="grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
          <div className="flex items-center gap-5">
            <div className="h-24 w-24 overflow-hidden rounded-[22px] border border-white/10 bg-[#0b1020]">
              {profileQuery.data?.profileIconId ? (
                <img
                  src={getProfileIconUrl(profileQuery.data.profileIconId)}
                  alt={`${profileQuery.data.gameName} 프로필 아이콘`}
                  className="h-full w-full object-cover"
                />
              ) : null}
            </div>
            <div>
              <p className="text-[11px] font-semibold uppercase tracking-[0.22em] text-tide">Summoner report</p>
              <h2 className="mt-2 text-3xl font-semibold tracking-[-0.05em] text-white">
                {profileQuery.data ? `${profileQuery.data.gameName}#${profileQuery.data.tagLine}` : "소환사 대시보드"}
              </h2>
              <p className="mt-3 text-sm text-slate-400">
                레벨 {profileQuery.data?.summonerLevel ?? "-"} · 최근 {matches.length}경기 기준
              </p>
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-3 lg:grid-cols-1">
            <StatPanel label="불러온 경기" value={String(matches.length)} />
            <StatPanel label="최근 승리" value={String(wins)} />
            <StatPanel label="마지막 동기화" value={profileQuery.data ? new Date(profileQuery.data.lastSyncedAt).toLocaleString("ko-KR") : "-"} compact />
          </div>
        </div>

        {syncMutation.isError ? (
          <p className="mt-5 rounded-2xl bg-rose-950/40 px-4 py-3 text-sm text-rose-200">
            {(syncMutation.error as Error).message}
          </p>
        ) : null}
      </SectionCard>

      <SectionCard title="경기 목록" description={hasActiveFilters ? `${displayedTotal}경기 필터링 중` : "최근 경기를 시간순으로 확인합니다."}>
        <div className="mb-6 rounded-[20px] border border-white/8 bg-[#0b1020] p-4">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-2xl border border-white/8 bg-[#131a28] text-tide">
                <Filter className="h-4 w-4" />
              </div>
              <div>
                <p className="text-sm font-semibold text-white">필터</p>
                <p className="mt-1 text-sm text-slate-400">
                  {hasActiveFilters ? "조건 적용 중" : "필요할 때만 경기 조건을 좁혀보세요."}
                </p>
              </div>
            </div>

            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => setIsFilterOpen((current) => !current)}
                className="inline-flex items-center gap-2 rounded-full border border-white/8 bg-[#131a28] px-4 py-2 text-sm font-medium text-slate-300 transition hover:bg-white/5"
              >
                {isFilterOpen ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
                {isFilterOpen ? "필터 접기" : "필터 보기"}
              </button>
              <button
                type="button"
                onClick={clearFilters}
                disabled={!hasActiveFilters}
                className="rounded-full border border-white/8 bg-[#131a28] px-4 py-2 text-sm font-medium text-slate-300 transition hover:bg-white/5 disabled:cursor-not-allowed disabled:text-slate-500"
              >
                초기화
              </button>
            </div>
          </div>

          {hasActiveFilters ? (
            <div className="mt-4 flex flex-wrap gap-2">
              {filters.championNames.map((value) => {
                const champion = filterOptionsQuery.data?.champions.find((item) => item.championName === value);
                return (
                  <ActiveChip key={value} onRemove={() => toggleSelection("championNames", value)}>
                    {champion?.championNameKo ?? value}
                  </ActiveChip>
                );
              })}
              {filters.teamPositions.map((value) => {
                const position = filterOptionsQuery.data?.positions.find((item) => item.teamPosition === value);
                return (
                  <ActiveChip key={value} onRemove={() => toggleSelection("teamPositions", value)}>
                    {position?.teamPositionKo ?? value}
                  </ActiveChip>
                );
              })}
              {filters.queueIds.map((value) => {
                const mode = filterOptionsQuery.data?.modes.find((item) => String(item.queueId) === value);
                return (
                  <ActiveChip key={value} onRemove={() => toggleSelection("queueIds", value)}>
                    {mode?.queueNameKo ?? value}
                  </ActiveChip>
                );
              })}
              {filters.win ? (
                <ActiveChip onRemove={() => setFilters((current) => ({ ...current, win: "" }))}>
                  {filters.win === "true" ? "승리" : "패배"}
                </ActiveChip>
              ) : null}
              {filters.minKda ? (
                <ActiveChip onRemove={() => setFilters((current) => ({ ...current, minKda: "" }))}>
                  KDA {filters.minKda}+
                </ActiveChip>
              ) : null}
            </div>
          ) : null}

          {isFilterOpen ? (
            <div className="mt-5 space-y-4">
              <FilterSection title="챔피언">
                {filterOptionsQuery.data?.champions.map((champion) => (
                  <ToggleChip
                    key={champion.championName}
                    selected={filters.championNames.includes(champion.championName)}
                    onClick={() => toggleSelection("championNames", champion.championName)}
                  >
                    {champion.championNameKo}
                  </ToggleChip>
                ))}
              </FilterSection>

              <div className="grid gap-4 lg:grid-cols-2">
                <FilterSection title="포지션">
                  {filterOptionsQuery.data?.positions.map((position) => (
                    <ToggleChip
                      key={position.teamPosition}
                      selected={filters.teamPositions.includes(position.teamPosition)}
                      onClick={() => toggleSelection("teamPositions", position.teamPosition)}
                    >
                      {position.teamPositionKo}
                    </ToggleChip>
                  ))}
                </FilterSection>

                <FilterSection title="게임 모드">
                  {filterOptionsQuery.data?.modes.map((mode) => (
                    <ToggleChip
                      key={`${mode.queueId}-${mode.queueNameKo}`}
                      selected={filters.queueIds.includes(String(mode.queueId))}
                      onClick={() => toggleSelection("queueIds", String(mode.queueId))}
                    >
                      {mode.queueNameKo}
                    </ToggleChip>
                  ))}
                </FilterSection>
              </div>

              <div className="grid gap-4 md:grid-cols-2">
                <label className="block">
                  <span className="mb-2 block text-sm font-medium text-slate-300">승패</span>
                  <select
                    value={filters.win}
                    onChange={(event) => setFilters((current) => ({ ...current, win: event.target.value }))}
                    className="w-full rounded-[18px] border border-white/8 bg-[#131a28] px-4 py-3 text-white outline-none transition focus:border-tide"
                  >
                    <option value="">전체</option>
                    <option value="true">승리</option>
                    <option value="false">패배</option>
                  </select>
                </label>

                <label className="block">
                  <span className="mb-2 block text-sm font-medium text-slate-300">최소 KDA</span>
                  <input
                    value={filters.minKda}
                    onChange={(event) => setFilters((current) => ({ ...current, minKda: event.target.value }))}
                    placeholder="예: 3"
                    className="w-full rounded-[18px] border border-white/8 bg-[#131a28] px-4 py-3 text-white outline-none transition placeholder:text-slate-500 focus:border-tide"
                  />
                </label>
              </div>
            </div>
          ) : null}
        </div>

        {filterOptionsQuery.isLoading ? <p className="mb-4 text-sm text-slate-400">필터를 준비하는 중입니다...</p> : null}
        {filterOptionsQuery.isError ? (
          <p className="mb-4 rounded-2xl bg-rose-950/40 px-4 py-3 text-sm text-rose-200">
            {(filterOptionsQuery.error as Error).message}
          </p>
        ) : null}
        {isLoadingMatches ? <p className="text-sm text-slate-400">경기를 불러오는 중입니다...</p> : null}
        {matchesError ? (
          <p className="rounded-2xl bg-rose-950/40 px-4 py-3 text-sm text-rose-200">
            {(matchesError as Error).message}
          </p>
        ) : null}

        {displayedMatches.length > 0 ? (
          <>
            <div className="grid gap-4 lg:grid-cols-2">
              {displayedMatches.map((match) => (
                <MatchSummaryCard key={match.matchId} puuid={puuid} match={match} />
              ))}
            </div>
            <div ref={loadMoreRef} className="mt-4 min-h-8">
              {isFetchingMoreMatches ? <p className="text-center text-sm text-slate-400">다음 경기를 불러오는 중입니다...</p> : null}
              {!hasMoreMatches && displayedMatches.length > 0 ? (
                <p className="text-center text-sm text-slate-500">
                  {hasActiveFilters ? `조건에 맞는 ${displayedTotal}경기를 모두 표시했습니다.` : "현재 불러올 수 있는 최근 경기까지 모두 표시했습니다."}
                </p>
              ) : null}
            </div>
          </>
        ) : null}

        {!isLoadingMatches && !matchesError && displayedMatches.length === 0 ? (
          <p className="rounded-[20px] border border-white/8 bg-[#0b1020] px-4 py-6 text-center text-sm text-slate-400">
            {hasActiveFilters ? "조건에 맞는 경기가 없습니다." : "최근 경기 데이터가 없습니다."}
          </p>
        ) : null}
      </SectionCard>
    </div>
  );
}

type StatPanelProps = {
  label: string;
  value: string;
  compact?: boolean;
};

function StatPanel({ label, value, compact = false }: StatPanelProps) {
  return (
    <div className="rounded-[18px] border border-white/8 bg-[#0b1020] p-4">
      <p className="text-xs font-semibold uppercase tracking-[0.16em] text-slate-500">{label}</p>
      <p className={`mt-3 font-semibold tracking-[-0.03em] text-white ${compact ? "text-sm leading-6" : "text-2xl"}`}>{value}</p>
    </div>
  );
}

type FilterSectionProps = {
  title: string;
  children: ReactNode;
};

function FilterSection({ title, children }: FilterSectionProps) {
  return (
    <div className="space-y-3">
      <span className="block text-sm font-medium text-slate-300">{title}</span>
      <div className="flex flex-wrap gap-2 rounded-[18px] border border-white/8 bg-[#131a28] p-3">{children}</div>
    </div>
  );
}

type ToggleChipProps = {
  selected: boolean;
  onClick: () => void;
  children: string;
};

function ToggleChip({ selected, onClick, children }: ToggleChipProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={[
        "rounded-full border px-3 py-2 text-sm font-medium transition",
        selected ? "border-tide bg-tide text-ink" : "border-white/8 bg-[#0b1020] text-slate-300 hover:bg-white/5",
      ].join(" ")}
    >
      {children}
    </button>
  );
}

type ActiveChipProps = {
  children: ReactNode;
  onRemove: () => void;
};

function ActiveChip({ children, onRemove }: ActiveChipProps) {
  return (
    <button
      type="button"
      onClick={onRemove}
      className="rounded-full border border-tide/40 bg-tide/12 px-3 py-1.5 text-sm font-medium text-tide transition hover:bg-tide/18"
    >
      {children}
    </button>
  );
}

function toSummaryMatch(match: SearchMatchResponse): SummonerMatchSummaryResponse {
  return {
    matchId: match.matchId,
    gameCreation: match.gameCreation,
    queueId: match.queueId,
    queueNameKo: match.queueNameKo,
    gameMode: match.gameMode,
    championName: match.championName,
    championKey: match.championKey,
    championNameKo: match.championNameKo,
    teamPosition: match.teamPosition,
    teamPositionKo: match.teamPositionKo,
    kills: match.kills,
    deaths: match.deaths,
    assists: match.assists,
    win: match.win,
  };
}

function getProfileIconUrl(profileIconId: number): string {
  return `https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/profile-icons/${profileIconId}.jpg`;
}
