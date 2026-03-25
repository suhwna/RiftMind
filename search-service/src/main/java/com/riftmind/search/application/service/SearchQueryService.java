package com.riftmind.search.application.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.riftmind.search.api.response.ChampionSuggestionListResponse;
import com.riftmind.search.api.response.ChampionSuggestionResponse;
import com.riftmind.search.api.response.SearchFilterChampionOptionResponse;
import com.riftmind.search.api.response.SearchFilterModeOptionResponse;
import com.riftmind.search.api.response.SearchFilterOptionsResponse;
import com.riftmind.search.api.response.SearchFilterPositionOptionResponse;
import com.riftmind.search.api.response.SearchMatchListResponse;
import com.riftmind.search.api.response.SearchMatchResponse;
import com.riftmind.search.domain.search.MatchSearchDocument;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Elasticsearch 기반 매치 검색을 담당합니다.
 *
 * @author 정수환
 * @since 2026-03-25
 */
@Service
public class SearchQueryService {

    private final ElasticsearchOperations elasticsearchOperations;

    public SearchQueryService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
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
    public SearchMatchListResponse searchMatches(
            String puuid,
            List<String> championNames,
            List<String> teamPositions,
            List<Integer> queueIds,
            Boolean win,
            String itemName,
            String summonerSpellName,
            String primaryRuneName,
            String secondaryRuneName,
            Double minKda,
            Double maxKda,
            Integer minDamage,
            Integer maxDamage,
            Integer minGold,
            Integer maxGold,
            Integer minCs,
            Integer maxCs,
            Integer minVisionScore,
            Integer maxVisionScore,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Query query = buildQuery(
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
                maxVisionScore
        );

        NativeQuery searchQuery = new NativeQueryBuilder()
                .withQuery(query)
                .withSort(sort -> sort.field(field -> field.field("gameCreation").order(SortOrder.Desc)))
                .withPageable(pageable)
                .build();

        SearchHits<MatchSearchDocument> searchHits =
                elasticsearchOperations.search(searchQuery, MatchSearchDocument.class);

        List<SearchMatchResponse> matches = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(SearchMatchResponse::from)
                .toList();

        return new SearchMatchListResponse(searchHits.getTotalHits(), page, size, matches);
    }

    /**
     * 현재 소환사 기준으로 체크형 고급 검색 필터 옵션을 계산합니다.
     *
     * @param puuid Riot PUUID
     * @return 필터 옵션 응답
     */
    public SearchFilterOptionsResponse getFilterOptions(String puuid) {
        NativeQuery query = new NativeQueryBuilder()
                .withQuery(termQuery("puuid", puuid))
                .withPageable(PageRequest.of(0, 100))
                .build();

        SearchHits<MatchSearchDocument> searchHits =
                elasticsearchOperations.search(query, MatchSearchDocument.class);

        Map<String, SearchFilterChampionOptionResponse> champions = new LinkedHashMap<>();
        Map<String, SearchFilterPositionOptionResponse> positions = new LinkedHashMap<>();
        Map<Integer, SearchFilterModeOptionResponse> modes = new LinkedHashMap<>();

        searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .forEach(document -> {
                    champions.putIfAbsent(
                            document.getChampionName(),
                            new SearchFilterChampionOptionResponse(document.getChampionName(), document.getChampionNameKo())
                    );

                    if (hasText(document.getTeamPosition())) {
                        positions.putIfAbsent(
                                document.getTeamPosition(),
                                new SearchFilterPositionOptionResponse(document.getTeamPosition(), document.getTeamPositionKo())
                        );
                    }

                    if (document.getQueueId() != null && hasText(document.getQueueNameKo())) {
                        Integer normalizedQueueId = document.getQueueId() == 1710 ? 1700 : document.getQueueId();
                        modes.putIfAbsent(
                                normalizedQueueId,
                                new SearchFilterModeOptionResponse(normalizedQueueId, document.getQueueNameKo())
                        );
                    }
                });

        return new SearchFilterOptionsResponse(
                champions.values().stream()
                        .sorted((left, right) -> left.championNameKo().compareToIgnoreCase(right.championNameKo()))
                        .toList(),
                positions.values().stream()
                        .sorted((left, right) -> left.teamPositionKo().compareToIgnoreCase(right.teamPositionKo()))
                        .toList(),
                modes.values().stream()
                        .sorted((left, right) -> left.queueNameKo().compareToIgnoreCase(right.queueNameKo()))
                        .toList()
        );
    }

    /**
     * 한글/영문 챔피언 이름으로 자동완성 목록을 조회합니다.
     *
     * @param keyword 사용자 입력 키워드
     * @param size 최대 자동완성 개수
     * @return 자동완성 목록 응답
     */
    public ChampionSuggestionListResponse suggestChampions(String keyword, int size) {
        if (!hasText(keyword)) {
            return new ChampionSuggestionListResponse(List.of());
        }

        NativeQuery query = new NativeQueryBuilder()
                .withQuery(championPrefixQuery(keyword))
                .withPageable(PageRequest.of(0, Math.max(size * 4, size)))
                .build();

        SearchHits<MatchSearchDocument> searchHits =
                elasticsearchOperations.search(query, MatchSearchDocument.class);

        Map<String, ChampionSuggestionResponse> suggestions = new LinkedHashMap<>();

        searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .forEach(document -> suggestions.putIfAbsent(
                        document.getChampionName(),
                        new ChampionSuggestionResponse(document.getChampionName(), document.getChampionNameKo())
                ));

        return new ChampionSuggestionListResponse(
                suggestions.values().stream()
                        .limit(size)
                        .toList()
        );
    }

    private Query buildQuery(
            String puuid,
            List<String> championNames,
            List<String> teamPositions,
            List<Integer> queueIds,
            Boolean win,
            String itemName,
            String summonerSpellName,
            String primaryRuneName,
            String secondaryRuneName,
            Double minKda,
            Double maxKda,
            Integer minDamage,
            Integer maxDamage,
            Integer minGold,
            Integer maxGold,
            Integer minCs,
            Integer maxCs,
            Integer minVisionScore,
            Integer maxVisionScore
    ) {
        List<Query> filters = new ArrayList<>();

        if (hasText(puuid)) {
            filters.add(termQuery("puuid", puuid));
        }
        if (hasValues(championNames)) {
            filters.add(anyChampionQuery(championNames));
        }
        if (hasValues(teamPositions)) {
            filters.add(anyStringTermsQuery("teamPosition", teamPositions));
        }
        if (hasValues(queueIds)) {
            filters.add(anyQueueQuery(queueIds));
        }
        if (win != null) {
            filters.add(Query.of(query -> query.term(term -> term.field("win").value(win))));
        }
        if (hasText(itemName)) {
            filters.add(termQuery("itemNames", itemName));
        }
        if (hasText(summonerSpellName)) {
            filters.add(termQuery("summonerSpellNames", summonerSpellName));
        }
        if (hasText(primaryRuneName)) {
            filters.add(termQuery("primaryRuneName", primaryRuneName));
        }
        if (hasText(secondaryRuneName)) {
            filters.add(termQuery("secondaryRuneName", secondaryRuneName));
        }
        addDoubleRange(filters, "kda", minKda, maxKda);
        addIntegerRange(filters, "totalDamageDealtToChampions", minDamage, maxDamage);
        addIntegerRange(filters, "goldEarned", minGold, maxGold);
        addIntegerRange(filters, "totalCs", minCs, maxCs);
        addIntegerRange(filters, "visionScore", minVisionScore, maxVisionScore);

        if (filters.isEmpty()) {
            return Query.of(query -> query.matchAll(matchAll -> matchAll));
        }

        return Query.of(query -> query.bool(bool -> bool.filter(filters)));
    }

    private void addDoubleRange(List<Query> filters, String field, Double minValue, Double maxValue) {
        if (minValue == null && maxValue == null) {
            return;
        }
        filters.add(Query.of(query -> query.range(range -> range.number(number -> {
            number.field(field);
            if (minValue != null) {
                number.gte(minValue);
            }
            if (maxValue != null) {
                number.lte(maxValue);
            }
            return number;
        }))));
    }

    private void addIntegerRange(List<Query> filters, String field, Integer minValue, Integer maxValue) {
        if (minValue == null && maxValue == null) {
            return;
        }
        filters.add(Query.of(query -> query.range(range -> range.number(number -> {
            number.field(field);
            if (minValue != null) {
                number.gte((double) minValue);
            }
            if (maxValue != null) {
                number.lte((double) maxValue);
            }
            return number;
        }))));
    }


    /**
     * 문자열 필드에 대한 exact term 쿼리를 생성합니다.
     *
     * @param field 필드명
     * @param value 검색 값
     * @return term 쿼리
     */
    private Query termQuery(String field, String value) {
        return Query.of(query -> query.term(term -> term.field(field).value(value)));
    }

    /**
     * 챔피언 이름은 영문/한글 양쪽 필드에서 부분 일치로 검색합니다.
     *
     * @param championName 사용자가 입력한 챔피언 이름
     * @return 챔피언 이름 검색 쿼리
     */
    private Query championNameQuery(String championName) {
        String keyword = "*" + championName.trim() + "*";

        return Query.of(query -> query.bool(bool -> bool.should(
                wildcardQuery("championName", keyword),
                wildcardQuery("championNameKo", keyword)
        ).minimumShouldMatch("1")));
    }

    private Query anyChampionQuery(List<String> championNames) {
        return Query.of(query -> query.bool(bool -> bool.should(
                championNames.stream()
                        .filter(this::hasText)
                        .map(name -> termQuery("championName", name))
                        .toList()
        ).minimumShouldMatch("1")));
    }

    private Query championPrefixQuery(String championName) {
        String keyword = championName.trim() + "*";

        return Query.of(query -> query.bool(bool -> bool.should(
                wildcardQuery("championName", keyword),
                wildcardQuery("championNameKo", keyword)
        ).minimumShouldMatch("1")));
    }

    /**
     * 숫자 필드에 대한 exact term 쿼리를 생성합니다.
     *
     * @param field 필드명
     * @param value 검색 값
     * @return term 쿼리
     */
    private Query termQuery(String field, Integer value) {
        return Query.of(query -> query.term(term -> term
                .field(field)
                .value(FieldValue.of(value.longValue()))));
    }

    /**
     * 아레나는 사용자 표시상 하나로 다루되 내부적으로는 1700, 1710을 함께 조회합니다.
     *
     * @param queueId 사용자가 선택한 queueId
     * @return queue 필터 쿼리
     */
    private Query queueIdQuery(Integer queueId) {
        if (queueId == 1700) {
            return Query.of(query -> query.bool(bool -> bool.should(
                    termQuery("queueId", 1700),
                    termQuery("queueId", 1710)
            ).minimumShouldMatch("1")));
        }
        return termQuery("queueId", queueId);
    }

    private Query anyQueueQuery(List<Integer> queueIds) {
        return Query.of(query -> query.bool(bool -> bool.should(
                queueIds.stream()
                        .filter(Objects::nonNull)
                        .map(this::queueIdQuery)
                        .toList()
        ).minimumShouldMatch("1")));
    }

    private Query anyStringTermsQuery(String field, List<String> values) {
        return Query.of(query -> query.bool(bool -> bool.should(
                values.stream()
                        .filter(this::hasText)
                        .map(value -> termQuery(field, value))
                        .toList()
        ).minimumShouldMatch("1")));
    }

    private Query wildcardQuery(String field, String value) {
        return Query.of(query -> query.wildcard(wildcard -> wildcard
                .field(field)
                .value(value)
                .caseInsensitive(true)));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean hasValues(List<?> values) {
        return values != null && !values.isEmpty();
    }
}
