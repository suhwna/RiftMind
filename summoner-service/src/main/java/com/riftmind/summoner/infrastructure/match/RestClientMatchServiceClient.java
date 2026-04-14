package com.riftmind.summoner.infrastructure.match;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.riftmind.summoner.api.response.SummonerMatchListResponse;
import com.riftmind.summoner.global.exception.RiotApiException;
import com.riftmind.summoner.infrastructure.config.MatchServiceProperties;

/**
 * RestClient 기반 match-service 연동 구현체입니다.
 *
 * @author 정수환
 * @since 2026-03-24
 */
@Component
public class RestClientMatchServiceClient implements MatchServiceClient {

    private final RestClient restClient;

    public RestClientMatchServiceClient(RestClient.Builder restClientBuilder, MatchServiceProperties properties) {
        this.restClient = restClientBuilder.clone()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatchSyncResult syncMatches(String puuid, Integer matchCount) {
        try {
            MatchSyncRequest request = new MatchSyncRequest(puuid, matchCount);
            MatchSyncResponse response = restClient.post()
                    .uri("/api/v1/matches/sync")
                    .body(request)
                    .retrieve()
                    .body(MatchSyncResponse.class);

            if (response == null) {
                throw new RiotApiException("match-service returned an empty sync response.");
            }

            return new MatchSyncResult(
                    response.puuid(),
                    response.requestedMatchCount(),
                    response.savedMatchCount(),
                    response.existingMatchCount());
        } catch (RestClientException exception) {
            throw new RiotApiException("Failed to call match-service sync API: " + exception.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SummonerMatchListResponse getRecentMatches(String puuid, int count) {
        try {
            SummonerMatchListResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/matches/by-puuid")
                            .queryParam("puuid", puuid)
                            .queryParam("count", count)
                            .build())
                    .retrieve()
                    .body(SummonerMatchListResponse.class);

            if (response == null) {
                throw new RiotApiException("match-service returned an empty recent matches response.");
            }

            return response;
        } catch (RestClientException exception) {
            throw new RiotApiException("Failed to call match-service recent matches API: " + exception.getMessage());
        }
    }

    private record MatchSyncRequest(
            String puuid,
            Integer matchCount) {
    }

    private record MatchSyncResponse(
            String puuid,
            int requestedMatchCount,
            int savedMatchCount,
            int existingMatchCount) {
    }
}
