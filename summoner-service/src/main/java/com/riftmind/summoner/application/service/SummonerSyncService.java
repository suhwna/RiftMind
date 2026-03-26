package com.riftmind.summoner.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.riftmind.summoner.application.dto.SummonerSyncResult;
import com.riftmind.summoner.domain.summoner.SummonerProfile;
import com.riftmind.summoner.domain.sync.SyncHistory;
import com.riftmind.summoner.global.exception.RiotApiException;
import com.riftmind.summoner.infrastructure.config.RiotApiProperties;
import com.riftmind.summoner.infrastructure.match.MatchServiceClient;
import com.riftmind.summoner.infrastructure.persistence.repository.SummonerProfileRepository;
import com.riftmind.summoner.infrastructure.persistence.repository.SyncHistoryRepository;
import com.riftmind.summoner.infrastructure.riot.RiotAccountPayload;
import com.riftmind.summoner.infrastructure.riot.RiotPlatformGateway;
import com.riftmind.summoner.infrastructure.riot.RiotSummonerPayload;
import com.riftmind.summoner.infrastructure.search.SearchServiceClient;

/**
 * Riot API 호출과 내부 저장을 조합해 소환사 동기화를 수행합니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Service
public class SummonerSyncService {

    private final RiotPlatformGateway riotPlatformGateway;
    private final MatchServiceClient matchServiceClient;
    private final SearchServiceClient searchServiceClient;
    private final RiotApiProperties riotApiProperties;
    private final SummonerProfileRepository summonerProfileRepository;
    private final SyncHistoryRepository syncHistoryRepository;

    public SummonerSyncService(
            RiotPlatformGateway riotPlatformGateway,
            MatchServiceClient matchServiceClient,
            SearchServiceClient searchServiceClient,
            RiotApiProperties riotApiProperties,
            SummonerProfileRepository summonerProfileRepository,
            SyncHistoryRepository syncHistoryRepository) {
        this.riotPlatformGateway = riotPlatformGateway;
        this.matchServiceClient = matchServiceClient;
        this.searchServiceClient = searchServiceClient;
        this.riotApiProperties = riotApiProperties;
        this.summonerProfileRepository = summonerProfileRepository;
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

            LocalDateTime syncedAt = LocalDateTime.now();
            upsertSummonerProfile(riotAccount, riotSummoner, syncedAt);

            MatchServiceClient.MatchSyncResult matchSyncResult =
                    matchServiceClient.syncMatches(puuid, normalizedCount);
            searchServiceClient.indexRecentMatches(puuid, normalizedCount);
            int savedMatchCount = matchSyncResult.savedMatchCount();

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
}
