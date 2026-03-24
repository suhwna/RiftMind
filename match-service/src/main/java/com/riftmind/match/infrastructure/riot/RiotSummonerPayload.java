package com.riftmind.match.infrastructure.riot;

/**
 * Riot 소환사 조회 결과를 담는 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public record RiotSummonerPayload(
        String summonerId,
        String accountId,
        Integer profileIconId,
        Long summonerLevel) {
}
