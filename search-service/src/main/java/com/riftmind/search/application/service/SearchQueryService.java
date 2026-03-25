package com.riftmind.search.application.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
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
import java.util.List;

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
            String championName,
            String teamPosition,
            Integer queueId,
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

    private Query buildQuery(
            String puuid,
            String championName,
            String teamPosition,
            Integer queueId,
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
        if (hasText(championName)) {
            filters.add(termQuery("championName", championName));
        }
        if (hasText(teamPosition)) {
            filters.add(termQuery("teamPosition", teamPosition));
        }
        if (queueId != null) {
            filters.add(termQuery("queueId", queueId));
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
