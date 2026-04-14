package com.riftmind.ai.infrastructure.search;

import java.util.List;

/**
 * search-service에서 조회한 AI 회고용 누적 기준 응답입니다.
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
        List<SearchReviewItemResponse> frequentItems,
        List<String> insights
) {
}
