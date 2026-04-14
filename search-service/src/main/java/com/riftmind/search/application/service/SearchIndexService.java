package com.riftmind.search.application.service;

import com.riftmind.search.api.response.MatchIndexResponse;
import com.riftmind.search.domain.search.MatchSearchDocument;
import com.riftmind.search.global.exception.ApiException;
import com.riftmind.search.global.exception.SearchIndexingException;
import com.riftmind.search.infrastructure.elasticsearch.MatchSearchDocumentRepository;
import com.riftmind.search.infrastructure.match.MatchServiceClient;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * match-service 데이터를 Elasticsearch 검색 문서로 색인합니다.
 *
 * @author 정수환
 * @since 2026-03-25
 */
@Service
public class SearchIndexService {

    private final MatchServiceClient matchServiceClient;
    private final MatchSearchDocumentRepository matchSearchDocumentRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public SearchIndexService(
            MatchServiceClient matchServiceClient,
            MatchSearchDocumentRepository matchSearchDocumentRepository,
            ElasticsearchOperations elasticsearchOperations
    ) {
        this.matchServiceClient = matchServiceClient;
        this.matchSearchDocumentRepository = matchSearchDocumentRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /**
     * PUUID 기준 최근 참가자 데이터를 검색 인덱스에 저장합니다.
     *
     * @param puuid Riot PUUID
     * @param matchCount 색인할 경기 수
     * @return 색인 결과 응답
     */
    public MatchIndexResponse indexRecentMatches(String puuid, int matchCount) {
        try {
            ensureIndexExists(); // 인덱스가 존재하지 않으면 생성

            // match-service에서 최근 경기 데이터 조회
            MatchServiceClient.SearchSourceListResult result =
                    matchServiceClient.getRecentMatchesForSearch(puuid, matchCount);

            // 조회된 경기 데이터를 Elasticsearch 검색 문서로 변환
            List<MatchSearchDocument> documents = result.matches().stream()
                    .map(match -> new MatchSearchDocument(
                            match.matchId() + ":" + result.puuid(),
                            match.matchId(),
                            result.puuid(),
                            match.gameCreation(),
                            match.queueId(),
                            match.queueNameKo(),
                            match.gameMode(),
                            match.summonerName(),
                            match.championName(),
                            match.championKey(),
                            match.championNameKo(),
                            match.opponentChampionName(),
                            match.opponentChampionKey(),
                            match.opponentChampionNameKo(),
                            match.teamPosition(),
                            match.teamPositionKo(),
                            match.totalDamageDealtToChampions(),
                            match.goldEarned(),
                            match.totalMinionsKilled(),
                            match.neutralMinionsKilled(),
                            match.totalMinionsKilled() + match.neutralMinionsKilled(),
                            match.visionScore(),
                            match.wardsPlaced(),
                            match.wardsKilled(),
                            match.champLevel(),
                            match.itemIds(),
                            match.itemNames(),
                            match.itemIconUrls(),
                            match.summonerSpellIds(),
                            match.summonerSpellNames(),
                            match.summonerSpellIconUrls(),
                            match.primaryRune(),
                            match.primaryRuneName(),
                            match.primaryRuneIconUrl(),
                            match.secondaryRune(),
                            match.secondaryRuneName(),
                            match.secondaryRuneIconUrl(),
                            match.interpretationTags(),
                            match.kills(),
                            match.deaths(),
                            match.assists(),
                            calculateKda(match.kills(), match.deaths(), match.assists()),
                            match.win(),
                            match.totalDamageTaken()
                    ))
                    .toList();

            matchSearchDocumentRepository.saveAll(documents); // Elasticsearch에 색인 저장
            elasticsearchOperations.indexOps(MatchSearchDocument.class).refresh(); // 색인 강제 새로고침

            return new MatchIndexResponse(
                    result.puuid(),
                    matchCount,
                    documents.size(),
                    LocalDateTime.now()
            );
        } catch (ApiException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new SearchIndexingException(
                    "최근 경기 색인에 실패했습니다. 원인: " + resolveCauseMessage(exception),
                    exception
            );
        }
    }

    /**
     * Elasticsearch 인덱스가 존재하는지 확인하고, 존재하지 않으면 생성합니다.
     */
    private void ensureIndexExists() {
        IndexOperations indexOperations = elasticsearchOperations.indexOps(MatchSearchDocument.class); // 인덱스 작업 객체 생성
        if (!indexOperations.exists()) {
            indexOperations.create();
            indexOperations.putMapping(indexOperations.createMapping(MatchSearchDocument.class)); // 매핑 설정 적용
        }
    }

    /**
     * 킬, 데스, 어시스트를 기반으로 KDA를 계산합니다. 데스가 0인 경우에는 KDA를 (킬 + 어시스트)로 간주합니다.
     *
     * @param kills 킬 수
     * @param deaths 데스 수
     * @param assists 어시스트 수
     * @return 계산된 KDA 값
     */
    private double calculateKda(int kills, int deaths, int assists) {
        if (deaths == 0) {
            return kills + assists;
        }
        return (double) (kills + assists) / deaths;
    }

    /**
     * 예외의 원인 메시지를 재귀적으로 탐색하여 가장 근본적인 원인 메시지를 반환합니다. 메시지가 없는 경우에는 예외 클래스 이름을 반환합니다.
     *
     * @param throwable 탐색할 예외 객체
     * @return 가장 근본적인 원인 메시지 또는 예외 클래스 이름
     */
    private String resolveCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }

        if (current.getMessage() == null || current.getMessage().isBlank()) {
            return current.getClass().getSimpleName();
        }
        return current.getMessage();
    }
}
