package com.riftmind.summoner.api.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.riftmind.summoner.api.request.SummonerSyncRequest;
import com.riftmind.summoner.api.response.SummonerMatchListResponse;
import com.riftmind.summoner.api.response.SummonerProfileResponse;
import com.riftmind.summoner.api.response.SummonerSyncResponse;
import com.riftmind.summoner.application.service.MatchQueryService;
import com.riftmind.summoner.application.service.SummonerQueryService;
import com.riftmind.summoner.application.service.SummonerSyncService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 소환사 조회 및 동기화 API를 제공합니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Validated
@Tag(name = "Summoner", description = "Riot ID 기반 소환사 조회 및 동기화 API")
@RestController
@RequestMapping("/api/v1/summoners")
public class SummonerController {

    private final SummonerSyncService summonerSyncService;
    private final SummonerQueryService summonerQueryService;
    private final MatchQueryService matchQueryService;

    public SummonerController(
            SummonerSyncService summonerSyncService,
            SummonerQueryService summonerQueryService,
            MatchQueryService matchQueryService) {
        this.summonerSyncService = summonerSyncService;
        this.summonerQueryService = summonerQueryService;
        this.matchQueryService = matchQueryService;
    }

    /**
     * Riot ID 기준으로 소환사 정보를 동기화합니다.
     *
     * @param request 소환사 동기화 요청 정보
     * @return 동기화 결과 응답
     */
    @PostMapping("/sync")
    @Operation(summary = "소환사 동기화", description = "Riot ID로 계정, 소환사, 최근 매치 정보를 수집해 내부 저장소에 동기화합니다.")
    public SummonerSyncResponse sync(@Valid @RequestBody SummonerSyncRequest request) {
        return SummonerSyncResponse.from(
                summonerSyncService.sync(request.gameName(), request.tagLine(), request.matchCount()));
    }

    /**
     * Riot ID로 저장된 소환사 프로필을 조회합니다.
     *
     * @param gameName 라이엇 게임 이름
     * @param tagLine 라이엇 태그 라인
     * @return 소환사 프로필 응답
     */
    @GetMapping("/by-riot-id")
    @Operation(summary = "Riot ID로 소환사 조회", description = "gameName과 tagLine으로 저장된 소환사 프로필을 조회합니다.")
    public SummonerProfileResponse getByRiotId(
            @Parameter(description = "라이엇 게임 이름", example = "Hide on bush")
            @RequestParam @NotBlank String gameName,
            @Parameter(description = "라이엇 태그 라인", example = "KR1")
            @RequestParam @NotBlank String tagLine) {
        return SummonerProfileResponse.from(summonerQueryService.getByRiotId(gameName, tagLine));
    }

    /**
     * PUUID로 저장된 소환사 프로필을 조회합니다.
     *
     * @param puuid Riot PUUID
     * @return 소환사 프로필 응답
     */
    @GetMapping("/{puuid}")
    @Operation(summary = "PUUID로 소환사 조회", description = "내부 저장소에 저장된 소환사 프로필을 PUUID 기준으로 조회합니다.")
    public SummonerProfileResponse getByPuuid(
            @Parameter(description = "Riot PUUID", example = "sample-puuid-value") @PathVariable String puuid) {
        return SummonerProfileResponse.from(summonerQueryService.getByPuuid(puuid));
    }

    /**
     * 소환사의 최근 경기 목록을 조회합니다.
     *
     * @param puuid Riot PUUID
     * @param count 조회할 경기 수
     * @return 최근 경기 목록 응답
     */
    @GetMapping("/{puuid}/matches")
    @Operation(summary = "소환사 최근 경기 조회", description = "저장된 최근 경기 목록을 PUUID 기준으로 조회합니다.")
    public SummonerMatchListResponse getRecentMatches(
            @Parameter(description = "Riot PUUID", example = "sample-puuid-value") @PathVariable String puuid,
            @Parameter(description = "조회할 경기 수", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(20) int count) {
        return SummonerMatchListResponse.from(puuid, matchQueryService.getRecentMatches(puuid, count));
    }
}
