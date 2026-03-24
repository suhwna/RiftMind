package com.riftmind.match.global.exception;

import org.springframework.http.HttpStatus;

/**
 * API 오류 코드와 대응 HTTP 상태를 정의합니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public enum ApiErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST),
    SUMMONER_NOT_FOUND(HttpStatus.NOT_FOUND),
    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND),
    RIOT_API_ERROR(HttpStatus.BAD_GATEWAY),
    RIOT_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ApiErrorCode(HttpStatus status) {
        this.status = status;
    }

    /**
     * 오류 코드에 대응하는 HTTP 상태를 반환합니다.
     *
     * @return HTTP 상태
     */
    public HttpStatus getStatus() {
        return status;
    }
}
