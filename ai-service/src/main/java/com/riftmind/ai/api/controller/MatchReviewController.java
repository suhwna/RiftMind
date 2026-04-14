package com.riftmind.ai.api.controller;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.riftmind.ai.api.response.MatchReviewResponse;
import com.riftmind.ai.application.service.MatchReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;

/**
 * AI 경기 회고 API를 제공합니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
@Validated
@Tag(name = "AI Review", description = "AI 경기 회고 API")
@RestController
@RequestMapping(value = "/api/v1/ai/matches", produces = MediaType.APPLICATION_JSON_VALUE)
public class MatchReviewController {

    private final MatchReviewService matchReviewService;

    public MatchReviewController(MatchReviewService matchReviewService) {
        this.matchReviewService = matchReviewService;
    }

    /**
     * 특정 플레이어 기준 경기 AI 회고를 생성합니다.
     *
     * @param matchId Riot matchId
     * @param puuid 회고 대상 PUUID
     * @return 경기 AI 회고 응답
     */
    @PostMapping("/{matchId}/review")
    @Operation(summary = "경기 AI 회고 생성", description = "match-service의 경기 상세 데이터를 바탕으로 AI 회고를 생성합니다.")
    public MatchReviewResponse reviewMatch(
            @Parameter(description = "Riot matchId", example = "KR_1234567890") @PathVariable String matchId,
            @Parameter(description = "회고 대상 PUUID", example = "sample-puuid-value")
            @RequestParam @NotBlank String puuid) {
        return MatchReviewResponse.from(matchReviewService.generateReview(matchId, puuid));
    }
}
