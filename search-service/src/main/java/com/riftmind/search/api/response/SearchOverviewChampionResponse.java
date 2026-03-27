package com.riftmind.search.api.response;

/**
 * 최근 경기 분석에서 챔피언 기준 요약 정보를 담습니다.
 *
 * @author 정수환
 * @since 2026-03-27
 */
public record SearchOverviewChampionResponse(
        String championName,
        String championKey,
        String championNameKo,
        int matchCount,
        int winCount,
        int winRate,
        double averageKda
) {
}
