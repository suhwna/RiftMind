package com.riftmind.match.global.exception;

/**
 * Riot API 호출 중 발생한 예외를 표현합니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public class RiotApiException extends ApiException {

    /**
     * Riot API 예외를 생성합니다.
     *
     * @param message 오류 메시지
     */
    public RiotApiException(String message) {
        super(ApiErrorCode.RIOT_API_ERROR, message);
    }
}
