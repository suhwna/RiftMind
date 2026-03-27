package com.riftmind.match.application.dto;

import java.util.List;

/**
 * 매치 상세 인사이트를 좋은 점과 아쉬운 점으로 나눠 담습니다.
 *
 * @author 정수환
 * @since 2026-03-27
 */
public record MatchDetailInsightSummary(
        List<String> strengths,
        List<String> weaknesses) {
}
