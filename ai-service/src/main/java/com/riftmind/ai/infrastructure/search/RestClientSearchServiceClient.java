package com.riftmind.ai.infrastructure.search;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.riftmind.ai.infrastructure.config.SearchServiceProperties;

/**
 * RestClient 기반 search-service 연동 구현체입니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
@Component
public class RestClientSearchServiceClient implements SearchServiceClient {

    private final RestClient restClient;

    public RestClientSearchServiceClient(
            RestClient.Builder restClientBuilder,
            SearchServiceProperties properties) {
        this.restClient = restClientBuilder.clone()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchReviewBaselineResponse getReviewBaseline(
            String championName,
            String opponentChampionName,
            String teamPosition) {
        if (!StringUtils.hasText(championName)) {
            return null;
        }

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/search/review-baseline")
                            .queryParam("championName", championName)
                            .queryParamIfPresent("opponentChampionName", optionalText(opponentChampionName))
                            .queryParamIfPresent("teamPosition", optionalText(teamPosition))
                            .build())
                    .retrieve()
                    .body(SearchReviewBaselineResponse.class);
        } catch (RestClientException exception) {
            return null;
        }
    }

    private java.util.Optional<String> optionalText(String value) {
        return StringUtils.hasText(value) ? java.util.Optional.of(value) : java.util.Optional.empty();
    }
}
