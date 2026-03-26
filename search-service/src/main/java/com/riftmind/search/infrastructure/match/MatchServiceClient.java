package com.riftmind.search.infrastructure.match;

import java.util.List;

/**
 * match-service 검색 원본 데이터를 조회하는 클라이언트 인터페이스입니다.
 *
 * @author 정수환
 * @since 2026-03-25
 */
public interface MatchServiceClient {

    /**
     * PUUID 기준 최근 참가자 상세를 조회합니다.
     *
     * @param puuid Riot PUUID
     * @param count 조회할 경기 수
     * @return 검색 색인용 최근 참가자 목록
     */
    SearchSourceListResult getRecentMatchesForSearch(String puuid, int count);

    /**
     * 검색 색인용 최근 참가자 목록 DTO입니다.
     *
     * @param puuid Riot PUUID
     * @param count 조회 건수
     * @param matches 참가자 상세 목록
     */
    record SearchSourceListResult(
            String puuid,
            int count,
            List<SearchSourceMatchResult> matches
    ) {
    }

    /**
     * 검색 색인용 매치 참가자 DTO입니다.
     */
    record SearchSourceMatchResult(
            String matchId,
            String gameCreation,
            Integer queueId,
            String queueNameKo,
            String gameMode,
            String puuid,
            String summonerName,
            String championName,
            String championKey,
            String championNameKo,
            String teamPosition,
            String teamPositionKo,
            int kills,
            int deaths,
            int assists,
            boolean win,
            int totalDamageDealtToChampions,
            int goldEarned,
            int totalMinionsKilled,
            int neutralMinionsKilled,
            int visionScore,
            int wardsPlaced,
            int wardsKilled,
            int champLevel,
            List<Integer> itemIds,
            List<String> itemNames,
            List<String> itemIconUrls,
            List<Integer> summonerSpellIds,
            List<String> summonerSpellNames,
            List<String> summonerSpellIconUrls,
            Integer primaryRune,
            String primaryRuneName,
            String primaryRuneIconUrl,
            Integer secondaryRune,
            String secondaryRuneName,
            String secondaryRuneIconUrl,
            List<String> interpretationTags,
            int totalDamageTaken
    ) {
    }
}
