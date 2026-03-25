package com.riftmind.search.api.response;

import java.time.LocalDateTime;

public record MatchIndexResponse(
        String puuid,
        int requestedMatchCount,
        int indexedCount,
        LocalDateTime indexedAt
) {
}
