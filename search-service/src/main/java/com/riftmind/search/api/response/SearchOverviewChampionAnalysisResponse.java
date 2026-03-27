package com.riftmind.search.api.response;

import java.util.List;

/**
 * 챔피언 기준 매치업/빌드/성과 분석 카드 응답입니다.
 *
 * @author 정수환
 * @since 2026-03-27
 */
public record SearchOverviewChampionAnalysisResponse(
        String championName,
        String championKey,
        String championNameKo,
        String primaryPosition,
        String primaryPositionKo,
        int matchCount,
        int winCount,
        int winRate,
        double averageKda,
        int averageDamage,
        int averageGold,
        int averageCs,
        int averageVisionScore,
        List<SearchOverviewMatchupResponse> frequentOpponents,
        List<SearchOverviewMatchupResponse> favorableOpponents,
        List<SearchOverviewMatchupResponse> toughestOpponents,
        List<SearchOverviewItemResponse> frequentItems,
        List<String> strengths,
        List<String> watchPoints,
        List<String> insights
) {
}
