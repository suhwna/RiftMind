package com.riftmind.search.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record MatchIndexRequest(
        @NotBlank(message = "puuid는 필수입니다.")
        String puuid,

        @Min(value = 1, message = "matchCount는 1 이상이어야 합니다.")
        @Max(value = 100, message = "matchCount는 100 이하여야 합니다.")
        Integer matchCount
) {
}
