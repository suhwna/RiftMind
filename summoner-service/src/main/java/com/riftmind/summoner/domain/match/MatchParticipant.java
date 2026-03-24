package com.riftmind.summoner.domain.match;

import com.riftmind.summoner.domain.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 매치 참가자 정보를 저장하는 엔티티입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Entity
@Table(name = "match_participant")
public class MatchParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchSummary matchSummary;

    @Column(length = 100, nullable = false)
    private String puuid;

    @Column(name = "summoner_name", length = 100)
    private String summonerName;

    @Column(name = "champion_name", length = 50, nullable = false)
    private String championName;

    @Column(name = "team_position", length = 20)
    private String teamPosition;

    @Column(nullable = false)
    private int kills;

    @Column(nullable = false)
    private int deaths;

    @Column(nullable = false)
    private int assists;

    @Column(nullable = false)
    private boolean win;

    @Column(name = "total_damage_dealt_to_champions")
    private Integer totalDamageDealtToChampions;

    @Column(name = "gold_earned")
    private Integer goldEarned;

    @Column(name = "total_minions_killed")
    private Integer totalMinionsKilled;

    @Column(name = "neutral_minions_killed")
    private Integer neutralMinionsKilled;

    @Column(name = "vision_score")
    private Integer visionScore;

    @Column(name = "wards_placed")
    private Integer wardsPlaced;

    @Column(name = "wards_killed")
    private Integer wardsKilled;

    @Column(name = "champ_level")
    private Integer champLevel;

    @Column(name = "item_0")
    private Integer item0;

    @Column(name = "item_1")
    private Integer item1;

    @Column(name = "item_2")
    private Integer item2;

    @Column(name = "item_3")
    private Integer item3;

    @Column(name = "item_4")
    private Integer item4;

    @Column(name = "item_5")
    private Integer item5;

    @Column(name = "item_6")
    private Integer item6;

    @Column(name = "summoner_1_id")
    private Integer summoner1Id;

    @Column(name = "summoner_2_id")
    private Integer summoner2Id;

    @Column(name = "primary_rune")
    private Integer primaryRune;

    @Column(name = "secondary_rune")
    private Integer secondaryRune;

    @Column(name = "total_damage_taken")
    private Integer totalDamageTaken;

    protected MatchParticipant() {
    }

    public MatchParticipant(
            String puuid,
            String summonerName,
            String championName,
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
            int totalDamageTaken) {
        this.puuid = puuid;
        this.summonerName = summonerName;
        this.championName = championName;
        this.teamPosition = teamPosition;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.win = win;
        this.totalDamageDealtToChampions = totalDamageDealtToChampions;
        this.goldEarned = goldEarned;
        this.totalMinionsKilled = totalMinionsKilled;
        this.neutralMinionsKilled = neutralMinionsKilled;
        this.visionScore = visionScore;
        this.wardsPlaced = wardsPlaced;
        this.wardsKilled = wardsKilled;
        this.champLevel = champLevel;
        this.item0 = item0;
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
        this.item4 = item4;
        this.item5 = item5;
        this.item6 = item6;
        this.summoner1Id = summoner1Id;
        this.summoner2Id = summoner2Id;
        this.primaryRune = primaryRune;
        this.secondaryRune = secondaryRune;
        this.totalDamageTaken = totalDamageTaken;
    }

    /**
     * 참가자를 특정 매치에 연결합니다.
     *
     * @param matchSummary 연결할 매치 엔티티
     */
    void assignTo(MatchSummary matchSummary) {
        this.matchSummary = matchSummary;
    }

    /**
     * 참가자 식별자를 반환합니다.
     *
     * @return 참가자 식별자
     */
    public Long getId() {
        return id;
    }

    /**
     * 연결된 매치 엔티티를 반환합니다.
     *
     * @return 매치 엔티티
     */
    public MatchSummary getMatchSummary() {
        return matchSummary;
    }

    /**
     * PUUID를 반환합니다.
     *
     * @return Riot PUUID
     */
    public String getPuuid() {
        return puuid;
    }

    /**
     * 참가자 Riot ID를 반환합니다.
     *
     * @return 참가자 Riot ID
     */
    public String getSummonerName() {
        return summonerName;
    }

    /**
     * 챔피언 이름을 반환합니다.
     *
     * @return 챔피언 이름
     */
    public String getChampionName() {
        return championName;
    }

    /**
     * 포지션을 반환합니다.
     *
     * @return 포지션
     */
    public String getTeamPosition() {
        return teamPosition;
    }

    /**
     * 킬 수를 반환합니다.
     *
     * @return 킬 수
     */
    public int getKills() {
        return kills;
    }

    /**
     * 데스 수를 반환합니다.
     *
     * @return 데스 수
     */
    public int getDeaths() {
        return deaths;
    }

    /**
     * 어시스트 수를 반환합니다.
     *
     * @return 어시스트 수
     */
    public int getAssists() {
        return assists;
    }

    /**
     * 승리 여부를 반환합니다.
     *
     * @return 승리 여부
     */
    public boolean isWin() {
        return win;
    }

    /**
     * 챔피언 대상 피해량을 반환합니다.
     *
     * @return 챔피언 대상 피해량
     */
    public int getTotalDamageDealtToChampions() {
        return zeroIfNull(totalDamageDealtToChampions);
    }

    /**
     * 획득한 골드를 반환합니다.
     *
     * @return 획득 골드
     */
    public int getGoldEarned() {
        return zeroIfNull(goldEarned);
    }

    /**
     * 총 미니언 처치 수를 반환합니다.
     *
     * @return 총 미니언 처치 수
     */
    public int getTotalMinionsKilled() {
        return zeroIfNull(totalMinionsKilled);
    }

    /**
     * 정글 몬스터 처치 수를 반환합니다.
     *
     * @return 정글 몬스터 처치 수
     */
    public int getNeutralMinionsKilled() {
        return zeroIfNull(neutralMinionsKilled);
    }

    /**
     * 시야 점수를 반환합니다.
     *
     * @return 시야 점수
     */
    public int getVisionScore() {
        return zeroIfNull(visionScore);
    }

    /**
     * 설치한 와드 수를 반환합니다.
     *
     * @return 설치 와드 수
     */
    public int getWardsPlaced() {
        return zeroIfNull(wardsPlaced);
    }

    /**
     * 제거한 와드 수를 반환합니다.
     *
     * @return 제거 와드 수
     */
    public int getWardsKilled() {
        return zeroIfNull(wardsKilled);
    }

    /**
     * 챔피언 레벨을 반환합니다.
     *
     * @return 챔피언 레벨
     */
    public int getChampLevel() {
        return zeroIfNull(champLevel);
    }

    /**
     * 첫 번째 아이템 ID를 반환합니다.
     *
     * @return 첫 번째 아이템 ID
     */
    public int getItem0() {
        return zeroIfNull(item0);
    }

    /**
     * 두 번째 아이템 ID를 반환합니다.
     *
     * @return 두 번째 아이템 ID
     */
    public int getItem1() {
        return zeroIfNull(item1);
    }

    /**
     * 세 번째 아이템 ID를 반환합니다.
     *
     * @return 세 번째 아이템 ID
     */
    public int getItem2() {
        return zeroIfNull(item2);
    }

    /**
     * 네 번째 아이템 ID를 반환합니다.
     *
     * @return 네 번째 아이템 ID
     */
    public int getItem3() {
        return zeroIfNull(item3);
    }

    /**
     * 다섯 번째 아이템 ID를 반환합니다.
     *
     * @return 다섯 번째 아이템 ID
     */
    public int getItem4() {
        return zeroIfNull(item4);
    }

    /**
     * 여섯 번째 아이템 ID를 반환합니다.
     *
     * @return 여섯 번째 아이템 ID
     */
    public int getItem5() {
        return zeroIfNull(item5);
    }

    /**
     * 장신구 아이템 ID를 반환합니다.
     *
     * @return 장신구 아이템 ID
     */
    public int getItem6() {
        return zeroIfNull(item6);
    }

    /**
     * 첫 번째 소환사 주문 ID를 반환합니다.
     *
     * @return 첫 번째 소환사 주문 ID
     */
    public int getSummoner1Id() {
        return zeroIfNull(summoner1Id);
    }

    /**
     * 두 번째 소환사 주문 ID를 반환합니다.
     *
     * @return 두 번째 소환사 주문 ID
     */
    public int getSummoner2Id() {
        return zeroIfNull(summoner2Id);
    }

    /**
     * 주 룬 ID를 반환합니다.
     *
     * @return 주 룬 ID
     */
    public Integer getPrimaryRune() {
        return primaryRune;
    }

    /**
     * 보조 룬 ID를 반환합니다.
     *
     * @return 보조 룬 ID
     */
    public Integer getSecondaryRune() {
        return secondaryRune;
    }

    /**
     * 총 받은 피해량을 반환합니다.
     *
     * @return 총 받은 피해량
     */
    public int getTotalDamageTaken() {
        return zeroIfNull(totalDamageTaken);
    }

    private int zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }
}
