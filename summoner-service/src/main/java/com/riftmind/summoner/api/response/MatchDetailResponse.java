package com.riftmind.summoner.api.response;

import java.time.LocalDateTime;
import java.util.List;

import com.riftmind.summoner.application.dto.MatchDetailView;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 매치 상세 정보를 담는 API 응답 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Schema(description = "매치 상세 응답")
public record MatchDetailResponse(
        @Schema(description = "Riot matchId", example = "KR_1234567890")
        String matchId,
        @Schema(description = "게임 시작 시각", example = "2026-03-23T14:30:00")
        LocalDateTime gameCreation,
        @Schema(description = "게임 길이(초)", example = "1920")
        Integer gameDuration,
        @Schema(description = "큐 ID", example = "420")
        Integer queueId,
        @Schema(description = "게임 모드", example = "CLASSIC")
        String gameMode,
        @Schema(description = "게임 버전", example = "15.6.1")
        String gameVersion,
        @ArraySchema(schema = @Schema(implementation = MatchParticipantResponse.class))
        List<MatchParticipantResponse> participants) {

    /**
     * 매치 상세 조회 DTO를 API 응답 DTO로 변환합니다.
     *
     * @param view 매치 상세 조회 DTO
     * @return 매치 상세 응답 DTO
     */
    public static MatchDetailResponse from(MatchDetailView view) {
        return new MatchDetailResponse(
                view.matchId(),
                view.gameCreation(),
                view.gameDuration(),
                view.queueId(),
                view.gameMode(),
                view.gameVersion(),
                view.participants().stream().map(MatchParticipantResponse::from).toList());
    }
}
