package com.riftmind.summoner.infrastructure.riot;

import java.util.List;

/**
 * Riot 플랫폼 API 접근을 추상화한 게이트웨이 계약입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public interface RiotPlatformGateway {

    /**
     * Riot ID로 계정 정보를 조회합니다.
     *
     * @param gameName 라이엇 게임 이름
     * @param tagLine 라이엇 태그 라인
     * @return Riot 계정 정보
     */
    RiotAccountPayload fetchAccountByRiotId(String gameName, String tagLine);

    /**
     * PUUID로 소환사 정보를 조회합니다.
     *
     * @param puuid Riot PUUID
     * @return Riot 소환사 정보
     */
    RiotSummonerPayload fetchSummonerByPuuid(String puuid);

    /**
     * PUUID 기준 최근 matchId 목록을 조회합니다.
     *
     * @param puuid Riot PUUID
     * @param count 조회할 경기 수
     * @return matchId 목록
     */
    List<String> fetchMatchIdsByPuuid(String puuid, int count);

    /**
     * matchId 기준 매치 상세 정보를 조회합니다.
     *
     * @param matchId Riot matchId
     * @return Riot 매치 상세 정보
     */
    RiotMatchPayload fetchMatchById(String matchId);
}
