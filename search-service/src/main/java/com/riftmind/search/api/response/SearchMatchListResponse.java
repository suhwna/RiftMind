package com.riftmind.search.api.response;

import java.util.List;

public record SearchMatchListResponse(
        long total,
        int page,
        int size,
        List<SearchMatchResponse> matches
) {
}
