package com.riftmind.search.api.response;

import java.util.List;

/**
 * 최근 경기 기반 플레이 패턴 요약 응답입니다.
 *
 * @author 정수환
 * @since 2026-03-27
 */
public record SearchOverviewResponse(
        String puuid,
        int requestedMatchCount,
        int analyzedMatchCount,
        long totalIndexedMatches,
        int winCount,
        int lossCount,
        int winRate,
        double averageKda,
        int averageDamage,
        int averageGold,
        int averageCs,
        int averageVisionScore,
        List<SearchOverviewChampionResponse> topPlayedChampions,
        SearchOverviewChampionResponse bestChampion,
        SearchOverviewRecentTrendResponse recentTrend,
        List<SearchOverviewChampionAnalysisResponse> championAnalyses,
        List<String> insights
) {
}
