package com.riftmind.summoner.application.dto;

import java.time.LocalDateTime;

/**
 * 소환사 동기화 결과를 담는 애플리케이션 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public record SummonerSyncResult(
        String puuid,
        String gameName,
        String tagLine,
        int requestedMatchCount,
        int savedMatchCount,
        LocalDateTime syncedAt) {
}
