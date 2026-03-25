package com.riftmind.search.global.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.riftmind.search.global.exception.ApiErrorCode;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        String code,
        String message,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp
) {

    public static ApiErrorResponse of(ApiErrorCode errorCode, String message) {
        return new ApiErrorResponse(
                errorCode.code(),
                message,
                LocalDateTime.now()
        );
    }
}
