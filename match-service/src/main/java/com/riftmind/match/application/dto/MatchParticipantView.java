package com.riftmind.match.application.dto;

import java.util.List;

import com.riftmind.match.domain.match.MatchParticipant;

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
        Integer teamId,
        String teamPosition,
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
        int totalDamageTaken,
        int doubleKills,
        int tripleKills,
        int quadraKills,
        int pentaKills,
        int largestKillingSpree,
        int largestMultiKill,
        int killingSprees,
        boolean firstBloodKill,
        boolean firstBloodAssist,
        boolean firstTowerKill,
        boolean firstTowerAssist,
        int turretKills,
        int inhibitorKills,
        int damageDealtToObjectives,
        int damageDealtToTurrets,
        int objectivesStolen,
        int objectivesStolenAssists,
        int totalHeal,
        int totalHealsOnTeammates,
        int totalDamageShieldedOnTeammates) {

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
                participant.getTeamId(),
                participant.getTeamPosition(),
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
                participant.getTotalDamageTaken(),
                participant.getDoubleKills(),
                participant.getTripleKills(),
                participant.getQuadraKills(),
                participant.getPentaKills(),
                participant.getLargestKillingSpree(),
                participant.getLargestMultiKill(),
                participant.getKillingSprees(),
                participant.isFirstBloodKill(),
                participant.isFirstBloodAssist(),
                participant.isFirstTowerKill(),
                participant.isFirstTowerAssist(),
                participant.getTurretKills(),
                participant.getInhibitorKills(),
                participant.getDamageDealtToObjectives(),
                participant.getDamageDealtToTurrets(),
                participant.getObjectivesStolen(),
                participant.getObjectivesStolenAssists(),
                participant.getTotalHeal(),
                participant.getTotalHealsOnTeammates(),
                participant.getTotalDamageShieldedOnTeammates());
    }
}
