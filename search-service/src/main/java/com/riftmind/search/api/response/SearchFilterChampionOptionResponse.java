package com.riftmind.search.api.response;

/**
 * 챔피언 필터 선택 항목을 담는 응답 DTO입니다.
 *
 * @param championName 챔피언 영문 이름
 * @param championNameKo 챔피언 한글 이름
 */
public record SearchFilterChampionOptionResponse(
        String championName,
        String championNameKo
) {
}
