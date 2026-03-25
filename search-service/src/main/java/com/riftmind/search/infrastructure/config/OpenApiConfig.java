package com.riftmind.search.infrastructure.config;

import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer relativeServerCustomizer() {
        return openApi -> openApi.setServers(java.util.List.of(new Server().url("/")));
    }
}
