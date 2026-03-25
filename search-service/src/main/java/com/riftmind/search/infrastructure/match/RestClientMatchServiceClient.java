package com.riftmind.search.infrastructure.match;

import com.riftmind.search.global.exception.ExternalApiException;
import com.riftmind.search.infrastructure.config.MatchServiceProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * RestClient 기반 match-service 연동 구현체입니다.
 *
 * @author 정수환
 * @since 2026-03-25
 */
@Component
public class RestClientMatchServiceClient implements MatchServiceClient {

    private final RestClient restClient;

    public RestClientMatchServiceClient(RestClient.Builder restClientBuilder, MatchServiceProperties properties) {
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .build();
    }

    /**
     * PUUID 기준 최근 참가자 검색 원본 데이터를 조회합니다.
     *
     * @param puuid Riot PUUID
     * @param count 조회할 경기 수
     * @return 검색 원본 데이터
     */
    @Override
    public SearchSourceListResult getRecentMatchesForSearch(String puuid, int count) {
        try {
            SearchSourceListResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/matches/search-source/by-puuid")
                            .queryParam("puuid", puuid)
                            .queryParam("count", count)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(SearchSourceListResponse.class);

            if (response == null) {
                throw new ExternalApiException("match-service 응답이 비어 있습니다.");
            }

            List<SearchSourceMatchResult> matches = response.matches() == null
                    ? List.of()
                    : response.matches().stream()
                    .map(match -> new SearchSourceMatchResult(
                            match.matchId(),
                            match.gameCreation(),
                            match.queueId(),
                            match.queueNameKo(),
                            match.gameMode(),
                            match.puuid(),
                            match.summonerName(),
                            match.championName(),
                            match.championNameKo(),
                            match.teamPosition(),
                            match.teamPositionKo(),
                            match.kills(),
                            match.deaths(),
                            match.assists(),
                            match.win(),
                            match.totalDamageDealtToChampions(),
                            match.goldEarned(),
                            match.totalMinionsKilled(),
                            match.neutralMinionsKilled(),
                            match.visionScore(),
                            match.wardsPlaced(),
                            match.wardsKilled(),
                            match.champLevel(),
                            match.itemIds(),
                            match.itemNames(),
                            match.summonerSpellIds(),
                            match.summonerSpellNames(),
                            match.primaryRune(),
                            match.primaryRuneName(),
                            match.secondaryRune(),
                            match.secondaryRuneName(),
                            match.totalDamageTaken()
                    ))
                    .toList();

            return new SearchSourceListResult(response.puuid(), response.count(), matches);
        } catch (RestClientException exception) {
            throw new ExternalApiException("match-service 검색 소스 조회에 실패했습니다.", exception);
        }
    }

    /**
     * match-service 응답 바인딩용 내부 DTO입니다.
     */
    private record SearchSourceListResponse(
            String puuid,
            int count,
            List<SearchSourceMatchResponse> matches
    ) {
    }

    /**
     * match-service 검색 소스 응답 바인딩용 내부 DTO입니다.
     */
    private record SearchSourceMatchResponse(
            String matchId,
            String gameCreation,
            Integer queueId,
            String queueNameKo,
            String gameMode,
            String puuid,
            String summonerName,
            String championName,
            String championNameKo,
            String teamPosition,
            String teamPositionKo,
            int kills,
            int deaths,
            int assists,
            boolean win,
            int totalDamageDealtToChampions,
            int goldEarned,
            int totalMinionsKilled,
            int neutralMinionsKilled,
            int visionScore,
            int wardsPlaced,
            int wardsKilled,
            int champLevel,
            List<Integer> itemIds,
            List<String> itemNames,
            List<Integer> summonerSpellIds,
            List<String> summonerSpellNames,
            Integer primaryRune,
            String primaryRuneName,
            Integer secondaryRune,
            String secondaryRuneName,
            int totalDamageTaken
    ) {
    }
}
