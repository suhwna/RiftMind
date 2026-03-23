package com.riftmind.summoner.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.riftmind.summoner.domain.match.MatchSummary;

/**
 * 매치 요약 저장소입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public interface MatchSummaryRepository extends JpaRepository<MatchSummary, String> {

    /**
     * 참가자 정보를 함께 포함해 매치 상세를 조회합니다.
     *
     * @param matchId Riot matchId
     * @return 매치 조회 결과
     */
    @EntityGraph(attributePaths = "participants")
    Optional<MatchSummary> findWithParticipantsByMatchId(String matchId);
}
