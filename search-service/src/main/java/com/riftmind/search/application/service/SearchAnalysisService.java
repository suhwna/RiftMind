package com.riftmind.search.application.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.riftmind.search.api.response.SearchOverviewChampionAnalysisResponse;
import com.riftmind.search.api.response.SearchOverviewChampionResponse;
import com.riftmind.search.api.response.SearchOverviewItemResponse;
import com.riftmind.search.api.response.SearchOverviewMatchupResponse;
import com.riftmind.search.api.response.SearchOverviewRecentTrendResponse;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 최근 경기 문서를 바탕으로 플레이 패턴 요약을 계산합니다.
 *
 * @author 정수환
 * @since 2026-03-27
 */
@Service
public class SearchAnalysisService {

    private static final int RECENT_TREND_MATCH_COUNT = 10;
    private static final int CHAMPION_ANALYSIS_LIMIT = 5;
    private static final Set<Integer> LOW_SIGNAL_ITEM_IDS = Set.of(
            0,
            2003,
            2031,
            2033,
            2055,
            2138,
            2139,
            2140,
            3340,
            3363,
            3364
    );

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
                    new SearchOverviewRecentTrendResponse(0, 0, 0, 0, 0, 0, List.of()),
                    List.of(),
                    List.of("색인된 최근 경기가 없습니다. 전적을 다시 동기화해 주세요.")
            );
        }

        MetricSummary overallMetrics = summarizeMetrics(matches);
        List<MatchSearchDocument> recentTrendMatches = matches.stream()
                .limit(Math.min(RECENT_TREND_MATCH_COUNT, matches.size()))
                .toList();
        MetricSummary recentTrendMetrics = summarizeMetrics(recentTrendMatches);

        List<SearchOverviewChampionResponse> topPlayedChampions =
                findTopPlayedChampions(matches, CHAMPION_ANALYSIS_LIMIT);
        SearchOverviewChampionResponse bestChampion = findBestChampion(matches);
        SearchOverviewRecentTrendResponse recentTrend =
                buildRecentTrend(recentTrendMatches, overallMetrics, recentTrendMetrics);
        List<SearchOverviewChampionAnalysisResponse> championAnalyses =
                buildChampionAnalyses(matches, topPlayedChampions, overallMetrics);

        return new SearchOverviewResponse(
                puuid,
                matchCount,
                matches.size(),
                searchHits.getTotalHits(),
                overallMetrics.winCount(),
                overallMetrics.lossCount(),
                overallMetrics.winRate(),
                overallMetrics.averageKda(),
                overallMetrics.averageDamage(),
                overallMetrics.averageGold(),
                overallMetrics.averageCs(),
                overallMetrics.averageVisionScore(),
                topPlayedChampions,
                bestChampion,
                recentTrend,
                championAnalyses,
                buildOverviewInsights(overallMetrics, recentTrend, championAnalyses)
        );
    }

    private SearchOverviewRecentTrendResponse buildRecentTrend(
            List<MatchSearchDocument> recentTrendMatches,
            MetricSummary overallMetrics,
            MetricSummary recentTrendMetrics
    ) {
        if (recentTrendMatches.isEmpty()) {
            return new SearchOverviewRecentTrendResponse(0, 0, 0, 0, 0, 0, List.of());
        }

        List<String> insights = new ArrayList<>();
        int winRateGap = recentTrendMetrics.winRate() - overallMetrics.winRate();
        if (Math.abs(winRateGap) >= 15) {
            insights.add("최근 " + recentTrendMatches.size() + "판 승률이 전체 기준보다 "
                    + Math.abs(winRateGap) + "%p " + (winRateGap > 0 ? "높습니다." : "낮습니다."));
        }

        double kdaGap = recentTrendMetrics.averageKda() - overallMetrics.averageKda();
        if (Math.abs(kdaGap) >= 0.7) {
            insights.add("최근 " + recentTrendMatches.size() + "판 평균 KDA가 "
                    + formatDecimal(recentTrendMetrics.averageKda()) + "로, 전체 기준보다 "
                    + (kdaGap > 0 ? "좋아졌습니다." : "낮아졌습니다."));
        }

        int damageGap = recentTrendMetrics.averageDamage() - overallMetrics.averageDamage();
        if (Math.abs(damageGap) >= 3500) {
            insights.add("최근 " + recentTrendMatches.size() + "판 평균 딜량은 "
                    + recentTrendMetrics.averageDamage() + "로, 전체 기준보다 "
                    + Math.abs(damageGap) + " " + (damageGap > 0 ? "높습니다." : "낮습니다."));
        }

        if (insights.isEmpty()) {
            insights.add("최근 " + recentTrendMatches.size() + "판 승률은 "
                    + recentTrendMetrics.winRate() + "%, 평균 KDA는 "
                    + formatDecimal(recentTrendMetrics.averageKda()) + "입니다.");
        }

        return new SearchOverviewRecentTrendResponse(
                recentTrendMatches.size(),
                recentTrendMetrics.winRate(),
                recentTrendMetrics.averageKda(),
                recentTrendMetrics.averageDamage(),
                recentTrendMetrics.averageCs(),
                recentTrendMetrics.averageVisionScore(),
                insights.stream().limit(3).toList()
        );
    }

    private List<SearchOverviewChampionAnalysisResponse> buildChampionAnalyses(
            List<MatchSearchDocument> matches,
            List<SearchOverviewChampionResponse> topPlayedChampions,
            MetricSummary overallMetrics
    ) {
        return topPlayedChampions.stream()
                .map(champion -> buildChampionAnalysis(champion, filterChampionMatches(matches, champion.championName()), overallMetrics))
                .toList();
    }

    private SearchOverviewChampionAnalysisResponse buildChampionAnalysis(
            SearchOverviewChampionResponse champion,
            List<MatchSearchDocument> championMatches,
            MetricSummary overallMetrics
    ) {
        MetricSummary championMetrics = summarizeMetrics(championMatches);
        List<SearchOverviewMatchupResponse> frequentOpponents = findFrequentOpponents(championMatches, 3);
        List<SearchOverviewMatchupResponse> favorableOpponents = findFavorableOpponents(championMatches, 3);
        List<SearchOverviewMatchupResponse> toughestOpponents = findToughestOpponents(championMatches, 3);
        List<SearchOverviewItemResponse> frequentItems = findFrequentItems(championMatches, 3);
        PositionSummary primaryPosition = findPrimaryPosition(championMatches);
        List<String> strengths = buildChampionStrengths(champion, championMetrics, overallMetrics, favorableOpponents, frequentOpponents);
        List<String> watchPoints = buildChampionWatchPoints(champion, championMetrics, overallMetrics, toughestOpponents);

        return new SearchOverviewChampionAnalysisResponse(
                champion.championName(),
                champion.championKey(),
                champion.championNameKo(),
                primaryPosition == null ? null : primaryPosition.teamPosition(),
                primaryPosition == null ? null : primaryPosition.teamPositionKo(),
                champion.matchCount(),
                champion.winCount(),
                champion.winRate(),
                champion.averageKda(),
                championMetrics.averageDamage(),
                championMetrics.averageGold(),
                championMetrics.averageCs(),
                championMetrics.averageVisionScore(),
                frequentOpponents,
                favorableOpponents,
                toughestOpponents,
                frequentItems,
                strengths,
                watchPoints,
                buildChampionInsights(
                        champion,
                        primaryPosition,
                        championMetrics,
                        overallMetrics,
                        frequentOpponents,
                        favorableOpponents,
                        toughestOpponents,
                        frequentItems,
                        strengths,
                        watchPoints
                )
        );
    }

    private List<String> buildChampionStrengths(
            SearchOverviewChampionResponse champion,
            MetricSummary championMetrics,
            MetricSummary overallMetrics,
            List<SearchOverviewMatchupResponse> favorableOpponents,
            List<SearchOverviewMatchupResponse> frequentOpponents
    ) {
        List<String> strengths = new ArrayList<>();

        int winRateGap = champion.winRate() - overallMetrics.winRate();
        if (champion.matchCount() >= 3 && winRateGap >= 10) {
            strengths.add("승률이 전체 기준보다 " + winRateGap + "%p 높습니다.");
        }

        double kdaGap = champion.averageKda() - overallMetrics.averageKda();
        if (champion.matchCount() >= 3 && kdaGap >= 0.8) {
            strengths.add("평균 KDA가 " + formatDecimal(champion.averageKda()) + "로 안정적인 편입니다.");
        }

        int damageGap = championMetrics.averageDamage() - overallMetrics.averageDamage();
        if (champion.matchCount() >= 3 && damageGap >= 3500) {
            strengths.add("평균 딜량이 전체 기준보다 " + damageGap + " 높습니다.");
        }

        int csGap = championMetrics.averageCs() - overallMetrics.averageCs();
        if (champion.matchCount() >= 3 && csGap >= 18) {
            strengths.add("평균 CS가 전체 기준보다 " + csGap + " 높아 성장력이 좋습니다.");
        }

        int visionGap = championMetrics.averageVisionScore() - overallMetrics.averageVisionScore();
        if (champion.matchCount() >= 3 && visionGap >= 8) {
            strengths.add("시야 점수가 높아 맵 기여도가 좋습니다.");
        }

        if (!favorableOpponents.isEmpty()) {
            SearchOverviewMatchupResponse favorableOpponent = favorableOpponents.get(0);
            if (favorableOpponent.matchCount() >= 2 && favorableOpponent.winRate() >= 60) {
                strengths.add(favorableOpponent.championNameKo() + " 상대로 승률 "
                        + favorableOpponent.winRate() + "%, 평균 KDA "
                        + formatDecimal(favorableOpponent.averageKda()) + "를 기록했습니다.");
            }
        }

        if (strengths.isEmpty() && !frequentOpponents.isEmpty() && frequentOpponents.get(0).matchCount() >= 2) {
            SearchOverviewMatchupResponse frequentOpponent = frequentOpponents.get(0);
            strengths.add("가장 자주 만난 " + frequentOpponent.championNameKo()
                    + " 상대로 " + frequentOpponent.matchCount() + "판을 치렀습니다.");
        }

        return strengths.stream().limit(3).toList();
    }

    private List<String> buildChampionWatchPoints(
            SearchOverviewChampionResponse champion,
            MetricSummary championMetrics,
            MetricSummary overallMetrics,
            List<SearchOverviewMatchupResponse> toughestOpponents
    ) {
        List<String> watchPoints = new ArrayList<>();

        if (champion.matchCount() < 3) {
            watchPoints.add("표본이 " + champion.matchCount() + "판이라 참고용으로 보는 편이 좋습니다.");
        }

        int winRateGap = champion.winRate() - overallMetrics.winRate();
        if (champion.matchCount() >= 3 && winRateGap <= -10) {
            watchPoints.add("승률이 전체 기준보다 " + Math.abs(winRateGap) + "%p 낮습니다.");
        }

        double deathGap = championMetrics.averageDeaths() - overallMetrics.averageDeaths();
        if (champion.matchCount() >= 3 && deathGap >= 1.0) {
            watchPoints.add("평균 데스가 전체 기준보다 " + formatDecimal(deathGap) + "회 많습니다.");
        }

        if (!toughestOpponents.isEmpty()) {
            SearchOverviewMatchupResponse toughestOpponent = toughestOpponents.get(0);
            if (toughestOpponent.matchCount() >= 2 && toughestOpponent.winRate() <= 40) {
                watchPoints.add(toughestOpponent.championNameKo() + " 상대로 승률 "
                        + toughestOpponent.winRate() + "%에 머물렀습니다.");
            }
        }

        return watchPoints.stream().limit(3).toList();
    }

    private List<String> buildChampionInsights(
            SearchOverviewChampionResponse champion,
            PositionSummary primaryPosition,
            MetricSummary championMetrics,
            MetricSummary overallMetrics,
            List<SearchOverviewMatchupResponse> frequentOpponents,
            List<SearchOverviewMatchupResponse> favorableOpponents,
            List<SearchOverviewMatchupResponse> toughestOpponents,
            List<SearchOverviewItemResponse> frequentItems,
            List<String> strengths,
            List<String> watchPoints
    ) {
        List<String> insights = new ArrayList<>();

        if (primaryPosition != null && champion.matchCount() >= 2) {
            insights.add(primaryPosition.teamPositionKo() + " 기준 " + champion.matchCount()
                    + "판 표본에서 승률 " + champion.winRate() + "%를 기록했습니다.");
        }

        if (!strengths.isEmpty()) {
            insights.add(strengths.get(0));
        }

        if (!toughestOpponents.isEmpty() && toughestOpponents.get(0).matchCount() >= 2) {
            SearchOverviewMatchupResponse opponent = toughestOpponents.get(0);
            insights.add(opponent.championNameKo() + " 상대로는 " + opponent.matchCount()
                    + "판에서 승률 " + opponent.winRate() + "%였습니다.");
        } else if (!favorableOpponents.isEmpty() && favorableOpponents.get(0).matchCount() >= 2) {
            SearchOverviewMatchupResponse opponent = favorableOpponents.get(0);
            insights.add(opponent.championNameKo() + " 상대로는 승률 "
                    + opponent.winRate() + "%, 평균 KDA " + formatDecimal(opponent.averageKda()) + "였습니다.");
        } else if (!frequentOpponents.isEmpty() && frequentOpponents.get(0).matchCount() >= 2) {
            SearchOverviewMatchupResponse opponent = frequentOpponents.get(0);
            insights.add("가장 자주 만난 상대는 " + opponent.championNameKo()
                    + "이며 " + opponent.matchCount() + "판을 치렀습니다.");
        }

        if (!frequentItems.isEmpty()) {
            SearchOverviewItemResponse frequentItem = frequentItems.get(0);
            insights.add(frequentItem.itemName() + "는 " + frequentItem.matchCount()
                    + "판에서 사용됐고 승률 " + frequentItem.winRate() + "%를 기록했습니다.");
        }

        if (!watchPoints.isEmpty()) {
            insights.add(watchPoints.get(0));
        }

        if (insights.isEmpty() || champion.matchCount() < 2) {
            insights.add(champion.championNameKo() + " " + champion.matchCount()
                    + "판 기준 승률 " + champion.winRate() + "%, 평균 KDA "
                    + formatDecimal(champion.averageKda()) + "입니다.");
        }

        return insights.stream().limit(3).toList();
    }

    private List<String> buildOverviewInsights(
            MetricSummary overallMetrics,
            SearchOverviewRecentTrendResponse recentTrend,
            List<SearchOverviewChampionAnalysisResponse> championAnalyses
    ) {
        List<String> insights = new ArrayList<>(recentTrend.insights());

        if (!championAnalyses.isEmpty()) {
            SearchOverviewChampionAnalysisResponse mainChampion = championAnalyses.get(0);
            insights.add(mainChampion.championNameKo() + "이(가) 최근 표본에서 가장 많이 나온 카드입니다.");

            if (!mainChampion.toughestOpponents().isEmpty() && mainChampion.toughestOpponents().get(0).matchCount() >= 2) {
                SearchOverviewMatchupResponse toughestOpponent = mainChampion.toughestOpponents().get(0);
                insights.add("주 챔피언 기준 가장 까다로운 상대는 "
                        + toughestOpponent.championNameKo() + "입니다.");
            }
        }

        if (insights.isEmpty()) {
            insights.add("최근 " + overallMetrics.matchCount() + "판 승률은 "
                    + overallMetrics.winRate() + "%, 평균 KDA는 "
                    + formatDecimal(overallMetrics.averageKda()) + "입니다.");
        }

        return insights.stream().limit(3).toList();
    }

    private List<MatchSearchDocument> filterChampionMatches(List<MatchSearchDocument> matches, String championName) {
        return matches.stream()
                .filter(match -> championName.equals(match.getChampionName()))
                .toList();
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

    private List<SearchOverviewMatchupResponse> findFrequentOpponents(List<MatchSearchDocument> matches, int limit) {
        return groupByOpponentChampion(matches).values().stream()
                .sorted(Comparator.comparingInt(ChampionSummary::matchCount).reversed()
                        .thenComparingInt(ChampionSummary::winRate))
                .map(this::toMatchupResponse)
                .limit(limit)
                .toList();
    }

    private List<SearchOverviewMatchupResponse> findToughestOpponents(List<MatchSearchDocument> matches, int limit) {
        return groupByOpponentChampion(matches).values().stream()
                .filter(summary -> summary.matchCount() >= 2)
                .sorted(Comparator.comparingInt(ChampionSummary::winRate)
                        .thenComparingDouble(ChampionSummary::averageDeaths).reversed()
                        .thenComparingInt(ChampionSummary::matchCount).reversed())
                .map(this::toMatchupResponse)
                .limit(limit)
                .toList();
    }

    private List<SearchOverviewMatchupResponse> findFavorableOpponents(List<MatchSearchDocument> matches, int limit) {
        return groupByOpponentChampion(matches).values().stream()
                .filter(summary -> summary.matchCount() >= 2)
                .sorted(Comparator.comparingInt(ChampionSummary::winRate).reversed()
                        .thenComparingDouble(ChampionSummary::averageKda).reversed()
                        .thenComparingInt(ChampionSummary::matchCount).reversed())
                .map(this::toMatchupResponse)
                .limit(limit)
                .toList();
    }

    private Map<String, ChampionSummary> groupByChampion(List<MatchSearchDocument> matches) {
        Map<String, List<MatchSearchDocument>> grouped = new LinkedHashMap<>();
        for (MatchSearchDocument match : matches) {
            grouped.computeIfAbsent(match.getChampionName(), ignored -> new ArrayList<>()).add(match);
        }

        Map<String, ChampionSummary> summaries = new LinkedHashMap<>();
        grouped.forEach((championName, championMatches) -> summaries.put(championName, summarizeChampion(
                championName,
                championMatches.get(0).getChampionKey(),
                championMatches.get(0).getChampionNameKo(),
                championMatches
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
        grouped.forEach((championName, championMatches) -> summaries.put(championName, summarizeChampion(
                championName,
                championMatches.get(0).getOpponentChampionKey(),
                championMatches.get(0).getOpponentChampionNameKo(),
                championMatches
        )));
        return summaries;
    }

    private PositionSummary findPrimaryPosition(List<MatchSearchDocument> matches) {
        Map<String, PositionSummary> summaries = groupByPosition(matches);
        return summaries.values().stream()
                .sorted(Comparator.comparingInt(PositionSummary::matchCount).reversed()
                        .thenComparingInt(PositionSummary::winRate).reversed())
                .findFirst()
                .orElse(null);
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
                toPercent((int) positionMatches.stream().filter(MatchSearchDocument::isWin).count(), positionMatches.size())
        )));
        return summaries;
    }

    private ChampionSummary summarizeChampion(
            String championName,
            String championKey,
            String championNameKo,
            List<MatchSearchDocument> matches
    ) {
        MetricSummary metrics = summarizeMetrics(matches);
        return new ChampionSummary(
                championName,
                championKey,
                championNameKo,
                metrics.matchCount(),
                metrics.winCount(),
                metrics.winRate(),
                metrics.averageKda(),
                metrics.averageDeaths(),
                metrics.averageDamage(),
                metrics.averageGold(),
                metrics.averageCs(),
                metrics.averageVisionScore()
        );
    }

    private MetricSummary summarizeMetrics(List<MatchSearchDocument> matches) {
        if (matches.isEmpty()) {
            return new MetricSummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        int matchCount = matches.size();
        int winCount = (int) matches.stream().filter(MatchSearchDocument::isWin).count();
        return new MetricSummary(
                matchCount,
                winCount,
                matchCount - winCount,
                toPercent(winCount, matchCount),
                roundToTwo(matches.stream().mapToDouble(MatchSearchDocument::getKda).average().orElse(0)),
                roundToTwo(matches.stream().mapToInt(MatchSearchDocument::getDeaths).average().orElse(0)),
                roundToInt(matches.stream().mapToInt(MatchSearchDocument::getTotalDamageDealtToChampions).average().orElse(0)),
                roundToInt(matches.stream().mapToInt(MatchSearchDocument::getGoldEarned).average().orElse(0)),
                roundToInt(matches.stream().mapToInt(MatchSearchDocument::getTotalCs).average().orElse(0)),
                roundToInt(matches.stream().mapToInt(MatchSearchDocument::getVisionScore).average().orElse(0))
        );
    }

    private List<SearchOverviewItemResponse> findFrequentItems(List<MatchSearchDocument> matches, int limit) {
        Map<String, List<MatchSearchDocument>> grouped = new LinkedHashMap<>();
        for (MatchSearchDocument match : matches) {
            Set<Integer> seenItemIds = new HashSet<>();
            int itemCount = Math.min(match.getItemIds().size(), match.getItemNames().size());
            for (int index = 0; index < itemCount; index++) {
                Integer itemId = match.getItemIds().get(index);
                String itemName = match.getItemNames().get(index);

                if (!isMeaningfulItem(itemId, itemName) || !seenItemIds.add(itemId)) {
                    continue;
                }

                grouped.computeIfAbsent(itemName, ignored -> new ArrayList<>()).add(match);
            }
        }

        return grouped.entrySet().stream()
                .map(entry -> toItemResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(SearchOverviewItemResponse::matchCount).reversed()
                        .thenComparingInt(SearchOverviewItemResponse::winRate).reversed()
                        .thenComparingDouble(SearchOverviewItemResponse::averageKda).reversed())
                .limit(limit)
                .toList();
    }

    private boolean isMeaningfulItem(Integer itemId, String itemName) {
        return itemId != null
                && !LOW_SIGNAL_ITEM_IDS.contains(itemId)
                && hasText(itemName)
                && !itemName.chars().allMatch(Character::isDigit);
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

    private SearchOverviewMatchupResponse toMatchupResponse(ChampionSummary summary) {
        return new SearchOverviewMatchupResponse(
                summary.championName(),
                summary.championKey(),
                summary.championNameKo(),
                summary.matchCount(),
                summary.winCount(),
                summary.winRate(),
                summary.averageKda(),
                summary.averageDeaths()
        );
    }

    private SearchOverviewItemResponse toItemResponse(String itemName, List<MatchSearchDocument> matches) {
        int winCount = (int) matches.stream().filter(MatchSearchDocument::isWin).count();
        return new SearchOverviewItemResponse(
                itemName,
                matches.size(),
                winCount,
                toPercent(winCount, matches.size()),
                roundToTwo(matches.stream().mapToDouble(MatchSearchDocument::getKda).average().orElse(0))
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
            double averageKda,
            double averageDeaths,
            int averageDamage,
            int averageGold,
            int averageCs,
            int averageVisionScore
    ) {
    }

    private record MetricSummary(
            int matchCount,
            int winCount,
            int lossCount,
            int winRate,
            double averageKda,
            double averageDeaths,
            int averageDamage,
            int averageGold,
            int averageCs,
            int averageVisionScore
    ) {
    }

    private record PositionSummary(
            String teamPosition,
            String teamPositionKo,
            int matchCount,
            int winCount,
            int winRate
    ) {
    }
}
