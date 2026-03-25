package com.riftmind.search.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "riftmind.match-service")
public record MatchServiceProperties(
        String baseUrl
) {

    public MatchServiceProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:18081";
        }
    }
}
