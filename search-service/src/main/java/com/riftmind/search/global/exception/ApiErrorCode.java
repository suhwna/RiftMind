package com.riftmind.search.global.exception;

import org.springframework.http.HttpStatus;

public enum ApiErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다."),
    MATCH_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "MATCH_SERVICE_ERROR", "match-service 호출에 실패했습니다."),
    SEARCH_INDEXING_ERROR(HttpStatus.BAD_GATEWAY, "SEARCH_INDEXING_ERROR", "검색 데이터 색인에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ApiErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
