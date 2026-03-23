package com.riftmind.summoner.infrastructure.riot;

/**
 * Riot 매치 참가자 정보를 담는 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public record RiotMatchParticipantPayload(
        String puuid,
        String summonerName,
        String championName,
        String teamPosition,
        int kills,
        int deaths,
        int assists,
        boolean win) {
}
