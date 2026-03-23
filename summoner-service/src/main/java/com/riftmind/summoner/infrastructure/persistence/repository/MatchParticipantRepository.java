package com.riftmind.summoner.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.riftmind.summoner.domain.match.MatchParticipant;

/**
 * 매치 참가자 저장소입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, Long> {

    /**
     * PUUID 기준으로 최근 경기 참가 이력을 조회합니다.
     *
     * @param puuid Riot PUUID
     * @param pageable 페이지 정보
     * @return 참가 이력 페이지
     */
    Page<MatchParticipant> findByPuuidOrderByMatchSummaryGameCreationDesc(String puuid, Pageable pageable);
}
