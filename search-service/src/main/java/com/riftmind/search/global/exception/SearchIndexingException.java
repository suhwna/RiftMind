package com.riftmind.search.global.exception;

public class SearchIndexingException extends ApiException {

    public SearchIndexingException(String message) {
        super(ApiErrorCode.SEARCH_INDEXING_ERROR, message);
    }

    public SearchIndexingException(String message, Throwable cause) {
        super(ApiErrorCode.SEARCH_INDEXING_ERROR, message, cause);
    }
}
