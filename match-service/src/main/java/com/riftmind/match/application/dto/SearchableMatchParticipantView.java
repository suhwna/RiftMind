package com.riftmind.match.application.dto;

import com.riftmind.match.domain.match.MatchParticipant;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 검색 색인용 매치 참가자 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-25
 */
public record SearchableMatchParticipantView(
        String matchId,
        LocalDateTime gameCreation,
        Integer queueId,
        String gameMode,
        String puuid,
        String summonerName,
        String championName,
        String teamPosition,
        String opponentChampionName,
        int kills,
        int deaths,
        int assists,
        boolean win,
        int totalDamageDealtToChampions,
        int goldEarned,
        int totalMinionsKilled,
        int neutralMinionsKilled,
        int visionScore,
        int wardsPlaced,
        int wardsKilled,
        int champLevel,
        int item0,
        int item1,
        int item2,
        int item3,
        int item4,
        int item5,
        int item6,
        int summoner1Id,
        int summoner2Id,
        Integer primaryRune,
        Integer secondaryRune,
        List<String> interpretationTags,
        int totalDamageTaken) {

    /**
     * 참가자 엔티티를 검색 색인용 DTO로 변환합니다.
     *
     * @param participant 참가자 엔티티
     * @return 검색 색인용 DTO
     */
    public static SearchableMatchParticipantView from(MatchParticipant participant) {
        MatchParticipant opponent = resolveLaneOpponent(participant);

        return new SearchableMatchParticipantView(
                participant.getMatchSummary().getMatchId(),
                participant.getMatchSummary().getGameCreation(),
                participant.getMatchSummary().getQueueId(),
                participant.getMatchSummary().getGameMode(),
                participant.getPuuid(),
                participant.getSummonerName(),
                participant.getChampionName(),
                participant.getTeamPosition(),
                opponent != null ? opponent.getChampionName() : null,
                participant.getKills(),
                participant.getDeaths(),
                participant.getAssists(),
                participant.isWin(),
                participant.getTotalDamageDealtToChampions(),
                participant.getGoldEarned(),
                participant.getTotalMinionsKilled(),
                participant.getNeutralMinionsKilled(),
                participant.getVisionScore(),
                participant.getWardsPlaced(),
                participant.getWardsKilled(),
                participant.getChampLevel(),
                participant.getItem0(),
                participant.getItem1(),
                participant.getItem2(),
                participant.getItem3(),
                participant.getItem4(),
                participant.getItem5(),
                participant.getItem6(),
                participant.getSummoner1Id(),
                participant.getSummoner2Id(),
                participant.getPrimaryRune(),
                participant.getSecondaryRune(),
                MatchInterpretationTagResolver.resolve(participant),
                participant.getTotalDamageTaken());
    }

    /**
     * 같은 경기에서 동일 포지션의 상대 참가자를 찾습니다.
     *
     * @param participant 기준 참가자
     * @return 상대 라인 참가자
     */
    private static MatchParticipant resolveLaneOpponent(MatchParticipant participant) {
        if (participant.getTeamId() == null || participant.getTeamPosition() == null) {
            return null;
        }

        return participant.getMatchSummary().getParticipants().stream()
                .filter(candidate -> !candidate.getPuuid().equals(participant.getPuuid()))
                .filter(candidate -> candidate.getTeamId() != null && !candidate.getTeamId().equals(participant.getTeamId()))
                .filter(candidate -> participant.getTeamPosition().equals(candidate.getTeamPosition()))
                .findFirst()
                .orElse(null);
    }
}
