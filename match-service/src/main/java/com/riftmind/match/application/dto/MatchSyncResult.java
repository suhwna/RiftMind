package com.riftmind.match.application.dto;

import java.time.LocalDateTime;

/**
 * 매치 동기화 결과를 담는 애플리케이션 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-24
 */
public record MatchSyncResult(
        String puuid,
        int requestedMatchCount,
        int savedMatchCount,
        LocalDateTime syncedAt) {
}
