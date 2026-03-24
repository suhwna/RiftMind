package com.riftmind.summoner.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.riftmind.summoner.application.dto.SummonerSyncResult;
import com.riftmind.summoner.domain.match.MatchParticipant;
import com.riftmind.summoner.domain.match.MatchSummary;
import com.riftmind.summoner.domain.summoner.SummonerProfile;
import com.riftmind.summoner.domain.sync.SyncHistory;
import com.riftmind.summoner.global.exception.RiotApiException;
import com.riftmind.summoner.infrastructure.config.RiotApiProperties;
import com.riftmind.summoner.infrastructure.persistence.repository.MatchSummaryRepository;
import com.riftmind.summoner.infrastructure.persistence.repository.SummonerProfileRepository;
import com.riftmind.summoner.infrastructure.persistence.repository.SyncHistoryRepository;
import com.riftmind.summoner.infrastructure.riot.RiotAccountPayload;
import com.riftmind.summoner.infrastructure.riot.RiotMatchParticipantPayload;
import com.riftmind.summoner.infrastructure.riot.RiotMatchPayload;
import com.riftmind.summoner.infrastructure.riot.RiotPlatformGateway;
import com.riftmind.summoner.infrastructure.riot.RiotSummonerPayload;

/**
 * Riot API 호출과 내부 저장을 조합해 소환사 동기화를 수행합니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Service
public class SummonerSyncService {

    private final RiotPlatformGateway riotPlatformGateway;
    private final RiotApiProperties riotApiProperties;
    private final SummonerProfileRepository summonerProfileRepository;
    private final MatchSummaryRepository matchSummaryRepository;
    private final SyncHistoryRepository syncHistoryRepository;

    public SummonerSyncService(
            RiotPlatformGateway riotPlatformGateway,
            RiotApiProperties riotApiProperties,
            SummonerProfileRepository summonerProfileRepository,
            MatchSummaryRepository matchSummaryRepository,
            SyncHistoryRepository syncHistoryRepository) {
        this.riotPlatformGateway = riotPlatformGateway;
        this.riotApiProperties = riotApiProperties;
        this.summonerProfileRepository = summonerProfileRepository;
        this.matchSummaryRepository = matchSummaryRepository;
        this.syncHistoryRepository = syncHistoryRepository;
    }

    /**
     * Riot ID 기준으로 계정, 소환사, 최근 매치를 동기화합니다.
     *
     * @param gameName 라이엇 게임 이름
     * @param tagLine 라이엇 태그 라인
     * @param requestedMatchCount 요청한 경기 수
     * @return 동기화 결과
     */
    @Transactional
    public SummonerSyncResult sync(String gameName, String tagLine, Integer requestedMatchCount) {
        int normalizedCount = normalizeMatchCount(requestedMatchCount);
        String puuid = null;

        try {
            RiotAccountPayload riotAccount = riotPlatformGateway.fetchAccountByRiotId(gameName, tagLine); // 계정 정보에서 PUUID를 얻어야 소환사 정보와 매치 정보를 조회할 수 있음
            puuid = riotAccount.puuid();
            RiotSummonerPayload riotSummoner = riotPlatformGateway.fetchSummonerByPuuid(puuid); // 소환사 정보는 계정 정보에서 얻은 PUUID로 조회
            List<String> matchIds = riotPlatformGateway.fetchMatchIdsByPuuid(puuid, normalizedCount); // 매치 ID 목록도 PUUID로 조회

            LocalDateTime syncedAt = LocalDateTime.now();
            upsertSummonerProfile(riotAccount, riotSummoner, syncedAt);

            int savedMatchCount = 0;
            // 매치 상세 정보는 matchId로 조회해야 하므로, matchId 목록을 먼저 얻은 후에 각각 조회
            for (String matchId : matchIds) {
                RiotMatchPayload riotMatch = riotPlatformGateway.fetchMatchById(matchId);
                upsertMatch(riotMatch);
                savedMatchCount++;
            }

            syncHistoryRepository.save(SyncHistory.success(puuid, normalizedCount, savedMatchCount));

            return new SummonerSyncResult(
                    puuid,
                    riotAccount.gameName(),
                    riotAccount.tagLine(),
                    normalizedCount,
                    savedMatchCount,
                    syncedAt);
        } catch (RuntimeException exception) {
            syncHistoryRepository.save(SyncHistory.failure(puuid, normalizedCount, exception.getMessage()));
            if (exception instanceof RiotApiException riotApiException) {
                throw riotApiException;
            }
            throw new RiotApiException("Failed to sync summoner data: " + exception.getMessage());
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
     * 소환사 프로필을 생성하거나 갱신합니다.
     *
     * @param riotAccount Riot 계정 정보
     * @param riotSummoner Riot 소환사 정보
     * @param syncedAt 동기화 시각
     */
    private void upsertSummonerProfile(
            RiotAccountPayload riotAccount,
            RiotSummonerPayload riotSummoner,
            LocalDateTime syncedAt) {
        SummonerProfile summonerProfile = summonerProfileRepository.findById(riotAccount.puuid())
                .orElseGet(() -> new SummonerProfile(riotAccount.puuid()));

        summonerProfile.updateProfile(
                riotAccount.gameName(),
                riotAccount.tagLine(),
                riotSummoner.summonerId(),
                riotSummoner.accountId(),
                riotSummoner.profileIconId(),
                riotSummoner.summonerLevel(),
                syncedAt);

        summonerProfileRepository.save(summonerProfile);
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
