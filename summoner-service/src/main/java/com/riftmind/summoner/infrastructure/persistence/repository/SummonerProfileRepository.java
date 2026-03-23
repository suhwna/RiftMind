package com.riftmind.summoner.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.riftmind.summoner.domain.summoner.SummonerProfile;

/**
 * 소환사 프로필 저장소입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public interface SummonerProfileRepository extends JpaRepository<SummonerProfile, String> {

    /**
     * Riot ID로 소환사 프로필을 조회합니다.
     *
     * @param gameName 라이엇 게임 이름
     * @param tagLine 라이엇 태그 라인
     * @return 소환사 프로필 조회 결과
     */
    Optional<SummonerProfile> findByGameNameAndTagLine(String gameName, String tagLine);
}
