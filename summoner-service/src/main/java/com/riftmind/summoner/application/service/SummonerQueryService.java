package com.riftmind.summoner.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.riftmind.summoner.application.dto.SummonerProfileView;
import com.riftmind.summoner.global.exception.ApiErrorCode;
import com.riftmind.summoner.global.exception.ResourceNotFoundException;
import com.riftmind.summoner.infrastructure.persistence.repository.SummonerProfileRepository;

/**
 * 저장된 소환사 프로필 조회를 담당합니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Service
@Transactional(readOnly = true)
public class SummonerQueryService {

    private final SummonerProfileRepository summonerProfileRepository;

    public SummonerQueryService(SummonerProfileRepository summonerProfileRepository) {
        this.summonerProfileRepository = summonerProfileRepository;
    }

    /**
     * Riot ID 기준으로 저장된 소환사 프로필을 조회합니다.
     *
     * @param gameName 라이엇 게임 이름
     * @param tagLine 라이엇 태그 라인
     * @return 소환사 프로필 조회 결과
     */
    public SummonerProfileView getByRiotId(String gameName, String tagLine) {
        return summonerProfileRepository.findByGameNameAndTagLine(gameName, tagLine)
                .map(SummonerProfileView::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ApiErrorCode.SUMMONER_NOT_FOUND,
                        "Summoner not found for Riot ID: %s#%s".formatted(gameName, tagLine)));
    }

    /**
     * PUUID 기준으로 저장된 소환사 프로필을 조회합니다.
     *
     * @param puuid Riot PUUID
     * @return 소환사 프로필 조회 결과
     */
    public SummonerProfileView getByPuuid(String puuid) {
        return summonerProfileRepository.findById(puuid)
                .map(SummonerProfileView::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ApiErrorCode.SUMMONER_NOT_FOUND,
                        "Summoner not found for puuid: " + puuid));
    }
}
