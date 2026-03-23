package com.riftmind.summoner.application.dto;

import java.time.LocalDateTime;

import com.riftmind.summoner.domain.match.MatchParticipant;

/**
 * 최근 경기 목록 조회용 애플리케이션 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public record RecentMatchView(
        String matchId,
        LocalDateTime gameCreation,
        Integer queueId,
        String gameMode,
        String championName,
        String teamPosition,
        int kills,
        int deaths,
        int assists,
        boolean win) {

    /**
     * 참가자 엔티티를 최근 경기 요약 DTO로 변환합니다.
     *
     * @param participant 참가자 엔티티
     * @return 최근 경기 요약 DTO
     */
    public static RecentMatchView from(MatchParticipant participant) {
        return new RecentMatchView(
                participant.getMatchSummary().getMatchId(),
                participant.getMatchSummary().getGameCreation(),
                participant.getMatchSummary().getQueueId(),
                participant.getMatchSummary().getGameMode(),
                participant.getChampionName(),
                participant.getTeamPosition(),
                participant.getKills(),
                participant.getDeaths(),
                participant.getAssists(),
                participant.isWin());
    }
}
