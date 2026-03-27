package com.riftmind.search.api.response;

/**
 * 특정 챔피언 플레이 기준 상대 매치업 요약입니다.
 *
 * @author 정수환
 * @since 2026-03-27
 */
public record SearchOverviewMatchupResponse(
        String championName,
        String championKey,
        String championNameKo,
        int matchCount,
        int winCount,
        int winRate,
        double averageKda,
        double averageDeaths
) {
}
