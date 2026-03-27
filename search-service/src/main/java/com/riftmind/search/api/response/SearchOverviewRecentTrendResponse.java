package com.riftmind.search.api.response;

import java.util.List;

/**
 * 최근 소수 경기 구간의 추세 요약입니다.
 *
 * @author 정수환
 * @since 2026-03-27
 */
public record SearchOverviewRecentTrendResponse(
        int matchCount,
        int winRate,
        double averageKda,
        int averageDamage,
        int averageCs,
        int averageVisionScore,
        List<String> insights
) {
}
