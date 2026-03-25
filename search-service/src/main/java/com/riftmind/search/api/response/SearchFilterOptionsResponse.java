package com.riftmind.search.api.response;

import java.util.List;

/**
 * 현재 소환사 기준 고급 검색 필터 옵션을 담는 응답 DTO입니다.
 *
 * @param champions 챔피언 필터 목록
 * @param positions 포지션 필터 목록
 * @param modes 게임 모드 필터 목록
 */
public record SearchFilterOptionsResponse(
        List<SearchFilterChampionOptionResponse> champions,
        List<SearchFilterPositionOptionResponse> positions,
        List<SearchFilterModeOptionResponse> modes
) {
}
