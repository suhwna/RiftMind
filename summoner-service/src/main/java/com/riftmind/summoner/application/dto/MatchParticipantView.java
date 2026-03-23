package com.riftmind.summoner.application.dto;

import com.riftmind.summoner.domain.match.MatchParticipant;

/**
 * 매치 참가자 조회용 애플리케이션 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public record MatchParticipantView(
        String puuid,
        String summonerName,
        String championName,
        String teamPosition,
        int kills,
        int deaths,
        int assists,
        boolean win) {

    /**
     * 참가자 엔티티를 참가자 조회 DTO로 변환합니다.
     *
     * @param participant 참가자 엔티티
     * @return 참가자 조회 DTO
     */
    public static MatchParticipantView from(MatchParticipant participant) {
        return new MatchParticipantView(
                participant.getPuuid(),
                participant.getSummonerName(),
                participant.getChampionName(),
                participant.getTeamPosition(),
                participant.getKills(),
                participant.getDeaths(),
                participant.getAssists(),
                participant.isWin());
    }
}
