package com.riftmind.search.api.response;

/**
 * 게임 모드 필터 선택 항목을 담는 응답 DTO입니다.
 *
 * @param queueId queueId
 * @param queueNameKo 게임 모드 한글 이름
 */
public record SearchFilterModeOptionResponse(
        Integer queueId,
        String queueNameKo
) {
}
