package com.riftmind.ai.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * search-service 연동 설정을 담습니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
@ConfigurationProperties(prefix = "riftmind.search-service")
public class SearchServiceProperties {

    private String baseUrl = "http://localhost:18082";

    /**
     * search-service base URL을 반환합니다.
     *
     * @return search-service base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
