package com.riftmind.search.api.controller;

import com.riftmind.search.api.request.MatchIndexRequest;
import com.riftmind.search.api.response.ChampionSuggestionListResponse;
import com.riftmind.search.api.response.MatchIndexResponse;
import com.riftmind.search.api.response.SearchFilterOptionsResponse;
import com.riftmind.search.api.response.SearchMatchListResponse;
import com.riftmind.search.api.response.SearchOverviewResponse;
import com.riftmind.search.application.service.SearchAnalysisService;
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

    private final SearchAnalysisService searchAnalysisService;
    private final SearchIndexService searchIndexService;
    private final SearchQueryService searchQueryService;

    public SearchController(
            SearchAnalysisService searchAnalysisService,
            SearchIndexService searchIndexService,
            SearchQueryService searchQueryService
    ) {
        this.searchAnalysisService = searchAnalysisService;
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
     * 챔피언 이름 자동완성 목록을 조회합니다.
     *
     * @param keyword 사용자 입력 키워드
     * @param size 최대 자동완성 개수
     * @return 자동완성 목록 응답
     */
    @Operation(summary = "챔피언 자동완성", description = "한글/영문 챔피언 이름 기준 자동완성 목록을 조회합니다.")
    @GetMapping("/champions/suggestions")
    public ChampionSuggestionListResponse suggestChampions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "8") @Min(1) @Max(20) int size
    ) {
        return searchQueryService.suggestChampions(keyword, size);
    }

    /**
     * 현재 소환사 기준 고급 검색 필터 옵션을 조회합니다.
     *
     * @param puuid Riot PUUID
     * @return 필터 옵션 응답
     */
    @Operation(summary = "검색 필터 옵션", description = "현재 소환사 기준으로 챔피언, 포지션, 게임 모드 필터 옵션을 조회합니다.")
    @GetMapping("/filter-options")
    public SearchFilterOptionsResponse getFilterOptions(@RequestParam String puuid) {
        return searchQueryService.getFilterOptions(puuid);
    }

    /**
     * 현재 소환사의 최근 경기 패턴 요약을 조회합니다.
     *
     * @param puuid Riot PUUID
     * @param count 분석할 최근 경기 수
     * @return 플레이 패턴 요약 응답
     */
    @Operation(summary = "플레이 요약", description = "최근 N판 기준으로 전역 요약, 최근 추세, 챔피언별 분석 카드와 인사이트를 반환합니다.")
    @GetMapping("/overview")
    public SearchOverviewResponse getOverview(
            @RequestParam String puuid,
            @RequestParam(defaultValue = "10") @Min(5) @Max(100) int count
    ) {
        return searchAnalysisService.getOverview(puuid, count);
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
            @RequestParam(required = false) java.util.List<String> championNames,
            @RequestParam(required = false) java.util.List<String> teamPositions,
            @RequestParam(required = false) java.util.List<Integer> queueIds,
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
                championNames,
                teamPositions,
                queueIds,
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
