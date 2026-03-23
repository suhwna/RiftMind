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
            boolean win) {
        this.puuid = puuid;
        this.summonerName = summonerName;
        this.championName = championName;
        this.teamPosition = teamPosition;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.win = win;
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
     * 소환사 이름을 반환합니다.
     *
     * @return 소환사 이름
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
}
