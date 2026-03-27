package com.riftmind.search.api.response;

/**
 * 챔피언 분석에서 아이템 기준 요약 정보를 담습니다.
 *
 * @author 정수환
 * @since 2026-03-27
 */
public record SearchOverviewItemResponse(
        String itemName,
        int matchCount,
        int winCount,
        int winRate,
        double averageKda
) {
}
