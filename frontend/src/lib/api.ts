import type {
  MatchDetailResponse,
  SearchFilterOptionsResponse,
  SearchMatchListResponse,
  SearchOverviewResponse,
  SummonerMatchListResponse,
  SummonerProfileResponse,
  SummonerSyncRequest,
  SummonerSyncResponse,
} from "../types/api";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:18000";

type ApiErrorBody = {
  code?: string;
  message?: string;
};

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {}),
    },
    ...init,
  });

  if (!response.ok) {
    let errorMessage = "요청 처리에 실패했습니다.";

    try {
      const body = (await response.json()) as ApiErrorBody;
      errorMessage = body.message ?? errorMessage;
    } catch {
      errorMessage = `${response.status} ${response.statusText}`;
    }

    throw new Error(errorMessage);
  }

  return response.json() as Promise<T>;
}

export function syncSummoner(payload: SummonerSyncRequest): Promise<SummonerSyncResponse> {
  return request<SummonerSyncResponse>("/api/v1/summoners/sync", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function getSummonerByPuuid(puuid: string): Promise<SummonerProfileResponse> {
  return request<SummonerProfileResponse>(`/api/v1/summoners/${puuid}`);
}

export function getRecentMatches(puuid: string, count = 20): Promise<SummonerMatchListResponse> {
  return request<SummonerMatchListResponse>(`/api/v1/summoners/${puuid}/matches?count=${count}`);
}

export function getMatchDetail(matchId: string, focusPuuid?: string | null): Promise<MatchDetailResponse> {
  const query = focusPuuid ? `?focusPuuid=${encodeURIComponent(focusPuuid)}` : "";
  return request<MatchDetailResponse>(`/api/v1/matches/${matchId}${query}`);
}

export function searchMatches(queryString: string): Promise<SearchMatchListResponse> {
  return request<SearchMatchListResponse>(`/api/v1/search/matches?${queryString}`);
}

export function getSearchFilterOptions(puuid: string): Promise<SearchFilterOptionsResponse> {
  return request<SearchFilterOptionsResponse>(`/api/v1/search/filter-options?puuid=${encodeURIComponent(puuid)}`);
}

export function getSearchOverview(puuid: string, count = 10): Promise<SearchOverviewResponse> {
  return request<SearchOverviewResponse>(`/api/v1/search/overview?puuid=${encodeURIComponent(puuid)}&count=${count}`);
}
