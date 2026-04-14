package com.riftmind.ai.infrastructure.match;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * match-service 참가자 응답 중 AI 회고에 필요한 필드를 담습니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MatchParticipantResponse(
        String puuid,
        String summonerName,
        String championName,
        String championNameKo,
        Integer teamId,
        String teamPosition,
        String teamPositionKo,
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
        List<String> itemNames,
        int summoner1Id,
        int summoner2Id,
        List<String> summonerSpellNames,
        Integer primaryRune,
        String primaryRuneName,
        Integer secondaryRune,
        String secondaryRuneName,
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
}
