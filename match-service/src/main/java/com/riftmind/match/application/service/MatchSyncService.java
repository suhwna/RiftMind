package com.riftmind.match.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.riftmind.match.application.dto.MatchSyncResult;
import com.riftmind.match.domain.match.MatchParticipant;
import com.riftmind.match.domain.match.MatchSummary;
import com.riftmind.match.domain.sync.SyncHistory;
import com.riftmind.match.global.exception.RiotApiException;
import com.riftmind.match.infrastructure.config.RiotApiProperties;
import com.riftmind.match.infrastructure.persistence.repository.MatchSummaryRepository;
import com.riftmind.match.infrastructure.persistence.repository.SyncHistoryRepository;
import com.riftmind.match.infrastructure.riot.RiotMatchParticipantPayload;
import com.riftmind.match.infrastructure.riot.RiotMatchPayload;
import com.riftmind.match.infrastructure.riot.RiotPlatformGateway;

/**
 * PUUID 기준으로 최근 매치를 수집하고 저장합니다.
 *
 * @author 정수환
 * @since 2026-03-24
 */
@Service
public class MatchSyncService {

    private final RiotPlatformGateway riotPlatformGateway;
    private final RiotApiProperties riotApiProperties;
    private final MatchSummaryRepository matchSummaryRepository;
    private final SyncHistoryRepository syncHistoryRepository;

    public MatchSyncService(
            RiotPlatformGateway riotPlatformGateway,
            RiotApiProperties riotApiProperties,
            MatchSummaryRepository matchSummaryRepository,
            SyncHistoryRepository syncHistoryRepository) {
        this.riotPlatformGateway = riotPlatformGateway;
        this.riotApiProperties = riotApiProperties;
        this.matchSummaryRepository = matchSummaryRepository;
        this.syncHistoryRepository = syncHistoryRepository;
    }

    /**
     * PUUID 기준으로 최근 매치 정보를 동기화합니다.
     *
     * @param puuid Riot PUUID
     * @param requestedMatchCount 요청한 경기 수
     * @return 동기화 결과
     */
    @Transactional
    public MatchSyncResult sync(String puuid, Integer requestedMatchCount) {
        int normalizedCount = normalizeMatchCount(requestedMatchCount);

        try {
            List<String> matchIds = riotPlatformGateway.fetchMatchIdsByPuuid(puuid, normalizedCount);
            LocalDateTime syncedAt = LocalDateTime.now();

            int savedMatchCount = 0;
            for (String matchId : matchIds) {
                RiotMatchPayload riotMatch = riotPlatformGateway.fetchMatchById(matchId);
                upsertMatch(riotMatch);
                savedMatchCount++;
            }

            syncHistoryRepository.save(SyncHistory.success(puuid, normalizedCount, savedMatchCount));

            return new MatchSyncResult(
                    puuid,
                    normalizedCount,
                    savedMatchCount,
                    syncedAt);
        } catch (RuntimeException exception) {
            syncHistoryRepository.save(SyncHistory.failure(puuid, normalizedCount, exception.getMessage()));
            if (exception instanceof RiotApiException riotApiException) {
                throw riotApiException;
            }
            throw new RiotApiException("Failed to sync match data: " + exception.getMessage());
        }
    }

    /**
     * 요청한 경기 수를 시스템 허용 범위로 보정합니다.
     *
     * @param requestedMatchCount 요청한 경기 수
     * @return 보정된 경기 수
     */
    private int normalizeMatchCount(Integer requestedMatchCount) {
        int defaultCount = riotApiProperties.getMatchFetchMaxCount();
        if (requestedMatchCount == null) {
            return defaultCount;
        }
        if (requestedMatchCount < 1) {
            return 1;
        }
        return Math.min(requestedMatchCount, riotApiProperties.getMatchFetchMaxCount());
    }

    /**
     * 매치 정보를 생성하거나 갱신합니다.
     *
     * @param riotMatch Riot 매치 정보
     */
    private void upsertMatch(RiotMatchPayload riotMatch) {
        MatchSummary matchSummary = matchSummaryRepository.findById(riotMatch.matchId())
                .orElseGet(() -> new MatchSummary(riotMatch.matchId()));

        matchSummary.updateSummary(
                riotMatch.gameCreation(),
                riotMatch.gameDuration(),
                riotMatch.queueId(),
                riotMatch.gameMode(),
                riotMatch.gameVersion());
        matchSummary.replaceParticipants(toParticipants(riotMatch.participants()));

        matchSummaryRepository.save(matchSummary);
    }

    /**
     * Riot 참가자 목록을 도메인 참가자 목록으로 변환합니다.
     *
     * @param participants Riot 참가자 목록
     * @return 도메인 참가자 목록
     */
    private List<MatchParticipant> toParticipants(List<RiotMatchParticipantPayload> participants) {
        return participants.stream()
                .map(this::toParticipant)
                .toList();
    }

    /**
     * Riot 참가자 정보를 도메인 참가자 객체로 변환합니다.
     *
     * @param participant Riot 참가자 정보
     * @return 도메인 참가자 객체
     */
    private MatchParticipant toParticipant(RiotMatchParticipantPayload participant) {
        return new MatchParticipant(
                participant.puuid(),
                participant.summonerName(),
                participant.championName(),
                participant.teamId(),
                participant.teamPosition(),
                participant.kills(),
                participant.deaths(),
                participant.assists(),
                participant.win(),
                participant.totalDamageDealtToChampions(),
                participant.goldEarned(),
                participant.totalMinionsKilled(),
                participant.neutralMinionsKilled(),
                participant.visionScore(),
                participant.wardsPlaced(),
                participant.wardsKilled(),
                participant.champLevel(),
                participant.item0(),
                participant.item1(),
                participant.item2(),
                participant.item3(),
                participant.item4(),
                participant.item5(),
                participant.item6(),
                participant.summoner1Id(),
                participant.summoner2Id(),
                participant.primaryRune(),
                participant.secondaryRune(),
                participant.totalDamageTaken());
    }
}
