package com.riftmind.ai.infrastructure.openai;

import com.riftmind.ai.application.service.MatchReviewResult;

/**
 * OpenAI 기반 경기 회고 생성을 추상화합니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
public interface OpenAiReviewClient {

    /**
     * 정리된 경기 입력 데이터를 바탕으로 회고를 생성합니다.
     *
     * @param promptPayload 회고 생성 입력 JSON
     * @return 구조화된 회고 결과
     */
    MatchReviewResult generateReview(String promptPayload);
}
