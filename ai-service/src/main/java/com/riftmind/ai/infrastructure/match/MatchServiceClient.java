package com.riftmind.ai.infrastructure.match;

/**
 * match-service에서 AI 회고에 필요한 경기 상세 데이터를 조회합니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
public interface MatchServiceClient {

    /**
     * matchId와 focus PUUID 기준으로 매치 상세 정보를 조회합니다.
     *
     * @param matchId Riot matchId
     * @param focusPuuid 회고 대상 PUUID
     * @return 매치 상세 응답
     */
    MatchDetailResponse getMatchDetail(String matchId, String focusPuuid);
}
