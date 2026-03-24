package com.riftmind.match.infrastructure.config;

import java.util.List;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.servers.Server;

/**
 * OpenAPI 문서가 게이트웨이 기준 상대 경로를 사용하도록 맞춥니다.
 *
 * @author 정수환
 * @since 2026-03-24
 */
@Configuration
public class OpenApiConfig {

    /**
     * Swagger UI가 서비스 고유 포트 대신 현재 호스트 기준으로 호출하도록 서버 URL을 상대 경로로 고정합니다.
     *
     * @return OpenAPI 커스터마이저
     */
    @Bean
    public OpenApiCustomizer gatewayRelativeServerCustomizer() {
        return openApi -> openApi.setServers(List.of(new Server().url("/")));
    }
}
