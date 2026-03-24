package com.riftmind.summoner.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riftmind.summoner.api.response.MatchDetailResponse;
import com.riftmind.summoner.application.service.MatchQueryService;
import com.riftmind.summoner.application.service.StaticDataService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 매치 조회 API를 제공합니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Tag(name = "Match", description = "매치 상세 조회 API")
@RestController
@RequestMapping("/api/v1/matches")
public class MatchController {

    private final MatchQueryService matchQueryService;
    private final StaticDataService staticDataService;

    public MatchController(MatchQueryService matchQueryService, StaticDataService staticDataService) {
        this.matchQueryService = matchQueryService;
        this.staticDataService = staticDataService;
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
}
