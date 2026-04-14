package com.riftmind.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.riftmind.ai.infrastructure.config.MatchServiceProperties;
import com.riftmind.ai.infrastructure.config.OpenAiProperties;

/**
 * AI Service 애플리케이션의 시작점입니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
@SpringBootApplication
@EnableConfigurationProperties({OpenAiProperties.class, MatchServiceProperties.class})
public class AiServiceApplication {

    /**
     * AI Service 애플리케이션을 실행합니다.
     *
     * @param args 애플리케이션 실행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }
}
