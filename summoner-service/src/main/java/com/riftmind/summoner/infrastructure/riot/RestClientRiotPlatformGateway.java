package com.riftmind.summoner.infrastructure.riot;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.riftmind.summoner.global.exception.RiotApiException;
import com.riftmind.summoner.infrastructure.config.RiotApiProperties;

/**
 * RestClient 기반 Riot 플랫폼 API 구현체입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Component
public class RestClientRiotPlatformGateway implements RiotPlatformGateway {

    private static final String RIOT_TOKEN_HEADER = "X-Riot-Token";
    private static final Set<String> VALID_TEAM_POSITIONS = Set.of(
            "TOP",
            "JUNGLE",
            "MIDDLE",
            "BOTTOM",
            "UTILITY");

    private final RiotApiProperties riotApiProperties;
    private final RestClient accountClient;
    private final RestClient summonerClient;
    private final RestClient matchClient;

    public RestClientRiotPlatformGateway(RestClient.Builder restClientBuilder, RiotApiProperties riotApiProperties) {
        this.riotApiProperties = riotApiProperties;
        this.accountClient = createClient(restClientBuilder, riotApiProperties.getAccountBaseUrl());
        this.summonerClient = createClient(restClientBuilder, riotApiProperties.getSummonerBaseUrl());
        this.matchClient = createClient(restClientBuilder, riotApiProperties.getMatchBaseUrl());
    }

    /**
     * Riot ID로 계정 정보를 조회합니다.
     *
     * @param gameName 라이엇 게임 이름
     * @param tagLine 라이엇 태그 라인
     * @return Riot 계정 정보
     */
    @Override
    @Cacheable(cacheNames = "riotAccountByRiotId", key = "#gameName + ':' + #tagLine")
    public RiotAccountPayload fetchAccountByRiotId(String gameName, String tagLine) {
        AccountResponse response = getForObject(
                accountClient,
                "/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}",
                AccountResponse.class,
                gameName,
                tagLine);

        return new RiotAccountPayload(response.puuid(), response.gameName(), response.tagLine());
    }

    /**
     * PUUID로 소환사 정보를 조회합니다.
     *
     * @param puuid Riot PUUID
     * @return Riot 소환사 정보
     */
    @Override
    @Cacheable(cacheNames = "riotSummonerByPuuid", key = "#puuid")
    public RiotSummonerPayload fetchSummonerByPuuid(String puuid) {
        SummonerResponse response = getForObject(
                summonerClient,
                "/lol/summoner/v4/summoners/by-puuid/{puuid}",
                SummonerResponse.class,
                puuid);

        return new RiotSummonerPayload(
                response.id(),
                response.accountId(),
                response.profileIconId(),
                response.summonerLevel());
    }

    /**
     * PUUID 기준 최근 matchId 목록을 조회합니다.
     *
     * @param puuid Riot PUUID
     * @param count 조회할 경기 수
     * @return matchId 목록
     */
    @Override
    @Cacheable(cacheNames = "riotMatchIdsByPuuid", key = "#puuid + ':' + #count")
    public List<String> fetchMatchIdsByPuuid(String puuid, int count) {
        String[] response = getForObject(
                matchClient,
                "/lol/match/v5/matches/by-puuid/{puuid}/ids?start=0&count={count}",
                String[].class,
                puuid,
                count);

        return response == null ? List.of() : List.of(response);
    }

    /**
     * matchId 기준 매치 상세 정보를 조회합니다.
     *
     * @param matchId Riot matchId
     * @return Riot 매치 상세 정보
     */
    @Override
    @Cacheable(cacheNames = "riotMatchById", key = "#matchId")
    public RiotMatchPayload fetchMatchById(String matchId) {
        MatchResponse response = getForObject(
                matchClient,
                "/lol/match/v5/matches/{matchId}",
                MatchResponse.class,
                matchId);

        if (response.metadata() == null || response.info() == null) {
            throw new RiotApiException("Riot match response is missing metadata or info.");
        }

        List<RiotMatchParticipantPayload> participants = response.info().participants() == null
                ? List.of()
                : response.info().participants().stream()
                        .map(this::toParticipantPayload)
                        .toList();

        return new RiotMatchPayload(
                response.metadata().matchId(),
                toLocalDateTime(response.info().gameCreation()),
                response.info().gameDuration(),
                response.info().queueId(),
                response.info().gameMode(),
                response.info().gameVersion(),
                participants);
    }

    /**
     * 기본 설정이 반영된 RestClient를 생성합니다.
     *
     * @param restClientBuilder RestClient 빌더
     * @param baseUrl API 기본 URL
     * @return 설정된 RestClient
     */
    private RestClient createClient(RestClient.Builder restClientBuilder, String baseUrl) {
        return restClientBuilder.clone()
                .baseUrl(baseUrl)
                .defaultHeader(RIOT_TOKEN_HEADER, riotApiProperties.getApiKey())
                .build();
    }

    /**
     * GET 요청을 실행하고 응답 객체를 반환합니다.
     *
     * @param client RestClient
     * @param path 요청 경로
     * @param responseType 응답 타입
     * @param uriVariables URI 변수
     * @param <T> 응답 객체 타입
     * @return 응답 객체
     */
    private <T> T getForObject(RestClient client, String path, Class<T> responseType, Object... uriVariables) {
        validateApiKey();

        try {
            T response = client.get()
                    .uri(path, uriVariables)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, httpResponse) -> {
                        throw new RiotApiException(
                                "Riot API request failed with status " + httpResponse.getStatusCode().value());
                    })
                    .body(responseType);

            if (response == null) {
                throw new RiotApiException("Riot API returned an empty response.");
            }

            return response;
        } catch (RiotApiException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new RiotApiException("Failed to call Riot API: " + exception.getMessage());
        }
    }

    /**
     * Riot API 키가 설정되어 있는지 검증합니다.
     */
    private void validateApiKey() {
        if (!StringUtils.hasText(riotApiProperties.getApiKey())) {
            throw new RiotApiException("RIOT_API_KEY is not configured.");
        }
    }

    /**
     * Riot 참가자 응답을 내부 참가자 DTO로 변환합니다.
     *
     * @param participant Riot 참가자 응답
     * @return 내부 참가자 DTO
     */
    private RiotMatchParticipantPayload toParticipantPayload(MatchParticipantResponse participant) {
        return new RiotMatchParticipantPayload(
                participant.puuid(),
                resolveParticipantRiotId(
                        participant.riotIdGameName(),
                        participant.riotIdTagline(),
                        participant.summonerName()),
                participant.championName(),
                resolveTeamPosition(participant.teamPosition(), participant.individualPosition()),
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
                extractPrimaryRune(participant.perks()),
                extractSecondaryRune(participant.perks()),
                participant.totalDamageTaken());
    }

    /**
     * 참가자 Riot ID를 구성하고, 값이 없으면 기존 소환사 이름으로 대체합니다.
     *
     * @param riotIdGameName Riot 게임 이름
     * @param riotIdTagline Riot 태그 라인
     * @param summonerName 기존 소환사 이름
     * @return 표시용 참가자 식별자
     */
    private String resolveParticipantRiotId(String riotIdGameName, String riotIdTagline, String summonerName) {
        if (StringUtils.hasText(riotIdGameName) && StringUtils.hasText(riotIdTagline)) {
            return riotIdGameName + "#" + riotIdTagline;
        }
        return summonerName;
    }

    /**
     * 팀 포지션을 우선 사용하고, 비어 있으면 개인 포지션으로 대체합니다.
     *
     * @param teamPosition 팀 포지션
     * @param individualPosition 개인 포지션
     * @return 저장할 포지션 값
     */
    private String resolveTeamPosition(String teamPosition, String individualPosition) {
        if (isValidTeamPosition(teamPosition)) {
            return teamPosition;
        }
        if (isValidTeamPosition(individualPosition)) {
            return individualPosition;
        }
        return null;
    }

    /**
     * 저장 가능한 포지션 값인지 검증합니다.
     *
     * @param position Riot 응답 포지션
     * @return 저장 가능 여부
     */
    private boolean isValidTeamPosition(String position) {
        return StringUtils.hasText(position) && VALID_TEAM_POSITIONS.contains(position);
    }

    /**
     * epoch milliseconds를 LocalDateTime으로 변환합니다.
     *
     * @param epochMillis epoch milliseconds 값
     * @return 변환된 LocalDateTime
     */
    private LocalDateTime toLocalDateTime(Long epochMillis) {
        if (epochMillis == null) {
            throw new RiotApiException("Riot match response is missing gameCreation.");
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }

    /**
     * 참가자 룬 정보에서 주 룬 스타일 ID를 추출합니다.
     *
     * @param perks 참가자 룬 정보
     * @return 주 룬 스타일 ID
     */
    private Integer extractPrimaryRune(PerksResponse perks) {
        return extractRuneStyleId(perks, "primaryStyle");
    }

    /**
     * 참가자 룬 정보에서 보조 룬 스타일 ID를 추출합니다.
     *
     * @param perks 참가자 룬 정보
     * @return 보조 룬 스타일 ID
     */
    private Integer extractSecondaryRune(PerksResponse perks) {
        return extractRuneStyleId(perks, "subStyle");
    }

    /**
     * 참가자 룬 정보에서 지정한 설명에 해당하는 룬 스타일 ID를 추출합니다.
     *
     * @param perks 참가자 룬 정보
     * @param description 룬 설명 키
     * @return 룬 스타일 ID
     */
    private Integer extractRuneStyleId(PerksResponse perks, String description) {
        if (perks == null || perks.styles() == null) {
            return null;
        }

        return perks.styles().stream()
                .filter(style -> description.equals(style.description()))
                .map(StyleResponse::style)
                .findFirst()
                .orElse(null);
    }

    private record AccountResponse(
            String puuid,
            String gameName,
            String tagLine) {
    }

    private record SummonerResponse(
            String id,
            String accountId,
            Integer profileIconId,
            Long summonerLevel) {
    }

    private record MatchResponse(
            MatchMetadata metadata,
            MatchInfo info) {
    }

    private record MatchMetadata(
            String matchId) {
    }

    private record MatchInfo(
            Long gameCreation,
            Integer gameDuration,
            Integer queueId,
            String gameMode,
            String gameVersion,
            List<MatchParticipantResponse> participants) {
    }

    private record MatchParticipantResponse(
            String puuid,
            String riotIdGameName,
            String riotIdTagline,
            String summonerName,
            String championName,
            String teamPosition,
            String individualPosition,
            int kills,
            int deaths,
            int assists,
            boolean win,
            int totalDamageDealtToChampions,
            int goldEarned,
            int totalMinionsKilled,
            int neutralMinionsKilled,
            int visionScore,
            int wardsPlaced,
            int wardsKilled,
            int champLevel,
            int item0,
            int item1,
            int item2,
            int item3,
            int item4,
            int item5,
            int item6,
            int summoner1Id,
            int summoner2Id,
            int totalDamageTaken,
            PerksResponse perks) {
    }

    private record PerksResponse(
            List<StyleResponse> styles) {
    }

    private record StyleResponse(
            String description,
            Integer style) {
    }
}
