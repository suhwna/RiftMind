package com.riftmind.ai.global.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * API 오류 응답 DTO입니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
@Schema(description = "API 오류 응답")
public record ApiErrorResponse(
        @Schema(description = "오류 코드", example = "OPENAI_API_ERROR")
        String code,
        @Schema(description = "오류 메시지", example = "OpenAI API request failed.")
        String message,
        @Schema(description = "오류 발생 시각", example = "2026-04-14T13:30:00")
        LocalDateTime timestamp) {

    /**
     * 오류 코드와 메시지로 응답을 생성합니다.
     *
     * @param code 오류 코드
     * @param message 오류 메시지
     * @return 오류 응답
     */
    public static ApiErrorResponse of(String code, String message) {
        return new ApiErrorResponse(code, message, LocalDateTime.now());
    }
}
