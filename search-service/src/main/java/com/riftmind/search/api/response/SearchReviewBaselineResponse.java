package com.riftmind.search.api.response;

import java.util.List;

/**
 * AI 경기 회고에 사용할 누적 기준 데이터를 담습니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
public record SearchReviewBaselineResponse(
        String championName,
        String championKey,
        String championNameKo,
        String opponentChampionName,
        String opponentChampionKey,
        String opponentChampionNameKo,
        String teamPosition,
        String teamPositionKo,
        int sampleCount,
        int winCount,
        int winRate,
        double averageKda,
        double averageDeaths,
        int averageDamage,
        int averageGold,
        int averageCs,
        int averageVisionScore,
        List<SearchOverviewItemResponse> frequentItems,
        List<String> insights
) {
}
