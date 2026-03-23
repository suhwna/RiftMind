package com.riftmind.summoner.application.dto;

import java.time.LocalDateTime;

import com.riftmind.summoner.domain.summoner.SummonerProfile;

/**
 * 소환사 프로필 조회용 애플리케이션 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public record SummonerProfileView(
        String puuid,
        String gameName,
        String tagLine,
        String summonerId,
        String accountId,
        Integer profileIconId,
        Long summonerLevel,
        LocalDateTime lastSyncedAt) {

    /**
     * 소환사 프로필 엔티티를 조회 DTO로 변환합니다.
     *
     * @param summonerProfile 소환사 프로필 엔티티
     * @return 소환사 프로필 조회 DTO
     */
    public static SummonerProfileView from(SummonerProfile summonerProfile) {
        return new SummonerProfileView(
                summonerProfile.getPuuid(),
                summonerProfile.getGameName(),
                summonerProfile.getTagLine(),
                summonerProfile.getSummonerId(),
                summonerProfile.getAccountId(),
                summonerProfile.getProfileIconId(),
                summonerProfile.getSummonerLevel(),
                summonerProfile.getLastSyncedAt());
    }
}
