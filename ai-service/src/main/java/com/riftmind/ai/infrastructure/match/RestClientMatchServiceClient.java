package com.riftmind.ai.infrastructure.match;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.riftmind.ai.global.exception.ApiErrorCode;
import com.riftmind.ai.global.exception.ApiException;
import com.riftmind.ai.infrastructure.config.MatchServiceProperties;

/**
 * RestClient 기반 match-service 클라이언트입니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
@Component
public class RestClientMatchServiceClient implements MatchServiceClient {

    private final RestClient matchClient;

    public RestClientMatchServiceClient(
            RestClient.Builder restClientBuilder,
            MatchServiceProperties properties) {
        this.matchClient = restClientBuilder.clone()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    /**
     * match-service의 매치 상세 API를 호출합니다.
     *
     * @param matchId Riot matchId
     * @param focusPuuid 회고 대상 PUUID
     * @return 매치 상세 응답
     */
    @Override
    public MatchDetailResponse getMatchDetail(String matchId, String focusPuuid) {
        try {
            MatchDetailResponse response = matchClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/matches/{matchId}")
                            .queryParam("focusPuuid", focusPuuid)
                            .build(matchId))
                    .retrieve()
                    .body(MatchDetailResponse.class);
            if (response == null) {
                throw new ApiException(
                        ApiErrorCode.MATCH_SERVICE_ERROR,
                        "match-service returned empty match detail response.");
            }
            return response;
        } catch (RestClientResponseException exception) {
            throw new ApiException(
                    ApiErrorCode.MATCH_SERVICE_ERROR,
                    "match-service request failed with status " + exception.getStatusCode().value() + ".");
        } catch (RestClientException exception) {
            throw new ApiException(
                    ApiErrorCode.MATCH_SERVICE_ERROR,
                    "Failed to call match-service: " + exception.getMessage());
        }
    }
}
