package com.riftmind.search.api.response;

/**
 * 포지션 필터 선택 항목을 담는 응답 DTO입니다.
 *
 * @param teamPosition 포지션 코드
 * @param teamPositionKo 포지션 한글 이름
 */
public record SearchFilterPositionOptionResponse(
        String teamPosition,
        String teamPositionKo
) {
}
