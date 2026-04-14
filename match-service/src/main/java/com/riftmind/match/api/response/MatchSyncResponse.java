package com.riftmind.match.api.response;

import java.time.LocalDateTime;

import com.riftmind.match.application.dto.MatchSyncResult;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 매치 동기화 결과를 담는 API 응답 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-24
 */
@Schema(description = "매치 동기화 응답")
public record MatchSyncResponse(
        @Schema(description = "Riot PUUID", example = "sample-puuid-value")
        String puuid,
        @Schema(description = "요청한 경기 수", example = "20")
        int requestedMatchCount,
        @Schema(description = "저장한 경기 수", example = "20")
        int savedMatchCount,
        @Schema(description = "이미 저장되어 상세 조회를 생략한 경기 수", example = "5")
        int existingMatchCount,
        @Schema(description = "동기화 시각", example = "2026-03-24T10:30:00")
        LocalDateTime syncedAt) {

    /**
     * 애플리케이션 DTO를 API 응답 DTO로 변환합니다.
     *
     * @param result 매치 동기화 결과
     * @return 매치 동기화 응답
     */
    public static MatchSyncResponse from(MatchSyncResult result) {
        return new MatchSyncResponse(
                result.puuid(),
                result.requestedMatchCount(),
                result.savedMatchCount(),
                result.existingMatchCount(),
                result.syncedAt());
    }
}
