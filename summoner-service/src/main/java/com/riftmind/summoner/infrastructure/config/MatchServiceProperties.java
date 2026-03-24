package com.riftmind.summoner.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * match-service 연동 설정 값을 바인딩하는 설정 클래스입니다.
 *
 * @author 정수환
 * @since 2026-03-24
 */
@ConfigurationProperties(prefix = "riftmind.match-service")
public class MatchServiceProperties {

    private String baseUrl = "http://localhost:18081";

    /**
     * match-service 기본 URL을 반환합니다.
     *
     * @return match-service 기본 URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * match-service 기본 URL을 설정합니다.
     *
     * @param baseUrl match-service 기본 URL
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
