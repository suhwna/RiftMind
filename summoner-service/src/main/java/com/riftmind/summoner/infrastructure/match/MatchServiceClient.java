package com.riftmind.summoner.infrastructure.match;

import com.riftmind.summoner.api.response.SummonerMatchListResponse;

/**
 * match-service 연동을 추상화한 클라이언트 계약입니다.
 *
 * @author 정수환
 * @since 2026-03-24
 */
public interface MatchServiceClient {

    /**
     * match-service에 최근 매치 동기화를 요청합니다.
     *
     * @param puuid Riot PUUID
     * @param matchCount 요청할 경기 수
     * @return 동기화 결과
     */
    MatchSyncResult syncMatches(String puuid, Integer matchCount);

    /**
     * match-service에서 최근 경기 목록을 조회합니다.
     *
     * @param puuid Riot PUUID
     * @param count 조회할 경기 수
     * @return 최근 경기 목록 응답
     */
    SummonerMatchListResponse getRecentMatches(String puuid, int count);

    /**
     * match-service 동기화 결과를 담는 내부 DTO입니다.
     *
     * @param puuid Riot PUUID
     * @param requestedMatchCount 요청한 경기 수
     * @param savedMatchCount 저장한 경기 수
     * @param existingMatchCount 이미 저장되어 상세 조회를 생략한 경기 수
     */
    record MatchSyncResult(
            String puuid,
            int requestedMatchCount,
            int savedMatchCount,
            int existingMatchCount) {
    }
}
