package com.riftmind.ai.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * match-service 연동 설정을 담습니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
@ConfigurationProperties(prefix = "riftmind.match-service")
public class MatchServiceProperties {

    private String baseUrl = "http://localhost:18081";

    /**
     * match-service base URL을 반환합니다.
     *
     * @return match-service base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
