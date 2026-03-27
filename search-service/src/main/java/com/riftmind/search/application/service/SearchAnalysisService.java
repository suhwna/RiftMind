package com.riftmind.search.application.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.riftmind.search.api.response.SearchOverviewChampionResponse;
import com.riftmind.search.api.response.SearchOverviewPositionResponse;
import com.riftmind.search.api.response.SearchOverviewResponse;
import com.riftmind.search.domain.search.MatchSearchDocument;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 최근 경기 문서를 바탕으로 플레이 패턴 요약을 계산합니다.
 *
 * @author 정수환
 * @since 2026-03-27
 */
@Service
public class SearchAnalysisService {

    private final ElasticsearchOperations elasticsearchOperations;

    public SearchAnalysisService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /**
     * PUUID 기준 최근 N판 플레이 요약을 반환합니다.
     *
     * @param puuid Riot PUUID
     * @param matchCount 분석할 최근 경기 수
     * @return 플레이 패턴 요약 응답
     */
    public SearchOverviewResponse getOverview(String puuid, int matchCount) {
        NativeQuery query = new NativeQueryBuilder()
                .withQuery(termQuery("puuid", puuid))
                .withSort(sort -> sort.field(field -> field.field("gameCreation").order(SortOrder.Desc)))
                .withPageable(PageRequest.of(0, matchCount))
                .build();

        SearchHits<MatchSearchDocument> searchHits =
                elasticsearchOperations.search(query, MatchSearchDocument.class);

        List<MatchSearchDocument> matches = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        if (matches.isEmpty()) {
            return new SearchOverviewResponse(
                    puuid,
                    matchCount,
                    0,
                    searchHits.getTotalHits(),
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    List.of(),
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    List.of("색인된 최근 경기가 없습니다. 전적을 다시 동기화해 주세요.")
            );
        }

        int analyzedMatchCount = matches.size();
        int winCount = (int) matches.stream().filter(MatchSearchDocument::isWin).count();
        int lossCount = analyzedMatchCount - winCount;
        int winRate = toPercent(winCount, analyzedMatchCount);
        double averageKda = roundToTwo(matches.stream().mapToDouble(MatchSearchDocument::getKda).average().orElse(0));
        int averageDamage = roundToInt(matches.stream().mapToInt(MatchSearchDocument::getTotalDamageDealtToChampions).average().orElse(0));
        int averageGold = roundToInt(matches.stream().mapToInt(MatchSearchDocument::getGoldEarned).average().orElse(0));
        int averageCs = roundToInt(matches.stream().mapToInt(MatchSearchDocument::getTotalCs).average().orElse(0));
        int averageVisionScore = roundToInt(matches.stream().mapToInt(MatchSearchDocument::getVisionScore).average().orElse(0));

        List<SearchOverviewChampionResponse> topPlayedChampions = findTopPlayedChampions(matches, 5);
        SearchOverviewChampionResponse mostPlayedChampion = findMostPlayedChampion(matches);
        SearchOverviewChampionResponse bestChampion = findBestChampion(matches);
        List<MatchSearchDocument> mainChampionMatches = filterMainChampionMatches(matches, mostPlayedChampion);
        SearchOverviewChampionResponse mainChampionFrequentOpponentChampion =
                findFrequentOpponentChampion(mainChampionMatches);
        SearchOverviewChampionResponse mainChampionToughestOpponentChampion =
                findToughestOpponentChampion(mainChampionMatches);
        List<String> mainChampionFrequentItemNames = findFrequentItemNames(mainChampionMatches);
        SearchOverviewChampionResponse toughestOpponentChampion = findToughestOpponentChampion(matches);
        SearchOverviewChampionResponse frequentOpponentChampion = findFrequentOpponentChampion(matches);
        SearchOverviewPositionResponse bestPosition = findBestPosition(matches);
        SearchOverviewPositionResponse weakPosition = findWeakPosition(matches);
        List<String> frequentItemNames = findFrequentItemNames(matches);

        return new SearchOverviewResponse(
                puuid,
                matchCount,
                analyzedMatchCount,
                searchHits.getTotalHits(),
                winCount,
                lossCount,
                winRate,
                averageKda,
                averageDamage,
                averageGold,
                averageCs,
                averageVisionScore,
                topPlayedChampions,
                mostPlayedChampion,
                bestChampion,
                mainChampionFrequentOpponentChampion,
                mainChampionToughestOpponentChampion,
                mainChampionFrequentItemNames,
                toughestOpponentChampion,
                frequentOpponentChampion,
                bestPosition,
                weakPosition,
                frequentItemNames,
                buildInsights(
                        matches,
                        winRate,
                        averageKda,
                        mostPlayedChampion,
                        bestChampion,
                        mainChampionFrequentOpponentChampion,
                        mainChampionToughestOpponentChampion,
                        mainChampionFrequentItemNames)
        );
    }

    private List<String> buildInsights(
            List<MatchSearchDocument> matches,
            int winRate,
            double averageKda,
            SearchOverviewChampionResponse mostPlayedChampion,
            SearchOverviewChampionResponse bestChampion,
            SearchOverviewChampionResponse mainChampionFrequentOpponentChampion,
            SearchOverviewChampionResponse mainChampionToughestOpponentChampion,
            List<String> mainChampionFrequentItemNames
    ) {
        List<String> insights = new ArrayList<>();

        insights.add("최근 " + matches.size() + "판 승률은 " + winRate + "%, 평균 KDA는 " + formatDecimal(averageKda) + "입니다.");

        if (mostPlayedChampion != null) {
            insights.add("가장 많이 플레이한 챔피언은 " + mostPlayedChampion.championNameKo()
                    + "이며 " + mostPlayedChampion.matchCount() + "판을 플레이했습니다.");
        }

        if (bestChampion != null && bestChampion.matchCount() >= 2) {
            insights.add(bestChampion.championNameKo() + " 플레이 성과가 가장 좋았습니다. 승률 "
                    + bestChampion.winRate() + "%, 평균 KDA " + formatDecimal(bestChampion.averageKda()) + "입니다.");
        } else {
            double winDamage = matches.stream()
                    .filter(MatchSearchDocument::isWin)
                    .mapToInt(MatchSearchDocument::getTotalDamageDealtToChampions)
                    .average()
                    .orElse(0);
            double lossDeaths = matches.stream()
                    .filter(match -> !match.isWin())
                    .mapToInt(MatchSearchDocument::getDeaths)
                    .average()
                    .orElse(0);
            if (winDamage > 0) {
                insights.add("승리 경기에서는 평균 딜량이 " + roundToInt(winDamage)
                        + "로, 교전 영향력이 더 높았습니다.");
            } else if (lossDeaths > 0) {
                insights.add("패배 경기에서는 평균 데스가 " + formatDecimal(lossDeaths)
                        + "로 높게 나타났습니다.");
            }
        }

        if (mostPlayedChampion != null && mainChampionToughestOpponentChampion != null
                && mainChampionToughestOpponentChampion.matchCount() >= 2) {
            insights.add(mostPlayedChampion.championNameKo() + " 플레이에서는 "
                    + mainChampionToughestOpponentChampion.championNameKo() + " 상대로 승률 "
                    + mainChampionToughestOpponentChampion.winRate() + "%였습니다.");
        } else if (mostPlayedChampion != null && mainChampionFrequentOpponentChampion != null
                && mainChampionFrequentOpponentChampion.matchCount() >= 2) {
            insights.add(mostPlayedChampion.championNameKo() + "로 가장 자주 만난 상대는 "
                    + mainChampionFrequentOpponentChampion.championNameKo() + "입니다.");
        }

        if (mostPlayedChampion != null && !mainChampionFrequentItemNames.isEmpty()) {
            insights.add(mostPlayedChampion.championNameKo() + "에서 자주 간 아이템은 "
                    + String.join(", ", mainChampionFrequentItemNames) + "입니다.");
        }

        return insights.stream().limit(3).toList();
    }

    private List<MatchSearchDocument> filterMainChampionMatches(
            List<MatchSearchDocument> matches,
            SearchOverviewChampionResponse mostPlayedChampion
    ) {
        if (mostPlayedChampion == null) {
            return List.of();
        }

        return matches.stream()
                .filter(match -> mostPlayedChampion.championName().equals(match.getChampionName()))
                .toList();
    }

    private SearchOverviewChampionResponse findMostPlayedChampion(List<MatchSearchDocument> matches) {
        return findTopPlayedChampions(matches, 1).stream()
                .findFirst()
                .orElse(null);
    }

    private List<SearchOverviewChampionResponse> findTopPlayedChampions(List<MatchSearchDocument> matches, int limit) {
        return groupByChampion(matches).values().stream()
                .sorted(Comparator.comparingInt(ChampionSummary::matchCount).reversed()
                        .thenComparingInt(ChampionSummary::winRate).reversed()
                        .thenComparingDouble(ChampionSummary::averageKda).reversed())
                .map(this::toChampionResponse)
                .limit(limit)
                .toList();
    }

    private SearchOverviewChampionResponse findBestChampion(List<MatchSearchDocument> matches) {
        List<ChampionSummary> summaries = groupByChampion(matches).values().stream().toList();
        List<ChampionSummary> candidates = summaries.stream()
                .filter(summary -> summary.matchCount() >= 2)
                .toList();

        return (candidates.isEmpty() ? summaries : candidates).stream()
                .sorted(Comparator.comparingInt(ChampionSummary::winRate).reversed()
                        .thenComparingDouble(ChampionSummary::averageKda).reversed()
                        .thenComparingInt(ChampionSummary::matchCount).reversed())
                .map(this::toChampionResponse)
                .findFirst()
                .orElse(null);
    }

    private SearchOverviewChampionResponse findToughestOpponentChampion(List<MatchSearchDocument> matches) {
        List<ChampionSummary> summaries = groupByOpponentChampion(matches).values().stream()
                .filter(summary -> summary.matchCount() >= 2)
                .toList();
        if (summaries.isEmpty()) {
            return null;
        }

        return summaries.stream()
                .sorted(Comparator.comparingInt(ChampionSummary::winRate)
                        .thenComparingDouble(ChampionSummary::averageKda)
                        .thenComparingInt(ChampionSummary::matchCount).reversed())
                .map(this::toChampionResponse)
                .findFirst()
                .orElse(null);
    }

    private SearchOverviewChampionResponse findFrequentOpponentChampion(List<MatchSearchDocument> matches) {
        return groupByOpponentChampion(matches).values().stream()
                .sorted(Comparator.comparingInt(ChampionSummary::matchCount).reversed()
                        .thenComparingInt(ChampionSummary::winRate))
                .map(this::toChampionResponse)
                .findFirst()
                .orElse(null);
    }

    private SearchOverviewPositionResponse findBestPosition(List<MatchSearchDocument> matches) {
        List<PositionSummary> summaries = groupByPosition(matches).values().stream().toList();
        if (summaries.isEmpty()) {
            return null;
        }

        List<PositionSummary> candidates = summaries.stream()
                .filter(summary -> summary.matchCount() >= 2)
                .toList();

        return (candidates.isEmpty() ? summaries : candidates).stream()
                .sorted(Comparator.comparingInt(PositionSummary::winRate).reversed()
                        .thenComparingDouble(PositionSummary::averageKda).reversed()
                        .thenComparingInt(PositionSummary::matchCount).reversed())
                .map(this::toPositionResponse)
                .findFirst()
                .orElse(null);
    }

    private SearchOverviewPositionResponse findWeakPosition(List<MatchSearchDocument> matches) {
        List<PositionSummary> summaries = groupByPosition(matches).values().stream()
                .filter(summary -> summary.matchCount() >= 2)
                .toList();
        if (summaries.isEmpty()) {
            return null;
        }

        return summaries.stream()
                .sorted(Comparator.comparingInt(PositionSummary::winRate)
                        .thenComparingDouble(PositionSummary::averageKda)
                        .thenComparingInt(PositionSummary::matchCount).reversed())
                .map(this::toPositionResponse)
                .findFirst()
                .orElse(null);
    }

    private Map<String, ChampionSummary> groupByChampion(List<MatchSearchDocument> matches) {
        Map<String, List<MatchSearchDocument>> grouped = new LinkedHashMap<>();
        for (MatchSearchDocument match : matches) {
            grouped.computeIfAbsent(match.getChampionName(), ignored -> new ArrayList<>()).add(match);
        }

        Map<String, ChampionSummary> summaries = new LinkedHashMap<>();
        grouped.forEach((championName, championMatches) -> summaries.put(championName, new ChampionSummary(
                championName,
                championMatches.get(0).getChampionKey(),
                championMatches.get(0).getChampionNameKo(),
                championMatches.size(),
                (int) championMatches.stream().filter(MatchSearchDocument::isWin).count(),
                toPercent((int) championMatches.stream().filter(MatchSearchDocument::isWin).count(), championMatches.size()),
                roundToTwo(championMatches.stream().mapToDouble(MatchSearchDocument::getKda).average().orElse(0))
        )));
        return summaries;
    }

    private Map<String, PositionSummary> groupByPosition(List<MatchSearchDocument> matches) {
        Map<String, List<MatchSearchDocument>> grouped = new LinkedHashMap<>();
        for (MatchSearchDocument match : matches) {
            if (hasText(match.getTeamPosition())) {
                grouped.computeIfAbsent(match.getTeamPosition(), ignored -> new ArrayList<>()).add(match);
            }
        }

        Map<String, PositionSummary> summaries = new LinkedHashMap<>();
        grouped.forEach((teamPosition, positionMatches) -> summaries.put(teamPosition, new PositionSummary(
                teamPosition,
                positionMatches.get(0).getTeamPositionKo(),
                positionMatches.size(),
                (int) positionMatches.stream().filter(MatchSearchDocument::isWin).count(),
                toPercent((int) positionMatches.stream().filter(MatchSearchDocument::isWin).count(), positionMatches.size()),
                roundToTwo(positionMatches.stream().mapToDouble(MatchSearchDocument::getKda).average().orElse(0))
        )));
        return summaries;
    }

    private Map<String, ChampionSummary> groupByOpponentChampion(List<MatchSearchDocument> matches) {
        Map<String, List<MatchSearchDocument>> grouped = new LinkedHashMap<>();
        for (MatchSearchDocument match : matches) {
            if (hasText(match.getOpponentChampionName())) {
                grouped.computeIfAbsent(match.getOpponentChampionName(), ignored -> new ArrayList<>()).add(match);
            }
        }

        Map<String, ChampionSummary> summaries = new LinkedHashMap<>();
        grouped.forEach((championName, championMatches) -> summaries.put(championName, new ChampionSummary(
                championName,
                championMatches.get(0).getOpponentChampionKey(),
                championMatches.get(0).getOpponentChampionNameKo(),
                championMatches.size(),
                (int) championMatches.stream().filter(MatchSearchDocument::isWin).count(),
                toPercent((int) championMatches.stream().filter(MatchSearchDocument::isWin).count(), championMatches.size()),
                roundToTwo(championMatches.stream().mapToDouble(MatchSearchDocument::getKda).average().orElse(0))
        )));
        return summaries;
    }

    private List<String> findFrequentItemNames(List<MatchSearchDocument> matches) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (MatchSearchDocument match : matches) {
            for (String itemName : match.getItemNames()) {
                if (hasText(itemName)) {
                    counts.merge(itemName, 1, Integer::sum);
                }
            }
        }

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(3)
                .toList();
    }

    private SearchOverviewChampionResponse toChampionResponse(ChampionSummary summary) {
        return new SearchOverviewChampionResponse(
                summary.championName(),
                summary.championKey(),
                summary.championNameKo(),
                summary.matchCount(),
                summary.winCount(),
                summary.winRate(),
                summary.averageKda()
        );
    }

    private SearchOverviewPositionResponse toPositionResponse(PositionSummary summary) {
        return new SearchOverviewPositionResponse(
                summary.teamPosition(),
                summary.teamPositionKo(),
                summary.matchCount(),
                summary.winCount(),
                summary.winRate(),
                summary.averageKda()
        );
    }

    private Query termQuery(String field, String value) {
        return Query.of(query -> query.term(term -> term.field(field).value(value)));
    }

    private int toPercent(int value, int total) {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.round((value * 100.0) / total);
    }

    private double roundToTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private int roundToInt(double value) {
        return (int) Math.round(value);
    }

    private String formatDecimal(double value) {
        return String.format("%.2f", value);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record ChampionSummary(
            String championName,
            String championKey,
            String championNameKo,
            int matchCount,
            int winCount,
            int winRate,
            double averageKda
    ) {
    }

    private record PositionSummary(
            String teamPosition,
            String teamPositionKo,
            int matchCount,
            int winCount,
            int winRate,
            double averageKda
    ) {
    }
}
