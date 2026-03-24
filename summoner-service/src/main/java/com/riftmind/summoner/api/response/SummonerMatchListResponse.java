package com.riftmind.summoner.api.response;

import java.util.List;

import com.riftmind.summoner.application.dto.RecentMatchView;
import com.riftmind.summoner.application.service.StaticDataService;

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

    /**
     * 최근 경기 조회 DTO 목록을 API 응답 DTO로 변환합니다.
     *
     * @param puuid Riot PUUID
     * @param matches 최근 경기 조회 DTO 목록
     * @param staticDataService 정적 데이터 서비스
     * @return 최근 경기 목록 응답 DTO
     */
    public static SummonerMatchListResponse from(
            String puuid,
            List<RecentMatchView> matches,
            StaticDataService staticDataService) {
        List<SummonerMatchSummaryResponse> summaries = matches.stream()
                .map(match -> SummonerMatchSummaryResponse.from(match, staticDataService))
                .toList();
        return new SummonerMatchListResponse(puuid, summaries.size(), summaries);
    }
}
