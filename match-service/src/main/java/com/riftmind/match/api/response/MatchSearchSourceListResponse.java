package com.riftmind.match.api.response;

import com.riftmind.match.application.dto.SearchableMatchParticipantView;
import com.riftmind.match.application.service.StaticDataService;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 검색 색인용 최근 참가자 목록 응답 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-25
 */
@Schema(description = "검색 색인용 최근 참가자 목록 응답")
public record MatchSearchSourceListResponse(
        @Schema(description = "Riot PUUID", example = "sample-puuid-value")
        String puuid,
        @Schema(description = "조회 건수", example = "10")
        int count,
        @ArraySchema(schema = @Schema(implementation = MatchSearchSourceResponse.class))
        List<MatchSearchSourceResponse> matches) {

    /**
     * 검색 색인용 참가자 목록을 API 응답 DTO로 변환합니다.
     *
     * @param puuid Riot PUUID
     * @param matches 참가자 목록
     * @param staticDataService 정적 데이터 서비스
     * @return 검색 색인용 목록 응답
     */
    public static MatchSearchSourceListResponse from(
            String puuid,
            List<SearchableMatchParticipantView> matches,
            StaticDataService staticDataService
    ) {
        return new MatchSearchSourceListResponse(
                puuid,
                matches.size(),
                matches.stream()
                        .map(match -> MatchSearchSourceResponse.from(match, staticDataService))
                        .toList());
    }
}
