package com.riftmind.summoner.application.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.riftmind.summoner.application.dto.MatchDetailView;
import com.riftmind.summoner.application.dto.RecentMatchView;
import com.riftmind.summoner.global.exception.ApiErrorCode;
import com.riftmind.summoner.global.exception.ResourceNotFoundException;
import com.riftmind.summoner.infrastructure.persistence.repository.MatchParticipantRepository;
import com.riftmind.summoner.infrastructure.persistence.repository.MatchSummaryRepository;

/**
 * 저장된 매치 데이터 조회를 담당합니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Service
@Transactional(readOnly = true)
public class MatchQueryService {

    private final MatchSummaryRepository matchSummaryRepository;
    private final MatchParticipantRepository matchParticipantRepository;

    public MatchQueryService(
            MatchSummaryRepository matchSummaryRepository,
            MatchParticipantRepository matchParticipantRepository) {
        this.matchSummaryRepository = matchSummaryRepository;
        this.matchParticipantRepository = matchParticipantRepository;
    }

    /**
     * 소환사의 최근 경기 목록을 조회합니다.
     *
     * @param puuid Riot PUUID
     * @param count 조회할 경기 수
     * @return 최근 경기 목록
     */
    public List<RecentMatchView> getRecentMatches(String puuid, int count) {
        return matchParticipantRepository
                .findByPuuidOrderByMatchSummaryGameCreationDesc(puuid, PageRequest.of(0, count))
                .stream()
                .map(RecentMatchView::from)
                .toList();
    }

    /**
     * matchId 기준으로 매치 상세 정보를 조회합니다.
     *
     * @param matchId Riot matchId
     * @return 매치 상세 정보
     */
    public MatchDetailView getMatchDetail(String matchId) {
        return matchSummaryRepository.findWithParticipantsByMatchId(matchId)
                .map(MatchDetailView::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ApiErrorCode.MATCH_NOT_FOUND,
                        "Match not found for matchId: " + matchId));
    }
}
