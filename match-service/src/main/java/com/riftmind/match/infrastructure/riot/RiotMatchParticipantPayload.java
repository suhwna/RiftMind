package com.riftmind.match.infrastructure.riot;

/**
 * Riot 매치 참가자 정보를 담는 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public record RiotMatchParticipantPayload(
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
}
