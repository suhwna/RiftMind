package com.riftmind.ai.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAI API 호출 설정을 담습니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
@ConfigurationProperties(prefix = "riftmind.openai")
public class OpenAiProperties {

    private String baseUrl = "https://api.openai.com/v1";
    private String apiKey;
    private String model = "gpt-5.2";

    /**
     * OpenAI API base URL을 반환합니다.
     *
     * @return API base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * OpenAI API 키를 반환합니다.
     *
     * @return API 키
     */
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 회고 생성에 사용할 모델명을 반환합니다.
     *
     * @return 모델명
     */
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
