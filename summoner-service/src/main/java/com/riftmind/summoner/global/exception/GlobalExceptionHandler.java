package com.riftmind.summoner.global.exception;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.riftmind.summoner.global.response.ApiErrorResponse;

import jakarta.validation.ConstraintViolationException;

/**
 * REST API 전역 예외를 공통 응답으로 변환합니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 애플리케이션 공통 예외를 처리합니다.
     *
     * @param exception 애플리케이션 예외
     * @return 오류 응답
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception) {
        ApiErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiErrorResponse.of(errorCode.name(), exception.getMessage()));
    }

    /**
     * 요청 바인딩 검증 예외를 처리합니다.
     *
     * @param exception 검증 예외
     * @return 오류 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .badRequest()
                .body(ApiErrorResponse.of(ApiErrorCode.INVALID_REQUEST.name(), message));
    }

    /**
     * 제약 조건 위반 예외를 처리합니다.
     *
     * @param exception 제약 조건 위반 예외
     * @return 오류 응답
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(
            ConstraintViolationException exception) {
        return ResponseEntity
                .badRequest()
                .body(ApiErrorResponse.of(ApiErrorCode.INVALID_REQUEST.name(), exception.getMessage()));
    }

    /**
     * 처리되지 않은 예외를 처리합니다.
     *
     * @param exception 알 수 없는 예외
     * @return 오류 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception) {
        return ResponseEntity
                .internalServerError()
                .body(ApiErrorResponse.of(ApiErrorCode.INTERNAL_SERVER_ERROR.name(), exception.getMessage()));
    }

    /**
     * 필드 오류를 문자열로 포맷합니다.
     *
     * @param fieldError 필드 오류
     * @return 포맷된 오류 메시지
     */
    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
