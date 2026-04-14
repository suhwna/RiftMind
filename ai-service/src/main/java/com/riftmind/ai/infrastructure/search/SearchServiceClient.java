package com.riftmind.ai.infrastructure.search;

/**
 * AI 회고에 필요한 누적 검색 기준 데이터를 조회하는 클라이언트 계약입니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
public interface SearchServiceClient {

    /**
     * 챔피언, 상대 챔피언, 포지션 기준 누적 표본을 조회합니다.
     *
     * @param championName 플레이어 챔피언 영문 이름
     * @param opponentChampionName 상대 챔피언 영문 이름
     * @param teamPosition 포지션
     * @return 누적 기준 응답
     */
    SearchReviewBaselineResponse getReviewBaseline(
            String championName,
            String opponentChampionName,
            String teamPosition);
}
