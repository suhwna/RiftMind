package com.riftmind.match;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

import com.riftmind.match.infrastructure.config.RiotApiProperties;

/**
 * Match Service 애플리케이션의 시작점입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(RiotApiProperties.class)
public class MatchServiceApplication {

    /**
     * Match Service 애플리케이션을 실행합니다.
     *
     * @param args 애플리케이션 실행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(MatchServiceApplication.class, args);
    }
}
