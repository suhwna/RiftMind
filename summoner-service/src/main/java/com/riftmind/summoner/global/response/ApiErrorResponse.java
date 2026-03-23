package com.riftmind.summoner.global.response;

import java.time.LocalDateTime;

/**
 * API 오류 응답 공통 포맷입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public record ApiErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp) {

    /**
     * 현재 시각을 포함한 오류 응답을 생성합니다.
     *
     * @param code 오류 코드
     * @param message 오류 메시지
     * @return 오류 응답 객체
     */
    public static ApiErrorResponse of(String code, String message) {
        return new ApiErrorResponse(code, message, LocalDateTime.now());
    }
}
