package com.riftmind.search.api.response;

import java.util.List;

/**
 * 챔피언 자동완성 목록을 담는 응답 DTO입니다.
 *
 * @param suggestions 자동완성 항목 목록
 */
public record ChampionSuggestionListResponse(
        List<ChampionSuggestionResponse> suggestions
) {
}
