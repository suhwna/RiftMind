package com.riftmind.summoner.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * search-service 연동 설정 값을 바인딩하는 설정 클래스입니다.
 *
 * @author 정수환
 * @since 2026-03-26
 */
@ConfigurationProperties(prefix = "riftmind.search-service")
public class SearchServiceProperties {

    private String baseUrl = "http://localhost:18082";

    /**
     * search-service 기본 URL을 반환합니다.
     *
     * @return search-service 기본 URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * search-service 기본 URL을 설정합니다.
     *
     * @param baseUrl search-service 기본 URL
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
