package com.riftmind.ai.application.service;

import java.util.List;

/**
 * AI가 생성한 경기 회고 결과를 담습니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
public record MatchReviewResult(
        String summary,
        List<String> strongPoints,
        List<String> weakPoints,
        List<String> nextFocus) {
}
