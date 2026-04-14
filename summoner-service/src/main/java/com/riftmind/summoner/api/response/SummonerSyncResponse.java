package com.riftmind.summoner.api.response;

import java.time.LocalDateTime;

import com.riftmind.summoner.application.dto.SummonerSyncResult;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 소환사 동기화 결과를 담는 API 응답 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Schema(description = "소환사 동기화 결과 응답")
public record SummonerSyncResponse(
        @Schema(description = "Riot PUUID", example = "sample-puuid-value")
        String puuid,
        @Schema(description = "라이엇 게임 이름", example = "Hide on bush")
        String gameName,
        @Schema(description = "라이엇 태그 라인", example = "KR1")
        String tagLine,
        @Schema(description = "요청한 경기 수", example = "20")
        int requestedMatchCount,
        @Schema(description = "실제로 저장된 경기 수", example = "20")
        int savedMatchCount,
        @Schema(description = "이미 저장되어 상세 조회를 생략한 경기 수", example = "5")
        int existingMatchCount,
        @Schema(description = "동기화 완료 시각", example = "2026-03-23T14:30:00")
        LocalDateTime syncedAt) {

    /**
     * 동기화 결과 DTO를 API 응답 DTO로 변환합니다.
     *
     * @param result 동기화 결과 DTO
     * @return 소환사 동기화 응답 DTO
     */
    public static SummonerSyncResponse from(SummonerSyncResult result) {
        return new SummonerSyncResponse(
                result.puuid(),
                result.gameName(),
                result.tagLine(),
                result.requestedMatchCount(),
                result.savedMatchCount(),
                result.existingMatchCount(),
                result.syncedAt());
    }
}
