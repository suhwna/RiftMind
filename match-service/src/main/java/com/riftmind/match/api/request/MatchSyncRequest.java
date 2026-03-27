package com.riftmind.match.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 매치 동기화 요청 정보를 담는 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-24
 */
@Schema(description = "매치 동기화 요청")
public record MatchSyncRequest(
        @Schema(description = "Riot PUUID", example = "sample-puuid-value")
        @NotBlank
        String puuid,
        @Schema(description = "동기화할 경기 수", example = "100")
        @Min(1)
        @Max(100)
        Integer matchCount) {
}
