package com.riftmind.summoner.api.response;

import com.riftmind.summoner.application.dto.MatchParticipantView;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 매치 참가자 정보를 담는 API 응답 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Schema(description = "매치 참가자 응답")
public record MatchParticipantResponse(
        @Schema(description = "Riot PUUID", example = "sample-puuid-value")
        String puuid,
        @Schema(description = "소환사 이름", example = "Hide on bush")
        String summonerName,
        @Schema(description = "챔피언 이름", example = "Ahri")
        String championName,
        @Schema(description = "라인 포지션", example = "MIDDLE")
        String teamPosition,
        @Schema(description = "킬 수", example = "10")
        int kills,
        @Schema(description = "데스 수", example = "2")
        int deaths,
        @Schema(description = "어시스트 수", example = "8")
        int assists,
        @Schema(description = "승리 여부", example = "true")
        boolean win) {

    /**
     * 참가자 조회 DTO를 API 응답 DTO로 변환합니다.
     *
     * @param view 참가자 조회 DTO
     * @return 참가자 응답 DTO
     */
    public static MatchParticipantResponse from(MatchParticipantView view) {
        return new MatchParticipantResponse(
                view.puuid(),
                view.summonerName(),
                view.championName(),
                view.teamPosition(),
                view.kills(),
                view.deaths(),
                view.assists(),
                view.win());
    }
}
