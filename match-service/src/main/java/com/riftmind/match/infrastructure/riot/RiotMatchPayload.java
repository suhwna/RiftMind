package com.riftmind.match.infrastructure.riot;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Riot 매치 상세 조회 결과를 담는 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public record RiotMatchPayload(
        String matchId,
        LocalDateTime gameCreation,
        Integer gameDuration,
        Integer queueId,
        String gameMode,
        String gameVersion,
        List<RiotMatchParticipantPayload> participants) {
}
