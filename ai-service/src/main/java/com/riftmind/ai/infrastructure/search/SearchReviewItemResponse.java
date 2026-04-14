package com.riftmind.ai.infrastructure.search;

/**
 * AI 회고용 누적 기준에서 사용하는 아이템 요약 응답입니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
public record SearchReviewItemResponse(
        String itemName,
        int matchCount,
        int winCount,
        int winRate,
        double averageKda
) {
}
