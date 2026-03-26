package com.riftmind.summoner.infrastructure.search;

/**
 * search-service 연동을 추상화한 클라이언트 계약입니다.
 *
 * @author 정수환
 * @since 2026-03-26
 */
public interface SearchServiceClient {

    /**
     * search-service에 최근 경기 재색인을 요청합니다.
     *
     * @param puuid Riot PUUID
     * @param matchCount 색인할 경기 수
     * @return 색인 결과
     */
    SearchIndexResult indexRecentMatches(String puuid, Integer matchCount);

    /**
     * search-service 색인 결과를 담는 내부 DTO입니다.
     *
     * @param puuid Riot PUUID
     * @param requestedMatchCount 요청한 경기 수
     * @param indexedMatchCount 색인한 경기 수
     */
    record SearchIndexResult(
            String puuid,
            int requestedMatchCount,
            int indexedMatchCount
    ) {
    }
}
