package com.riftmind.ai.api.response;

import java.util.List;

import com.riftmind.ai.application.service.MatchReviewResult;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 경기 AI 회고 응답 DTO입니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
@Schema(description = "경기 AI 회고 응답")
public record MatchReviewResponse(
        @Schema(description = "회고 요약", example = "초반 성장과 교전 기여는 좋았지만, 중후반 데스 관리가 아쉬웠습니다.")
        String summary,
        @ArraySchema(schema = @Schema(example = "딜량과 오브젝트 관여가 높아 승리 흐름을 만드는 데 기여했습니다."))
        List<String> strongPoints,
        @ArraySchema(schema = @Schema(example = "중반 이후 단독 진입으로 불필요한 데스가 늘었습니다."))
        List<String> weakPoints,
        @ArraySchema(schema = @Schema(example = "다음 경기에서는 한타 전에 와드 시야를 먼저 확보하세요."))
        List<String> nextFocus) {

    /**
     * 서비스 결과를 API 응답으로 변환합니다.
     *
     * @param result 회고 결과
     * @return 회고 응답 DTO
     */
    public static MatchReviewResponse from(MatchReviewResult result) {
        return new MatchReviewResponse(
                result.summary(),
                result.strongPoints(),
                result.weakPoints(),
                result.nextFocus());
    }
}
