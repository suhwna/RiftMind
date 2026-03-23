package com.riftmind.summoner.global.exception;

/**
 * 리소스를 찾을 수 없을 때 사용하는 예외입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public class ResourceNotFoundException extends ApiException {

    /**
     * 리소스 미존재 예외를 생성합니다.
     *
     * @param errorCode 오류 코드
     * @param message 오류 메시지
     */
    public ResourceNotFoundException(ApiErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
