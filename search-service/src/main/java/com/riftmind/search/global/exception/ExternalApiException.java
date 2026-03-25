package com.riftmind.search.global.exception;

public class ExternalApiException extends ApiException {

    public ExternalApiException(String message) {
        super(ApiErrorCode.MATCH_SERVICE_ERROR, message);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(ApiErrorCode.MATCH_SERVICE_ERROR, message, cause);
    }
}
