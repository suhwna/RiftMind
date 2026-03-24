package com.riftmind.match.application.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.riftmind.match.domain.match.MatchSummary;

/**
 * 매치 상세 조회용 애플리케이션 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public record MatchDetailView(
        String matchId,
        LocalDateTime gameCreation,
        Integer gameDuration,
        Integer queueId,
        String gameMode,
        String gameVersion,
        List<MatchParticipantView> participants) {

    /**
     * 매치 엔티티를 상세 조회 DTO로 변환합니다.
     *
     * @param matchSummary 매치 엔티티
     * @return 매치 상세 조회 DTO
     */
    public static MatchDetailView from(MatchSummary matchSummary) {
        return new MatchDetailView(
                matchSummary.getMatchId(),
                matchSummary.getGameCreation(),
                matchSummary.getGameDuration(),
                matchSummary.getQueueId(),
                matchSummary.getGameMode(),
                matchSummary.getGameVersion(),
                matchSummary.getParticipants().stream().map(MatchParticipantView::from).toList());
    }
}
