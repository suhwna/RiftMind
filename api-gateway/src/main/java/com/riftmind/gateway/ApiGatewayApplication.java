package com.riftmind.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway 애플리케이션의 시작점입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@SpringBootApplication
public class ApiGatewayApplication {

    /**
     * API Gateway 애플리케이션을 실행합니다.
     *
     * @param args 애플리케이션 실행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
