package com.riftmind.summoner.infrastructure.riot;

/**
 * Riot 계정 조회 결과를 담는 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public record RiotAccountPayload(
        String puuid,
        String gameName,
        String tagLine) {
}
