package com.riftmind.summoner.domain.summoner;

import java.time.LocalDateTime;

import com.riftmind.summoner.domain.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 소환사 프로필 정보를 저장하는 엔티티입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Entity
@Table(
        name = "summoner_profile",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_summoner_profile_riot_id",
                columnNames = {"game_name", "tag_line"}))
public class SummonerProfile extends BaseTimeEntity {

    @Id
    @Column(length = 100, nullable = false)
    private String puuid;

    @Column(name = "game_name", length = 50, nullable = false)
    private String gameName;

    @Column(name = "tag_line", length = 10, nullable = false)
    private String tagLine;

    @Column(name = "summoner_id", length = 100)
    private String summonerId;

    @Column(name = "account_id", length = 100)
    private String accountId;

    @Column(name = "profile_icon_id")
    private Integer profileIconId;

    @Column(name = "summoner_level")
    private Long summonerLevel;

    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;

    protected SummonerProfile() {
    }

    public SummonerProfile(String puuid) {
        this.puuid = puuid;
    }

    /**
     * 소환사 프로필 정보를 갱신합니다.
     *
     * @param gameName 라이엇 게임 이름
     * @param tagLine 라이엇 태그 라인
     * @param summonerId Riot summonerId
     * @param accountId Riot accountId
     * @param profileIconId 프로필 아이콘 ID
     * @param summonerLevel 소환사 레벨
     * @param lastSyncedAt 마지막 동기화 시각
     */
    public void updateProfile(
            String gameName,
            String tagLine,
            String summonerId,
            String accountId,
            Integer profileIconId,
            Long summonerLevel,
            LocalDateTime lastSyncedAt) {
        this.gameName = gameName;
        this.tagLine = tagLine;
        this.summonerId = summonerId;
        this.accountId = accountId;
        this.profileIconId = profileIconId;
        this.summonerLevel = summonerLevel;
        this.lastSyncedAt = lastSyncedAt;
    }

    public String getPuuid() {
        return puuid;
    }

    public String getGameName() {
        return gameName;
    }

    public String getTagLine() {
        return tagLine;
    }

    public String getSummonerId() {
        return summonerId;
    }

    public String getAccountId() {
        return accountId;
    }

    public Integer getProfileIconId() {
        return profileIconId;
    }

    public Long getSummonerLevel() {
        return summonerLevel;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }
}
