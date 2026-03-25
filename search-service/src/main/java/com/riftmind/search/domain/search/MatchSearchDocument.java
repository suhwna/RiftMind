package com.riftmind.search.domain.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * 매치 참가자 기준 Elasticsearch 검색 문서입니다.
 *
 * @author 정수환
 * @since 2026-03-25
 */
@Document(indexName = MatchSearchDocument.INDEX_NAME)
public class MatchSearchDocument {

    public static final String INDEX_NAME = "match-search-v3";

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String matchId;

    @Field(type = FieldType.Keyword)
    private String puuid;

    @Field(type = FieldType.Keyword)
    private String gameCreation;

    @Field(type = FieldType.Integer)
    private Integer queueId;

    @Field(type = FieldType.Keyword)
    private String queueNameKo;

    @Field(type = FieldType.Keyword)
    private String gameMode;

    @Field(type = FieldType.Keyword)
    private String summonerName;

    @Field(type = FieldType.Keyword)
    private String championName;

    @Field(type = FieldType.Keyword)
    private String championNameKo;

    @Field(type = FieldType.Keyword)
    private String teamPosition;

    @Field(type = FieldType.Keyword)
    private String teamPositionKo;

    @Field(type = FieldType.Integer)
    private int totalDamageDealtToChampions;

    @Field(type = FieldType.Integer)
    private int goldEarned;

    @Field(type = FieldType.Integer)
    private int totalMinionsKilled;

    @Field(type = FieldType.Integer)
    private int neutralMinionsKilled;

    @Field(type = FieldType.Integer)
    private int totalCs;

    @Field(type = FieldType.Integer)
    private int visionScore;

    @Field(type = FieldType.Integer)
    private int wardsPlaced;

    @Field(type = FieldType.Integer)
    private int wardsKilled;

    @Field(type = FieldType.Integer)
    private int champLevel;

    @Field(type = FieldType.Keyword)
    private List<Integer> itemIds;

    @Field(type = FieldType.Keyword)
    private List<String> itemNames;

    @Field(type = FieldType.Keyword)
    private List<Integer> summonerSpellIds;

    @Field(type = FieldType.Keyword)
    private List<String> summonerSpellNames;

    @Field(type = FieldType.Integer)
    private Integer primaryRune;

    @Field(type = FieldType.Keyword)
    private String primaryRuneName;

    @Field(type = FieldType.Integer)
    private Integer secondaryRune;

    @Field(type = FieldType.Keyword)
    private String secondaryRuneName;

    @Field(type = FieldType.Integer)
    private int kills;

    @Field(type = FieldType.Integer)
    private int deaths;

    @Field(type = FieldType.Integer)
    private int assists;

    @Field(type = FieldType.Double)
    private double kda;

    @Field(type = FieldType.Boolean)
    private boolean win;

    @Field(type = FieldType.Integer)
    private int totalDamageTaken;

    protected MatchSearchDocument() {
    }

    public MatchSearchDocument(
            String id,
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
        this.id = id;
        this.matchId = matchId;
        this.puuid = puuid;
        this.gameCreation = gameCreation;
        this.queueId = queueId;
        this.queueNameKo = queueNameKo;
        this.gameMode = gameMode;
        this.summonerName = summonerName;
        this.championName = championName;
        this.championNameKo = championNameKo;
        this.teamPosition = teamPosition;
        this.teamPositionKo = teamPositionKo;
        this.totalDamageDealtToChampions = totalDamageDealtToChampions;
        this.goldEarned = goldEarned;
        this.totalMinionsKilled = totalMinionsKilled;
        this.neutralMinionsKilled = neutralMinionsKilled;
        this.totalCs = totalCs;
        this.visionScore = visionScore;
        this.wardsPlaced = wardsPlaced;
        this.wardsKilled = wardsKilled;
        this.champLevel = champLevel;
        this.itemIds = itemIds;
        this.itemNames = itemNames;
        this.summonerSpellIds = summonerSpellIds;
        this.summonerSpellNames = summonerSpellNames;
        this.primaryRune = primaryRune;
        this.primaryRuneName = primaryRuneName;
        this.secondaryRune = secondaryRune;
        this.secondaryRuneName = secondaryRuneName;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.kda = kda;
        this.win = win;
        this.totalDamageTaken = totalDamageTaken;
    }

    public String getId() { return id; }
    public String getMatchId() { return matchId; }
    public String getPuuid() { return puuid; }
    public String getGameCreation() { return gameCreation; }
    public Integer getQueueId() { return queueId; }
    public String getQueueNameKo() { return queueNameKo; }
    public String getGameMode() { return gameMode; }
    public String getSummonerName() { return summonerName; }
    public String getChampionName() { return championName; }
    public String getChampionNameKo() { return championNameKo; }
    public String getTeamPosition() { return teamPosition; }
    public String getTeamPositionKo() { return teamPositionKo; }
    public int getTotalDamageDealtToChampions() { return totalDamageDealtToChampions; }
    public int getGoldEarned() { return goldEarned; }
    public int getTotalMinionsKilled() { return totalMinionsKilled; }
    public int getNeutralMinionsKilled() { return neutralMinionsKilled; }
    public int getTotalCs() { return totalCs; }
    public int getVisionScore() { return visionScore; }
    public int getWardsPlaced() { return wardsPlaced; }
    public int getWardsKilled() { return wardsKilled; }
    public int getChampLevel() { return champLevel; }
    public List<Integer> getItemIds() { return itemIds; }
    public List<String> getItemNames() { return itemNames; }
    public List<Integer> getSummonerSpellIds() { return summonerSpellIds; }
    public List<String> getSummonerSpellNames() { return summonerSpellNames; }
    public Integer getPrimaryRune() { return primaryRune; }
    public String getPrimaryRuneName() { return primaryRuneName; }
    public Integer getSecondaryRune() { return secondaryRune; }
    public String getSecondaryRuneName() { return secondaryRuneName; }
    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public int getAssists() { return assists; }
    public double getKda() { return kda; }
    public boolean isWin() { return win; }
    public int getTotalDamageTaken() { return totalDamageTaken; }
}
