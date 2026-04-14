package com.riftmind.ai.infrastructure.match;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * match-service 매치 상세 응답 중 AI 회고에 필요한 필드를 담습니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MatchDetailResponse(
        String matchId,
        LocalDateTime gameCreation,
        Integer gameDuration,
        String gameDurationText,
        Integer queueId,
        String queueNameKo,
        String gameMode,
        String gameVersion,
        List<String> focusStrengths,
        List<String> focusWeaknesses,
        List<MatchParticipantResponse> participants) {
}
