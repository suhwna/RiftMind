package com.riftmind.match.api.controller;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.riftmind.match.api.request.MatchSyncRequest;
import com.riftmind.match.api.response.MatchDetailResponse;
import com.riftmind.match.api.response.MatchSearchSourceListResponse;
import com.riftmind.match.api.response.MatchSyncResponse;
import com.riftmind.match.api.response.SummonerMatchListResponse;
import com.riftmind.match.application.service.MatchSyncService;
import com.riftmind.match.application.service.MatchQueryService;
import com.riftmind.match.application.service.StaticDataService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 매치 조회 API를 제공합니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Validated
@Tag(name = "Match", description = "매치 조회 및 동기화 API")
@RestController
@RequestMapping(value = "/api/v1/matches", produces = MediaType.APPLICATION_JSON_VALUE)
public class MatchController {

    private final MatchSyncService matchSyncService;
    private final MatchQueryService matchQueryService;
    private final StaticDataService staticDataService;

    public MatchController(
            MatchSyncService matchSyncService,
            MatchQueryService matchQueryService,
            StaticDataService staticDataService) {
        this.matchSyncService = matchSyncService;
        this.matchQueryService = matchQueryService;
        this.staticDataService = staticDataService;
    }

    /**
     * PUUID 기준으로 최근 매치 정보를 동기화합니다.
     *
     * @param request 매치 동기화 요청
     * @return 동기화 결과 응답
     */
    @PostMapping(value = "/sync", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "매치 동기화", description = "PUUID 기준으로 최근 매치 상세 정보를 수집해 저장합니다.")
    public MatchSyncResponse sync(@Valid @RequestBody MatchSyncRequest request) {
        return MatchSyncResponse.from(matchSyncService.sync(request.puuid(), request.matchCount()));
    }

    /**
     * matchId 기준으로 저장된 매치 상세 정보를 조회합니다.
     *
     * @param matchId Riot matchId
     * @return 매치 상세 응답
     */
    @GetMapping("/{matchId}")
    @Operation(summary = "매치 상세 조회", description = "저장된 매치 상세 정보를 matchId 기준으로 조회합니다.")
    public MatchDetailResponse getMatchDetail(
            @Parameter(description = "Riot matchId", example = "KR_1234567890") @PathVariable String matchId) {
        return MatchDetailResponse.from(matchQueryService.getMatchDetail(matchId), staticDataService);
    }

    /**
     * PUUID 기준으로 최근 경기 목록을 조회합니다.
     *
     * @param puuid Riot PUUID
     * @param count 조회할 경기 수
     * @return 최근 경기 목록 응답
     */
    @GetMapping("/by-puuid")
    @Operation(summary = "PUUID 기준 최근 경기 조회", description = "저장된 최근 경기 목록을 PUUID 기준으로 조회합니다.")
    public SummonerMatchListResponse getRecentMatches(
            @Parameter(description = "Riot PUUID", example = "sample-puuid-value")
            @RequestParam @NotBlank String puuid,
            @Parameter(description = "조회할 경기 수", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(20) int count) {
        return SummonerMatchListResponse.from(
                puuid,
                matchQueryService.getRecentMatches(puuid, count),
                staticDataService);
    }

    @GetMapping("/search-source/by-puuid")
    @Operation(summary = "검색 색인용 최근 참가자 조회", description = "저장된 최근 참가자 상세를 PUUID 기준으로 조회합니다.")
    public MatchSearchSourceListResponse getRecentMatchesForSearch(
            @Parameter(description = "Riot PUUID", example = "sample-puuid-value")
            @RequestParam @NotBlank String puuid,
            @Parameter(description = "조회할 경기 수", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(20) int count) {
        return MatchSearchSourceListResponse.from(
                puuid,
                matchQueryService.getRecentSearchSourceMatches(puuid, count),
                staticDataService);
    }
}
