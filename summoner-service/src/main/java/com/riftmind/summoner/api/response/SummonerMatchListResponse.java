package com.riftmind.summoner.api.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 소환사 최근 경기 목록을 담는 API 응답 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Schema(description = "소환사 최근 경기 목록 응답")
public record SummonerMatchListResponse(
        @Schema(description = "Riot PUUID", example = "sample-puuid-value")
        String puuid,
        @Schema(description = "응답된 경기 수", example = "20")
        int count,
        @ArraySchema(schema = @Schema(implementation = SummonerMatchSummaryResponse.class))
        List<SummonerMatchSummaryResponse> matches) {
}
