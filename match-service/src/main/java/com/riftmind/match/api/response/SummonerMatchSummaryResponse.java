package com.riftmind.match.api.response;

import java.time.LocalDateTime;

import com.riftmind.match.application.dto.RecentMatchView;
import com.riftmind.match.application.service.StaticDataService;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 소환사 경기 요약을 담는 API 응답 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Schema(description = "소환사 경기 요약 응답")
public record SummonerMatchSummaryResponse(
        @Schema(description = "Riot matchId", example = "KR_1234567890")
        String matchId,
        @Schema(description = "게임 시작 시각", example = "2026-03-23T14:30:00")
        LocalDateTime gameCreation,
        @Schema(description = "큐 ID", example = "420")
        Integer queueId,
        @Schema(description = "큐 한글 이름", example = "솔로 랭크")
        String queueNameKo,
        @Schema(description = "게임 모드", example = "CLASSIC")
        String gameMode,
        @Schema(description = "플레이 챔피언", example = "Ahri")
        String championName,
        @Schema(description = "챔피언 아이콘 키", example = "Ahri")
        String championKey,
        @Schema(description = "플레이 챔피언 한글 이름", example = "아리")
        String championNameKo,
        @Schema(description = "라인 포지션", example = "MIDDLE")
        String teamPosition,
        @Schema(description = "라인 포지션 한글 이름", example = "미드")
        String teamPositionKo,
        @Schema(description = "킬 수", example = "10")
        int kills,
        @Schema(description = "데스 수", example = "2")
        int deaths,
        @Schema(description = "어시스트 수", example = "8")
        int assists,
        @Schema(description = "승리 여부", example = "true")
        boolean win) {

    /**
     * 최근 경기 조회 DTO를 API 응답 DTO로 변환합니다.
     *
     * @param view 최근 경기 조회 DTO
     * @param staticDataService 정적 데이터 서비스
     * @return 경기 요약 응답 DTO
     */
    public static SummonerMatchSummaryResponse from(RecentMatchView view, StaticDataService staticDataService) {
        return new SummonerMatchSummaryResponse(
                view.matchId(),
                view.gameCreation(),
                view.queueId(),
                staticDataService.getQueueNameKo(view.queueId()),
                view.gameMode(),
                view.championName(),
                staticDataService.getChampionAssetKey(view.championName()),
                staticDataService.getChampionNameKo(view.championName()),
                view.teamPosition(),
                staticDataService.getTeamPositionKo(view.teamPosition()),
                view.kills(),
                view.deaths(),
                view.assists(),
                view.win());
    }
}
