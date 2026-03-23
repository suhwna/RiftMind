package com.riftmind.summoner.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 소환사 동기화 요청 정보를 담는 API 요청 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Schema(description = "소환사 동기화 요청")
public record SummonerSyncRequest(
        @Schema(description = "라이엇 게임 이름", example = "Hide on bush")
        @NotBlank(message = "gameName is required")
        String gameName,
        @Schema(description = "라이엇 태그 라인", example = "KR1")
        @NotBlank(message = "tagLine is required")
        String tagLine,
        @Schema(description = "수집할 최근 경기 수", example = "20", minimum = "1", maximum = "20")
        @Min(value = 1, message = "matchCount must be at least 1")
        @Max(value = 20, message = "matchCount must be at most 20")
        Integer matchCount) {
}
