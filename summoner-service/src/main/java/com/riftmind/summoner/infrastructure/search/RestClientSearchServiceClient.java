package com.riftmind.summoner.infrastructure.search;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.riftmind.summoner.global.exception.RiotApiException;
import com.riftmind.summoner.infrastructure.config.SearchServiceProperties;

/**
 * RestClient 기반 search-service 연동 구현체입니다.
 *
 * @author 정수환
 * @since 2026-03-26
 */
@Component
public class RestClientSearchServiceClient implements SearchServiceClient {

    private final RestClient restClient;

    public RestClientSearchServiceClient(RestClient.Builder restClientBuilder, SearchServiceProperties properties) {
        this.restClient = restClientBuilder.clone()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchIndexResult indexRecentMatches(String puuid, Integer matchCount) {
        try {
            SearchIndexRequest request = new SearchIndexRequest(puuid, matchCount);
            SearchIndexResponse response = restClient.post()
                    .uri("/api/v1/search/index/matches")
                    .body(request)
                    .retrieve()
                    .body(SearchIndexResponse.class);

            if (response == null) {
                throw new RiotApiException("search-service returned an empty indexing response.");
            }

            return new SearchIndexResult(
                    response.puuid(),
                    response.requestedMatchCount(),
                    response.indexedMatchCount());
        } catch (RestClientException exception) {
            throw new RiotApiException("Failed to call search-service indexing API: " + exception.getMessage());
        }
    }

    private record SearchIndexRequest(
            String puuid,
            Integer matchCount
    ) {
    }

    private record SearchIndexResponse(
            String puuid,
            int requestedMatchCount,
            int indexedMatchCount
    ) {
    }
}
