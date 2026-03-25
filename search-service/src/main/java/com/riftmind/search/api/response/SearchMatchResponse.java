package com.riftmind.search.api.response;

import com.riftmind.search.domain.search.MatchSearchDocument;

import java.util.List;

public record SearchMatchResponse(
        String matchId,
        String puuid,
        String gameCreation,
        Integer queueId,
        String queueNameKo,
        String gameMode,
        String summonerName,
        String championName,
        String championNameKo,
        String teamPosition,
        String teamPositionKo,
        int totalDamageDealtToChampions,
        int goldEarned,
        int totalMinionsKilled,
        int neutralMinionsKilled,
        int totalCs,
        int visionScore,
        int wardsPlaced,
        int wardsKilled,
        int champLevel,
        List<Integer> itemIds,
        List<String> itemNames,
        List<Integer> summonerSpellIds,
        List<String> summonerSpellNames,
        Integer primaryRune,
        String primaryRuneName,
        Integer secondaryRune,
        String secondaryRuneName,
        int kills,
        int deaths,
        int assists,
        double kda,
        boolean win,
        int totalDamageTaken
) {

    public static SearchMatchResponse from(MatchSearchDocument document) {
        return new SearchMatchResponse(
                document.getMatchId(),
                document.getPuuid(),
                document.getGameCreation(),
                document.getQueueId(),
                document.getQueueNameKo(),
                document.getGameMode(),
                document.getSummonerName(),
                document.getChampionName(),
                document.getChampionNameKo(),
                document.getTeamPosition(),
                document.getTeamPositionKo(),
                document.getTotalDamageDealtToChampions(),
                document.getGoldEarned(),
                document.getTotalMinionsKilled(),
                document.getNeutralMinionsKilled(),
                document.getTotalCs(),
                document.getVisionScore(),
                document.getWardsPlaced(),
                document.getWardsKilled(),
                document.getChampLevel(),
                document.getItemIds(),
                document.getItemNames(),
                document.getSummonerSpellIds(),
                document.getSummonerSpellNames(),
                document.getPrimaryRune(),
                document.getPrimaryRuneName(),
                document.getSecondaryRune(),
                document.getSecondaryRuneName(),
                document.getKills(),
                document.getDeaths(),
                document.getAssists(),
                document.getKda(),
                document.isWin(),
                document.getTotalDamageTaken()
        );
    }
}
