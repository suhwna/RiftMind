package com.riftmind.search.global.response;

import com.riftmind.search.global.exception.ApiErrorCode;
import com.riftmind.search.global.exception.ApiException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception) {
        return ResponseEntity
                .status(exception.errorCode().status())
                .body(ApiErrorResponse.of(exception.errorCode(), exception.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiErrorResponse> handleValidationException(Exception exception) {
        return ResponseEntity
                .status(ApiErrorCode.INVALID_REQUEST.status())
                .body(ApiErrorResponse.of(ApiErrorCode.INVALID_REQUEST, exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception exception) {
        return ResponseEntity
                .status(ApiErrorCode.INTERNAL_SERVER_ERROR.status())
                .body(ApiErrorResponse.of(ApiErrorCode.INTERNAL_SERVER_ERROR, exception.getMessage()));
    }
}
