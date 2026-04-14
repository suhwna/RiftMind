package com.riftmind.ai.infrastructure.openai;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.riftmind.ai.application.service.MatchReviewResult;
import com.riftmind.ai.global.exception.ApiErrorCode;
import com.riftmind.ai.global.exception.ApiException;
import com.riftmind.ai.infrastructure.config.OpenAiProperties;

/**
 * RestClient 기반 OpenAI Responses API 구현체입니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
@Component
public class RestClientOpenAiReviewClient implements OpenAiReviewClient {

    private static final String SYSTEM_PROMPT = """
            당신은 League of Legends 경기 회고 코치입니다.
            입력으로 제공된 경기 요약 데이터를 바탕으로 해당 플레이어의 경기 회고를 작성하세요.
            accumulatedBaseline이 있으면 누적 표본과 현재 경기의 차이를 함께 비교하세요.
            accumulatedBaseline.sampleCount가 0이거나 너무 작으면 기준 데이터는 참고용이라고 판단하세요.
            반드시 한국어로 답하고, 실제 수치와 맥락을 바탕으로 짧고 명확하게 정리하세요.
            원시 데이터에 없는 사실을 추측하지 마세요.
            strongPoints, weakPoints, nextFocus는 각각 중복 없이 간결한 문장으로 작성하세요.
            """;

    private final RestClient openAiClient;
    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper;

    public RestClientOpenAiReviewClient(
            RestClient.Builder restClientBuilder,
            OpenAiProperties openAiProperties,
            ObjectMapper objectMapper) {
        this.openAiProperties = openAiProperties;
        this.objectMapper = objectMapper;
        this.openAiClient = restClientBuilder.clone()
                .baseUrl(openAiProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiProperties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * OpenAI Responses API로 구조화된 경기 회고를 생성합니다.
     *
     * @param promptPayload 경기 요약 JSON 문자열
     * @return 회고 결과
     */
    @Override
    public MatchReviewResult generateReview(String promptPayload) {
        validateConfiguration();

        try {
            JsonNode response = openAiClient.post()
                    .uri("/responses")
                    .body(buildRequestBody(promptPayload))
                    .retrieve()
                    .body(JsonNode.class);

            String outputText = extractOutputText(response);
            if (!StringUtils.hasText(outputText)) {
                throw new ApiException(ApiErrorCode.OPENAI_API_ERROR, "OpenAI response did not include output text.");
            }

            return objectMapper.readValue(outputText, MatchReviewResult.class);
        } catch (RestClientResponseException exception) {
            throw new ApiException(
                    ApiErrorCode.OPENAI_API_ERROR,
                    "OpenAI API request failed with status " + exception.getStatusCode().value() + ".");
        } catch (RestClientException | JsonProcessingException exception) {
            throw new ApiException(
                    ApiErrorCode.OPENAI_API_ERROR,
                    "OpenAI review generation failed: " + exception.getMessage());
        }
    }

    private Map<String, Object> buildRequestBody(String promptPayload) {
        return Map.of(
                "model", openAiProperties.getModel(),
                "input", List.of(
                        Map.of(
                                "role", "system",
                                "content", List.of(Map.of("type", "input_text", "text", SYSTEM_PROMPT))),
                        Map.of(
                                "role", "user",
                                "content", List.of(Map.of("type", "input_text", "text", promptPayload)))),
                "text", Map.of(
                        "format", Map.of(
                                "type", "json_schema",
                                "name", "match_review",
                                "strict", true,
                                "schema", reviewSchema())));
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(openAiProperties.getApiKey())) {
            throw new ApiException(ApiErrorCode.OPENAI_API_ERROR, "OPENAI_API_KEY is not configured.");
        }
        if (!StringUtils.hasText(openAiProperties.getModel())) {
            throw new ApiException(ApiErrorCode.OPENAI_API_ERROR, "OpenAI model is not configured.");
        }
    }

    private String extractOutputText(JsonNode response) {
        if (response == null) {
            return null;
        }

        String outputText = response.path("output_text").asText(null);
        if (StringUtils.hasText(outputText)) {
            return outputText;
        }

        for (JsonNode output : response.path("output")) {
            for (JsonNode content : output.path("content")) {
                String text = content.path("text").asText(null);
                if (StringUtils.hasText(text)) {
                    return text;
                }
            }
        }

        return null;
    }

    private Map<String, Object> reviewSchema() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "summary", Map.of("type", "string"),
                        "strongPoints", stringArraySchema(2, 3),
                        "weakPoints", stringArraySchema(2, 3),
                        "nextFocus", stringArraySchema(1, 2)),
                "required", List.of("summary", "strongPoints", "weakPoints", "nextFocus"));
    }

    private Map<String, Object> stringArraySchema(int minItems, int maxItems) {
        return Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "minItems", minItems,
                "maxItems", maxItems);
    }
}
