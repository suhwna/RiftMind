package com.riftmind.summoner.global.exception;

/**
 * 애플리케이션 공통 예외의 기본 클래스입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public class ApiException extends RuntimeException {

    private final ApiErrorCode errorCode;

    public ApiException(ApiErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 예외에 연결된 오류 코드를 반환합니다.
     *
     * @return 오류 코드
     */
    public ApiErrorCode getErrorCode() {
        return errorCode;
    }
}
