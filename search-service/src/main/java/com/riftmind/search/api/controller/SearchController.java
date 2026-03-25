package com.riftmind.search.api.controller;

import com.riftmind.search.api.request.MatchIndexRequest;
import com.riftmind.search.api.response.MatchIndexResponse;
import com.riftmind.search.api.response.SearchMatchListResponse;
import com.riftmind.search.application.service.SearchIndexService;
import com.riftmind.search.application.service.SearchQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 검색 색인 및 매치 검색 API를 제공합니다.
 *
 * @author 정수환
 * @since 2026-03-25
 */
@Validated
@Tag(name = "Search", description = "Elasticsearch 기반 매치 검색 API")
@RestController
@RequestMapping(value = "/api/v1/search", produces = MediaType.APPLICATION_JSON_VALUE)
public class SearchController {

    private final SearchIndexService searchIndexService;
    private final SearchQueryService searchQueryService;

    public SearchController(SearchIndexService searchIndexService, SearchQueryService searchQueryService) {
        this.searchIndexService = searchIndexService;
        this.searchQueryService = searchQueryService;
    }

    /**
     * PUUID 기준 최근 경기 참가자 데이터를 검색 인덱스에 색인합니다.
     *
     * @param request 색인 요청
     * @return 색인 결과 응답
     */
    @Operation(summary = "최근 경기 색인", description = "match-service의 최근 참가자 상세를 검색 인덱스에 저장합니다.")
    @PostMapping(value = "/index/matches", consumes = MediaType.APPLICATION_JSON_VALUE)
    public MatchIndexResponse indexRecentMatches(@Valid @RequestBody MatchIndexRequest request) {
        int matchCount = request.matchCount() == null ? 20 : request.matchCount();
        return searchIndexService.indexRecentMatches(request.puuid(), matchCount);
    }

    /**
     * 다양한 조건으로 매치를 검색합니다.
     *
     * @param puuid Riot PUUID
     * @param championName 챔피언 영문 이름
     * @param teamPosition 포지션
     * @param queueId 큐 ID
     * @param win 승리 여부
     * @param itemName 아이템 한글 이름
     * @param summonerSpellName 소환사 주문 한글 이름
     * @param primaryRuneName 주 룬 스타일 이름
     * @param secondaryRuneName 보조 룬 스타일 이름
     * @param minKda 최소 KDA
     * @param maxKda 최대 KDA
     * @param minDamage 최소 챔피언 대상 피해량
     * @param maxDamage 최대 챔피언 대상 피해량
     * @param minGold 최소 획득 골드
     * @param maxGold 최대 획득 골드
     * @param minCs 최소 총 CS
     * @param maxCs 최대 총 CS
     * @param minVisionScore 최소 시야 점수
     * @param maxVisionScore 최대 시야 점수
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 검색 결과 응답
     */
    @Operation(summary = "매치 검색", description = "챔피언, 포지션, 아이템, 룬, 스펠, 승패, KDA, 딜량, 골드, CS, 시야 기준으로 매치를 검색합니다.")
    @GetMapping("/matches")
    public SearchMatchListResponse searchMatches(
            @RequestParam(required = false) String puuid,
            @RequestParam(required = false) String championName,
            @RequestParam(required = false) String teamPosition,
            @RequestParam(required = false) Integer queueId,
            @RequestParam(required = false) Boolean win,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String summonerSpellName,
            @RequestParam(required = false) String primaryRuneName,
            @RequestParam(required = false) String secondaryRuneName,
            @RequestParam(required = false) Double minKda,
            @RequestParam(required = false) Double maxKda,
            @RequestParam(required = false) Integer minDamage,
            @RequestParam(required = false) Integer maxDamage,
            @RequestParam(required = false) Integer minGold,
            @RequestParam(required = false) Integer maxGold,
            @RequestParam(required = false) Integer minCs,
            @RequestParam(required = false) Integer maxCs,
            @RequestParam(required = false) Integer minVisionScore,
            @RequestParam(required = false) Integer maxVisionScore,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return searchQueryService.searchMatches(
                puuid,
                championName,
                teamPosition,
                queueId,
                win,
                itemName,
                summonerSpellName,
                primaryRuneName,
                secondaryRuneName,
                minKda,
                maxKda,
                minDamage,
                maxDamage,
                minGold,
                maxGold,
                minCs,
                maxCs,
                minVisionScore,
                maxVisionScore,
                page,
                size
        );
    }
}
