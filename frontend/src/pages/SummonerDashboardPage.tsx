import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ChevronDown, ChevronUp, Filter, RefreshCw } from "lucide-react";
import type { ReactNode } from "react";
import { useEffect, useMemo, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import { MatchSummaryCard } from "../components/MatchSummaryCard";
import { SectionCard } from "../components/SectionCard";
import { getChampionPortraitUrl } from "../lib/assets";
import { getRecentMatches, getSearchFilterOptions, getSearchOverview, getSummonerByPuuid, searchMatches, syncSummoner } from "../lib/api";
import type {
  SearchMatchResponse,
  SearchOverviewChampionAnalysisResponse,
  SearchOverviewItemResponse,
  SearchOverviewMatchupResponse,
  SummonerMatchSummaryResponse,
} from "../types/api";

const INITIAL_MATCH_COUNT = 8;
const MATCH_LOAD_STEP = 4;
const MAX_MATCH_COUNT = 20;
const SEARCH_PAGE_SIZE = 8;
const OVERVIEW_MATCH_COUNT = 100;

type DashboardFilterState = {
  championNames: string[];
  teamPositions: string[];
  queueIds: string[];
  win: string;
};

const initialFilterState: DashboardFilterState = {
  championNames: [],
  teamPositions: [],
  queueIds: [],
  win: "",
};

export function SummonerDashboardPage() {
  const { puuid = "" } = useParams();
  const queryClient = useQueryClient();
  const loadMoreRef = useRef<HTMLDivElement | null>(null);
  const [requestedCount, setRequestedCount] = useState(INITIAL_MATCH_COUNT);
  const [filters, setFilters] = useState<DashboardFilterState>(initialFilterState);
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [selectedChampionAnalysis, setSelectedChampionAnalysis] = useState<string>("");

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

  const overviewQuery = useQuery({
    queryKey: ["search-overview", puuid],
    queryFn: () => getSearchOverview(puuid, OVERVIEW_MATCH_COUNT),
    enabled: Boolean(puuid),
  });

  const hasActiveFilters = useMemo(
      () =>
        filters.championNames.length > 0 ||
        filters.teamPositions.length > 0 ||
        filters.queueIds.length > 0 ||
        filters.win.trim().length > 0,
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
  const recentWinRate = matches.length > 0 ? Math.round((matches.filter((match) => match.win).length / matches.length) * 100) : 0;
  const averageKda = matches.length > 0
    ? matches.reduce((sum, match) => {
        const kda = match.deaths === 0 ? match.kills + match.assists : (match.kills + match.assists) / match.deaths;
        return sum + kda;
      }, 0) / matches.length
    : 0;
  const mostPlayedChampion = useMemo(() => {
    const counts = new Map<string, { nameKo: string; count: number }>();
    for (const match of matches) {
      const current = counts.get(match.championName) ?? { nameKo: match.championNameKo, count: 0 };
      current.count += 1;
      counts.set(match.championName, current);
    }

    return Array.from(counts.values()).sort((left, right) => right.count - left.count)[0] ?? null;
  }, [matches]);
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
        queryClient.invalidateQueries({ queryKey: ["search-overview", puuid] }),
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
    const firstChampion = overviewQuery.data?.championAnalyses[0]?.championName;
    if (!firstChampion) {
      return;
    }

    setSelectedChampionAnalysis((current) => {
      if (current && overviewQuery.data?.championAnalyses.some((analysis) => analysis.championName === current)) {
        return current;
      }
      return firstChampion;
    });
  }, [overviewQuery.data]);

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

  const currentChampionAnalysis = overviewQuery.data?.championAnalyses.find(
    (analysis) => analysis.championName === selectedChampionAnalysis,
  ) ?? overviewQuery.data?.championAnalyses[0] ?? null;

  return (
      <div className="space-y-6">
        <SectionCard title="">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-stretch">
            <div className="min-w-0 flex-1 border border-slate-800 bg-[#0b1220]">
              <div className="flex items-center justify-between gap-3 border-b border-slate-800 px-4 py-2.5">
                <p className="text-[11px] font-semibold uppercase tracking-[0.22em] text-tide">Summoner profile</p>
                <button
                  type="button"
                  onClick={handleRefresh}
                  disabled={!profileQuery.data || syncMutation.isPending}
                  className="inline-flex items-center gap-2 border border-tide/40 bg-[#171d27] px-3 py-2 text-sm font-medium text-tide transition hover:bg-[#1d2531] disabled:cursor-not-allowed disabled:opacity-60"
                >
                  <RefreshCw className={`h-4 w-4 ${syncMutation.isPending ? "animate-spin" : ""}`} />
                  {syncMutation.isPending ? "동기화 중..." : "동기화"}
                </button>
              </div>
              <div className="grid gap-4 px-4 py-3 md:grid-cols-[80px_1fr] md:items-center">
                <div className="h-20 w-20 overflow-hidden border border-slate-700 bg-[#0b1220]">
                  {profileQuery.data?.profileIconId ? (
                    <img
                      src={getProfileIconUrl(profileQuery.data.profileIconId)}
                      alt={`${profileQuery.data.gameName} 프로필 아이콘`}
                      className="h-full w-full object-cover"
                    />
                  ) : null}
                </div>
                <div className="min-w-0 space-y-3">
                  <div>
                    <h2 className="text-[1.7rem] font-semibold tracking-[-0.05em] text-white">
                      {profileQuery.data ? `${profileQuery.data.gameName}#${profileQuery.data.tagLine}` : "소환사 대시보드"}
                    </h2>
                  </div>
                  <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-slate-400">
                    <span>레벨 {profileQuery.data?.summonerLevel ?? "-"}</span>
                    <span className="hidden h-3 w-px bg-slate-700 sm:block" />
                    <span>최근 {matches.length}경기 기준</span>
                  </div>
                  <p className="text-xs text-slate-500">
                    마지막 동기화 {profileQuery.data ? new Date(profileQuery.data.lastSyncedAt).toLocaleString("ko-KR") : "-"}
                  </p>
                </div>
              </div>
              {overviewQuery.data?.insights.length ? (
                <div className="border-t border-slate-800 px-4 py-4">
                  <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-tide">플레이 인사이트</p>
                  <div className="mt-3 space-y-2">
                    {overviewQuery.data.insights.map((insight) => (
                      <p key={insight} className="text-sm leading-6 text-slate-300">
                        {insight}
                      </p>
                    ))}
                  </div>
                </div>
              ) : null}
            </div>

            <div className="w-full border border-slate-800 bg-[#0b1220] lg:h-full lg:w-[360px] lg:flex-none">
              <div className="border-b border-slate-800 px-4 py-2.5">
                <p className="text-[11px] font-semibold uppercase tracking-[0.22em] text-tide">플레이 요약</p>
              </div>
              {overviewQuery.isLoading ? <p className="px-4 py-4 text-sm text-slate-400">플레이 요약을 불러오는 중입니다...</p> : null}
              {overviewQuery.isError ? (
                <p className="px-4 py-4 text-sm text-rose-200">{(overviewQuery.error as Error).message}</p>
              ) : null}
              {overviewQuery.data ? (
                <>
                  <div className="divide-y divide-slate-800">
                    <StatRow label="분석 표본" value={`최근 ${overviewQuery.data.analyzedMatchCount}판`} />
                    <StatRow label="최근 승률" value={`${overviewQuery.data.winRate}%`} />
                    <StatRow label="평균 KDA" value={overviewQuery.data.averageKda > 0 ? overviewQuery.data.averageKda.toFixed(2) : "-"} />
                    <StatRow label="평균 딜량" value={overviewQuery.data.averageDamage > 0 ? overviewQuery.data.averageDamage.toLocaleString("ko-KR") : "-"} />
                    <StatRow label="평균 CS" value={overviewQuery.data.averageCs > 0 ? overviewQuery.data.averageCs.toLocaleString("ko-KR") : "-"} />
                    <ChampionListRow
                      label="많이 한 챔피언"
                      champions={overviewQuery.data.topPlayedChampions}
                    />
                    <StatRow
                      label="가장 성과 좋은 챔피언"
                      value={overviewQuery.data.bestChampion
                        ? `${overviewQuery.data.bestChampion.championNameKo} · 승률 ${overviewQuery.data.bestChampion.winRate}%`
                        : "-"}
                    />
                  </div>
                  <div className="border-t border-slate-800 px-4 py-4">
                    <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-tide">
                      최근 {overviewQuery.data.recentTrend.matchCount}판 추세
                    </p>
                    <div className="mt-3 space-y-2">
                      {overviewQuery.data.recentTrend.insights.map((insight) => (
                        <p key={insight} className="text-sm leading-6 text-slate-300">
                          {insight}
                        </p>
                      ))}
                    </div>
                  </div>
                </>
              ) : !overviewQuery.isLoading && !overviewQuery.isError ? (
                <div className="divide-y divide-slate-800">
                  <StatRow label="최근 승률" value={`${recentWinRate}%`} />
                  <StatRow label="평균 KDA" value={averageKda > 0 ? averageKda.toFixed(2) : "-"} />
                  <StatRow
                    label="많이 한 챔피언"
                    value={mostPlayedChampion ? `${mostPlayedChampion.nameKo} · ${mostPlayedChampion.count}판` : "-"}
                  />
                </div>
              ) : null}
            </div>
          </div>

        {syncMutation.isError ? (
          <p className="mt-5 rounded-2xl bg-rose-950/40 px-4 py-3 text-sm text-rose-200">
            {(syncMutation.error as Error).message}
          </p>
        ) : null}
      </SectionCard>

      <SectionCard
        title="챔피언별 분석"
        description={`최근 ${OVERVIEW_MATCH_COUNT}판에서 많이 플레이한 챔피언 기준으로 성과, 상대, 빌드를 정리했습니다.`}
      >
        {overviewQuery.isLoading ? <p className="text-sm text-slate-400">챔피언별 분석을 불러오는 중입니다...</p> : null}
        {overviewQuery.isError ? (
          <p className="rounded-2xl bg-rose-950/40 px-4 py-3 text-sm text-rose-200">
            {(overviewQuery.error as Error).message}
          </p>
        ) : null}
        {overviewQuery.data?.championAnalyses.length ? (
          <div className="space-y-4">
            <div className="flex flex-wrap gap-2">
              {overviewQuery.data.championAnalyses.map((analysis) => (
                <button
                  key={analysis.championName}
                  type="button"
                  onClick={() => setSelectedChampionAnalysis(analysis.championName)}
                  className={[
                    "inline-flex items-center gap-2 border px-3 py-2 text-sm font-medium transition",
                    currentChampionAnalysis?.championName === analysis.championName
                      ? "border-tide bg-tide text-ink"
                      : "border-slate-700 bg-[#0b1220] text-slate-300 hover:bg-[#182233]",
                  ].join(" ")}
                >
                  <img
                    src={getChampionPortraitUrl(analysis.championKey, analysis.championName)}
                    alt={analysis.championNameKo}
                    className="h-5 w-5 border border-slate-700 object-cover"
                  />
                  {analysis.championNameKo}
                </button>
              ))}
            </div>
            {currentChampionAnalysis ? <ChampionAnalysisCard analysis={currentChampionAnalysis} /> : null}
          </div>
        ) : null}
        {!overviewQuery.isLoading && !overviewQuery.isError && !overviewQuery.data?.championAnalyses.length ? (
          <p className="border border-slate-800 bg-[#0b1220] px-4 py-6 text-center text-sm text-slate-400">
            챔피언별 분석에 필요한 경기 표본이 부족합니다.
          </p>
        ) : null}
      </SectionCard>

      <SectionCard title="경기 목록" description={hasActiveFilters ? `${displayedTotal}경기 필터링 중` : "최근 경기를 시간순으로 확인합니다."}>
        <div className="mb-6 border border-slate-800 bg-[#0b1220]">
          <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-800 px-4 py-3">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center border border-slate-700 bg-[#111827] text-tide">
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
                className="inline-flex items-center gap-2 border border-slate-700 bg-[#111827] px-4 py-2 text-sm font-medium text-slate-300 transition hover:bg-[#182233]"
              >
                {isFilterOpen ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
                {isFilterOpen ? "필터 접기" : "필터 보기"}
              </button>
              <button
                type="button"
                onClick={clearFilters}
                disabled={!hasActiveFilters}
                className="border border-slate-700 bg-[#111827] px-4 py-2 text-sm font-medium text-slate-300 transition hover:bg-[#182233] disabled:cursor-not-allowed disabled:text-slate-500"
              >
                초기화
              </button>
            </div>
          </div>

          {hasActiveFilters ? (
            <div className="border-b border-slate-800 px-4 py-3">
              <div className="flex flex-wrap gap-2">
              {filters.championNames.map((value) => {
                const champion = filterOptionsQuery.data?.champions.find((item) => item.championName === value);
                return (
                  <ActiveChip
                    key={value}
                    onRemove={() => toggleSelection("championNames", value)}
                    icon={champion?.championKey ? (
                      <img
                        src={getChampionPortraitUrl(champion.championKey, champion.championName)}
                        alt={champion.championNameKo}
                        className="h-4 w-4 object-cover"
                      />
                    ) : null}
                  >
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
                </div>
              </div>
            ) : null}

          {isFilterOpen ? (
            <div className="space-y-4 px-4 py-4">
              <FilterSection title="챔피언">
                {filterOptionsQuery.data?.champions.map((champion) => (
                  <ToggleChip
                    key={champion.championName}
                    selected={filters.championNames.includes(champion.championName)}
                    onClick={() => toggleSelection("championNames", champion.championName)}
                    icon={
                      <img
                        src={getChampionPortraitUrl(champion.championKey, champion.championName)}
                        alt={champion.championNameKo}
                        className="h-4 w-4 object-cover"
                      />
                    }
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

                <FilterSection title="승패">
                  <ToggleChip
                    selected={filters.win === "true"}
                    onClick={() =>
                      setFilters((current) => ({
                        ...current,
                        win: current.win === "true" ? "" : "true",
                      }))
                    }
                  >
                    승리
                  </ToggleChip>
                  <ToggleChip
                    selected={filters.win === "false"}
                    onClick={() =>
                      setFilters((current) => ({
                        ...current,
                        win: current.win === "false" ? "" : "false",
                      }))
                    }
                  >
                    패배
                  </ToggleChip>
                </FilterSection>
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
            <div className="space-y-3">
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
          <p className="border border-slate-800 bg-[#0b1220] px-4 py-6 text-center text-sm text-slate-400">
            {hasActiveFilters ? "조건에 맞는 경기가 없습니다." : "최근 경기 데이터가 없습니다."}
          </p>
        ) : null}
      </SectionCard>
    </div>
  );
}

type StatRowProps = {
  label: string;
  value: string;
};

function StatRow({ label, value }: StatRowProps) {
  return (
    <div className="flex items-center justify-between gap-4 px-4 py-4">
      <p className="text-xs font-semibold uppercase tracking-[0.16em] text-slate-500">{label}</p>
      <p className="text-right text-sm font-semibold text-white">{value}</p>
    </div>
  );
}

type ChampionListRowProps = {
  label: string;
  champions: {
    championName: string;
    championKey: string;
    championNameKo: string;
    matchCount: number;
  }[];
};

function ChampionListRow({ label, champions }: ChampionListRowProps) {
  return (
    <div className="px-4 py-4">
      <p className="text-xs font-semibold uppercase tracking-[0.16em] text-slate-500">{label}</p>
      {champions.length > 0 ? (
        <div className="mt-3 flex flex-wrap gap-2">
          {champions.map((champion) => (
            <div
              key={champion.championName}
              className="inline-flex items-center gap-2 border border-slate-700 bg-[#111827] px-2.5 py-2 text-sm text-slate-200"
            >
              <img
                src={getChampionPortraitUrl(champion.championKey, champion.championName)}
                alt={champion.championNameKo}
                className="h-5 w-5 object-cover"
              />
              <span>{champion.championNameKo}</span>
              <span className="text-slate-500">{champion.matchCount}판</span>
            </div>
          ))}
        </div>
      ) : (
        <p className="mt-2 text-sm font-semibold text-white">-</p>
      )}
    </div>
  );
}

type ChampionAnalysisCardProps = {
  analysis: SearchOverviewChampionAnalysisResponse;
};

function ChampionAnalysisCard({ analysis }: ChampionAnalysisCardProps) {
  return (
    <div className="border border-slate-800 bg-[#0b1220]">
      <div className="flex items-center justify-between gap-4 border-b border-slate-800 px-4 py-3">
        <div className="flex min-w-0 items-center gap-3">
          <img
            src={getChampionPortraitUrl(analysis.championKey, analysis.championName)}
            alt={analysis.championNameKo}
            className="h-11 w-11 border border-slate-700 object-cover"
          />
          <div className="min-w-0">
            <p className="text-base font-semibold text-white">{analysis.championNameKo}</p>
            <p className="mt-1 text-xs text-slate-500">
              {(analysis.primaryPositionKo ? `${analysis.primaryPositionKo} · ` : "") + `${analysis.matchCount}판 · 승률 ${analysis.winRate}% · KDA ${analysis.averageKda.toFixed(2)}`}
            </p>
          </div>
        </div>
      </div>

      <div className="grid gap-px border-b border-slate-800 bg-slate-800 sm:grid-cols-4">
        <CompactOverviewStat label="평균 딜량" value={analysis.averageDamage.toLocaleString("ko-KR")} />
        <CompactOverviewStat label="평균 골드" value={analysis.averageGold.toLocaleString("ko-KR")} />
        <CompactOverviewStat label="평균 CS" value={analysis.averageCs.toLocaleString("ko-KR")} />
        <CompactOverviewStat label="평균 시야" value={analysis.averageVisionScore.toLocaleString("ko-KR")} />
      </div>

      <div className="space-y-4 px-4 py-4">
        <AnalysisListSection title="자주 만난 상대">
          {analysis.frequentOpponents.length > 0 ? (
            analysis.frequentOpponents.map((opponent) => (
              <MatchupRow key={`frequent-${analysis.championName}-${opponent.championName}`} matchup={opponent} />
            ))
          ) : (
            <EmptyLine />
          )}
        </AnalysisListSection>

        <AnalysisListSection title="까다로운 상대">
          {analysis.toughestOpponents.length > 0 ? (
            analysis.toughestOpponents.map((opponent) => (
              <MatchupRow key={`tough-${analysis.championName}-${opponent.championName}`} matchup={opponent} />
            ))
          ) : (
            <EmptyLine />
          )}
        </AnalysisListSection>

        <AnalysisListSection title="성과 좋은 상대">
          {analysis.favorableOpponents.length > 0 ? (
            analysis.favorableOpponents.map((opponent) => (
              <MatchupRow key={`fav-${analysis.championName}-${opponent.championName}`} matchup={opponent} />
            ))
          ) : (
            <EmptyLine />
          )}
        </AnalysisListSection>

        <AnalysisListSection title="자주 간 아이템">
          {analysis.frequentItems.length > 0 ? (
            <div className="space-y-2">
              {analysis.frequentItems.map((item) => (
                <ItemRow key={`${analysis.championName}-${item.itemName}`} item={item} />
              ))}
            </div>
          ) : (
            <EmptyLine />
          )}
        </AnalysisListSection>

        <AnalysisListSection title="좋은 점">
          {analysis.strengths.length > 0 ? (
            <div className="space-y-2">
              {analysis.strengths.map((strength) => (
                <p key={strength} className="text-sm leading-6 text-slate-300">
                  {strength}
                </p>
              ))}
            </div>
          ) : (
            <EmptyLine />
          )}
        </AnalysisListSection>

        <AnalysisListSection title="주의할 점">
          {analysis.watchPoints.length > 0 ? (
            <div className="space-y-2">
              {analysis.watchPoints.map((watchPoint) => (
                <p key={watchPoint} className="text-sm leading-6 text-slate-300">
                  {watchPoint}
                </p>
              ))}
            </div>
          ) : (
            <EmptyLine />
          )}
        </AnalysisListSection>

        <AnalysisListSection title="핵심 해석">
          <div className="space-y-2">
            {analysis.insights.map((insight) => (
              <p key={insight} className="text-sm leading-6 text-slate-300">
                {insight}
              </p>
            ))}
          </div>
        </AnalysisListSection>
      </div>
    </div>
  );
}

type CompactOverviewStatProps = {
  label: string;
  value: string;
};

function CompactOverviewStat({ label, value }: CompactOverviewStatProps) {
  return (
    <div className="bg-[#101722] px-4 py-3">
      <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-slate-500">{label}</p>
      <p className="mt-2 text-sm font-semibold text-white">{value}</p>
    </div>
  );
}

type AnalysisListSectionProps = {
  title: string;
  children: ReactNode;
};

function AnalysisListSection({ title, children }: AnalysisListSectionProps) {
  return (
    <div className="space-y-2">
      <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-slate-500">{title}</p>
      {children}
    </div>
  );
}

type MatchupRowProps = {
  matchup: SearchOverviewMatchupResponse;
};

function MatchupRow({ matchup }: MatchupRowProps) {
  return (
    <div className="flex items-center justify-between gap-3 border border-slate-800 bg-[#111827] px-3 py-2.5">
      <div className="flex min-w-0 items-center gap-2">
        <img
          src={getChampionPortraitUrl(matchup.championKey, matchup.championName)}
          alt={matchup.championNameKo}
          className="h-8 w-8 border border-slate-700 object-cover"
        />
        <div className="min-w-0">
          <p className="truncate text-sm font-medium text-slate-200">{matchup.championNameKo}</p>
          <p className="text-xs text-slate-500">{matchup.matchCount}판</p>
        </div>
      </div>
      <div className="text-right text-xs text-slate-400">
        <p>승률 {matchup.winRate}%</p>
        <p>KDA {matchup.averageKda.toFixed(2)} · 데스 {matchup.averageDeaths.toFixed(2)}</p>
      </div>
    </div>
  );
}

type ItemRowProps = {
  item: SearchOverviewItemResponse;
};

function ItemRow({ item }: ItemRowProps) {
  return (
    <div className="flex items-center justify-between gap-3 border border-slate-800 bg-[#111827] px-3 py-2.5">
      <div className="min-w-0">
        <p className="truncate text-sm font-medium text-slate-200">{item.itemName}</p>
        <p className="mt-1 text-xs text-slate-500">{item.matchCount}판 사용</p>
      </div>
      <div className="text-right text-xs text-slate-400">
        <p>승률 {item.winRate}%</p>
        <p>KDA {item.averageKda.toFixed(2)}</p>
      </div>
    </div>
  );
}

function EmptyLine() {
  return <p className="text-sm text-slate-500">표시할 데이터가 아직 충분하지 않습니다.</p>;
}

type FilterSectionProps = {
  title: string;
  children: ReactNode;
};

function FilterSection({ title, children }: FilterSectionProps) {
  return (
    <div className="space-y-3">
      <span className="block text-sm font-medium text-slate-300">{title}</span>
      <div className="flex flex-wrap gap-2 border border-slate-800 bg-[#111827] p-3">{children}</div>
    </div>
  );
}

type ToggleChipProps = {
  selected: boolean;
  onClick: () => void;
  children: string;
  icon?: ReactNode;
};

function ToggleChip({ selected, onClick, children, icon }: ToggleChipProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={[
        "inline-flex items-center gap-2 border px-3 py-2 text-sm font-medium transition",
        selected ? "border-tide bg-tide text-ink" : "border-slate-700 bg-[#0b1220] text-slate-300 hover:bg-[#182233]",
      ].join(" ")}
    >
      {icon}
      {children}
    </button>
  );
}

type ActiveChipProps = {
  children: ReactNode;
  onRemove: () => void;
  icon?: ReactNode;
};

function ActiveChip({ children, onRemove, icon }: ActiveChipProps) {
  return (
    <button
      type="button"
      onClick={onRemove}
      className="inline-flex items-center gap-2 border border-tide/40 bg-[#151a20] px-3 py-1.5 text-sm font-medium text-tide transition hover:bg-[#1a212b]"
    >
      {icon}
      {children}
    </button>
  );
}

function toSummaryMatch(match: SearchMatchResponse): SummonerMatchSummaryResponse {
  return {
    matchId: match.matchId,
    gameCreation: match.gameCreation,
    gameDuration: 0,
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
    totalDamageDealtToChampions: match.totalDamageDealtToChampions,
    goldEarned: match.goldEarned,
    totalMinionsKilled: match.totalMinionsKilled,
    neutralMinionsKilled: match.neutralMinionsKilled,
    visionScore: match.visionScore,
    wardsPlaced: match.wardsPlaced,
    interpretationTags: match.interpretationTags,
    win: match.win,
  };
}

function getProfileIconUrl(profileIconId: number): string {
  return `https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/profile-icons/${profileIconId}.jpg`;
}
