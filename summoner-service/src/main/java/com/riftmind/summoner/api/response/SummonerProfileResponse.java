package com.riftmind.summoner.api.response;

import java.time.LocalDateTime;

import com.riftmind.summoner.application.dto.SummonerProfileView;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 소환사 프로필 응답 정보를 담는 API 응답 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Schema(description = "소환사 프로필 응답")
public record SummonerProfileResponse(
        @Schema(description = "Riot PUUID", example = "sample-puuid-value")
        String puuid,
        @Schema(description = "라이엇 게임 이름", example = "Hide on bush")
        String gameName,
        @Schema(description = "라이엇 태그 라인", example = "KR1")
        String tagLine,
        @Schema(description = "Riot summonerId", example = "sample-summoner-id")
        String summonerId,
        @Schema(description = "Riot accountId", example = "sample-account-id")
        String accountId,
        @Schema(description = "프로필 아이콘 ID", example = "685")
        Integer profileIconId,
        @Schema(description = "소환사 레벨", example = "987")
        Long summonerLevel,
        @Schema(description = "마지막 동기화 시각", example = "2026-03-23T14:30:00")
        LocalDateTime lastSyncedAt) {

    /**
     * 애플리케이션 조회 DTO를 API 응답 DTO로 변환합니다.
     *
     * @param view 소환사 프로필 조회 DTO
     * @return 소환사 프로필 응답 DTO
     */
    public static SummonerProfileResponse from(SummonerProfileView view) {
        return new SummonerProfileResponse(
                view.puuid(),
                view.gameName(),
                view.tagLine(),
                view.summonerId(),
                view.accountId(),
                view.profileIconId(),
                view.summonerLevel(),
                view.lastSyncedAt());
    }
}
