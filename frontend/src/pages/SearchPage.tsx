import { useInfiniteQuery, useQuery } from "@tanstack/react-query";
import { useDeferredValue, useEffect, useMemo, useRef, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { SectionCard } from "../components/SectionCard";
import { getSearchFilterOptions, searchMatches } from "../lib/api";

type SearchFormState = {
  championNames: string[];
  teamPositions: string[];
  queueIds: string[];
  win: string;
  minKda: string;
};

const initialForm: SearchFormState = {
  championNames: [],
  teamPositions: [],
  queueIds: [],
  win: "",
  minKda: "",
};

const PAGE_SIZE = 4;
export function SearchPage() {
  const [searchParams] = useSearchParams();
  const puuid = searchParams.get("puuid") ?? "";
  const [form, setForm] = useState<SearchFormState>(initialForm);
  const deferredForm = useDeferredValue(form);
  const loadMoreRef = useRef<HTMLDivElement | null>(null);
  const scrollContainerRef = useRef<HTMLDivElement | null>(null);

  const baseQueryString = useMemo(() => {
    const nextSearchParams = new URLSearchParams();

    if (puuid) {
      nextSearchParams.set("puuid", puuid);
    }

    deferredForm.championNames.forEach((value) => nextSearchParams.append("championNames", value));
    deferredForm.teamPositions.forEach((value) => nextSearchParams.append("teamPositions", value));
    deferredForm.queueIds.forEach((value) => nextSearchParams.append("queueIds", value));
    if (deferredForm.win.trim()) {
      nextSearchParams.set("win", deferredForm.win.trim());
    }
    if (deferredForm.minKda.trim()) {
      nextSearchParams.set("minKda", deferredForm.minKda.trim());
    }

    return nextSearchParams.toString();
  }, [deferredForm, puuid]);

  const searchQuery = useInfiniteQuery({
    queryKey: ["search-matches", baseQueryString],
    queryFn: ({ pageParam }) => {
      const nextSearchParams = new URLSearchParams(baseQueryString);
      nextSearchParams.set("page", String(pageParam));
      nextSearchParams.set("size", String(PAGE_SIZE));
      return searchMatches(nextSearchParams.toString());
    },
    enabled: baseQueryString.length > 0,
    initialPageParam: 0,
    getNextPageParam: (lastPage, allPages) => {
      const loadedCount = allPages.flatMap((page) => page.matches).length;
      return loadedCount < lastPage.total ? allPages.length : undefined;
    },
  });

  const filterOptionsQuery = useQuery({
    queryKey: ["search-filter-options", puuid],
    queryFn: () => getSearchFilterOptions(puuid),
    enabled: Boolean(puuid),
  });

  const flattenedMatches = searchQuery.data?.pages.flatMap((page) => page.matches) ?? [];
  const total = searchQuery.data?.pages[0]?.total ?? 0;

  useEffect(() => {
    if (!loadMoreRef.current || !searchQuery.hasNextPage || searchQuery.isFetchingNextPage) {
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        const [entry] = entries;

        if (entry?.isIntersecting) {
          void searchQuery.fetchNextPage();
        }
      },
      {
        root: scrollContainerRef.current,
        rootMargin: "120px 0px",
      },
    );

    observer.observe(loadMoreRef.current);

    return () => observer.disconnect();
  }, [searchQuery.fetchNextPage, searchQuery.hasNextPage, searchQuery.isFetchingNextPage, flattenedMatches.length]);

  const toggleSelection = (field: "championNames" | "teamPositions" | "queueIds", value: string) => {
    setForm((current) => {
      const values = current[field];
      const nextValues = values.includes(value)
        ? values.filter((item) => item !== value)
        : [...values, value];

      return {
        ...current,
        [field]: nextValues,
      };
    });
  };

  return (
    <div className="grid gap-6 xl:grid-cols-[0.85fr_1.15fr]">
      <SectionCard title="고급 검색">
        {!puuid ? (
          <p className="mb-4 rounded-2xl bg-amber-50 px-4 py-3 text-sm text-amber-700">
            먼저 소환사 전적을 불러온 뒤 이용하세요.
          </p>
        ) : null}
        <form className="space-y-4">
          <div className="space-y-2">
            <span className="block text-sm font-medium text-slate-600">챔피언</span>
            <div className="max-h-44 space-y-2 overflow-y-auto rounded-2xl border border-slate-200 bg-slate-50 p-3">
              {filterOptionsQuery.data?.champions.map((champion) => (
                <label key={champion.championName} className="flex cursor-pointer items-center gap-3 text-sm text-slate-700">
                  <input
                    type="checkbox"
                    checked={form.championNames.includes(champion.championName)}
                    onChange={() => toggleSelection("championNames", champion.championName)}
                    className="h-4 w-4 rounded border-slate-300 text-ink focus:ring-tide"
                  />
                  <span>{champion.championNameKo}</span>
                </label>
              ))}
            </div>
          </div>

          <div className="space-y-2">
            <span className="block text-sm font-medium text-slate-600">포지션</span>
            <div className="space-y-2 rounded-2xl border border-slate-200 bg-slate-50 p-3">
              {filterOptionsQuery.data?.positions.map((position) => (
                <label key={position.teamPosition} className="flex cursor-pointer items-center gap-3 text-sm text-slate-700">
                  <input
                    type="checkbox"
                    checked={form.teamPositions.includes(position.teamPosition)}
                    onChange={() => toggleSelection("teamPositions", position.teamPosition)}
                    className="h-4 w-4 rounded border-slate-300 text-ink focus:ring-tide"
                  />
                  <span>{position.teamPositionKo}</span>
                </label>
              ))}
            </div>
          </div>

          <div className="space-y-2">
            <span className="block text-sm font-medium text-slate-600">게임 모드</span>
            <div className="space-y-2 rounded-2xl border border-slate-200 bg-slate-50 p-3">
              {filterOptionsQuery.data?.modes.map((mode) => (
                <label key={`${mode.queueId}-${mode.queueNameKo}`} className="flex cursor-pointer items-center gap-3 text-sm text-slate-700">
                  <input
                    type="checkbox"
                    checked={form.queueIds.includes(String(mode.queueId))}
                    onChange={() => toggleSelection("queueIds", String(mode.queueId))}
                    className="h-4 w-4 rounded border-slate-300 text-ink focus:ring-tide"
                  />
                  <span>{mode.queueNameKo}</span>
                </label>
              ))}
            </div>
          </div>

          <label className="block">
            <span className="mb-2 block text-sm font-medium text-slate-600">최소 KDA</span>
            <input
              value={form.minKda}
              onChange={(event) => setForm((current) => ({ ...current, minKda: event.target.value }))}
              placeholder="예: 3"
              className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none transition focus:border-tide"
            />
          </label>

          <label className="block">
            <span className="mb-2 block text-sm font-medium text-slate-600">승패</span>
            <select
              value={form.win}
              onChange={(event) => setForm((current) => ({ ...current, win: event.target.value }))}
              className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none transition focus:border-tide"
            >
              <option value="">전체</option>
              <option value="true">승리</option>
              <option value="false">패배</option>
            </select>
          </label>
        </form>
      </SectionCard>

      <SectionCard title="검색 결과">
        {!baseQueryString ? <p className="text-sm text-slate-500">조건을 입력하고 검색을 실행하세요.</p> : null}
        {searchQuery.isLoading ? <p className="text-sm text-slate-500">검색 결과를 불러오는 중입니다...</p> : null}
        {searchQuery.isError ? (
          <p className="rounded-2xl bg-rose-50 px-4 py-3 text-sm text-rose-700">
            {(searchQuery.error as Error).message}
          </p>
        ) : null}
        {searchQuery.data ? (
          <div className="space-y-4">
            <p className="text-sm text-slate-500">
              총 {total}건 · 현재 {flattenedMatches.length}건 표시
            </p>
            <div
              ref={scrollContainerRef}
              className="max-h-[520px] space-y-4 overflow-y-auto rounded-[24px] border border-slate-200 bg-slate-50/70 p-3"
            >
              {flattenedMatches.map((match) => (
                <Link
                  key={`${match.matchId}-${match.puuid}`}
                  to={`/matches/${match.matchId}?puuid=${match.puuid}`}
                  className="block rounded-2xl border border-slate-200 bg-slate-50 p-4 transition hover:bg-white"
                >
                  <div className="flex flex-wrap items-center justify-between gap-3">
                    <div>
                      <p className="text-xs font-semibold uppercase tracking-[0.18em] text-tide">{match.queueNameKo}</p>
                      <h3 className="mt-2 text-lg font-semibold text-slate-950">
                        {match.championNameKo} · {match.summonerName}
                      </h3>
                      <p className="mt-1 text-sm text-slate-500">
                        {match.teamPositionKo ?? "포지션 미상"} · {new Date(match.gameCreation).toLocaleString("ko-KR")}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className={`text-sm font-semibold ${match.win ? "text-emerald-700" : "text-rose-700"}`}>
                        {match.win ? "승리" : "패배"}
                      </p>
                      <p className="mt-2 text-sm text-slate-600">
                        {match.kills} / {match.deaths} / {match.assists}
                      </p>
                      <p className="text-xs text-slate-500">KDA {match.kda.toFixed(2)}</p>
                    </div>
                  </div>
                </Link>
              ))}
              <div ref={loadMoreRef} className="min-h-8">
                {searchQuery.isFetchingNextPage ? (
                  <p className="text-center text-sm text-slate-500">다음 검색 결과를 불러오는 중입니다...</p>
                ) : null}
                {!searchQuery.hasNextPage && flattenedMatches.length > 0 ? (
                  <p className="text-center text-sm text-slate-400">검색 결과를 모두 불러왔습니다.</p>
                ) : null}
              </div>
            </div>
          </div>
        ) : null}
      </SectionCard>
    </div>
  );
}
