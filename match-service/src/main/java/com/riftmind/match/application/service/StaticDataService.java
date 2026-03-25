package com.riftmind.match.application.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.riftmind.match.infrastructure.config.RiotApiProperties;

/**
 * Data Dragon 정적 데이터를 조회해 표시용 한글 이름을 제공합니다.
 *
 * @author 정수환
 * @since 2026-03-24
 */
@Service
public class StaticDataService {

    private static final Logger log = LoggerFactory.getLogger(StaticDataService.class);
    private static final String QUEUES_JSON_URL = "https://static.developer.riotgames.com/docs/lol/queues.json";

    private final RiotApiProperties riotApiProperties;
    private final RestClient dataDragonClient;
    private final RestClient queueDataClient;

    private volatile DataDragonVersions versions;
    private volatile Map<String, String> championNames = Map.of();
    private volatile Map<String, String> championAssetKeys = Map.of();
    private volatile Map<String, String> itemNames = Map.of();
    private volatile Map<String, String> summonerSpellNames = Map.of();
    private volatile Map<Integer, String> runeStyleNames = Map.of();
    private volatile Map<Integer, String> queueNames = Map.of();

    public StaticDataService(RestClient.Builder restClientBuilder, RiotApiProperties riotApiProperties) {
        this.riotApiProperties = riotApiProperties;
        this.dataDragonClient = restClientBuilder.clone()
                .baseUrl(riotApiProperties.getDataDragonBaseUrl())
                .build();
        this.queueDataClient = restClientBuilder.clone().build();
    }

    /**
     * 애플리케이션 기동 완료 후 정적 데이터를 미리 로드합니다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void preload() {
        try {
            ensureChampionNamesLoaded();
            ensureItemNamesLoaded();
            ensureSummonerSpellNamesLoaded();
            ensureRuneStyleNamesLoaded();
            ensureQueueNamesLoaded();
            log.info("Data Dragon static data preloaded successfully.");
        } catch (RuntimeException exception) {
            log.warn("Failed to preload Data Dragon static data: {}", exception.getMessage());
        }
    }

    /**
     * 챔피언 영문 식별값을 한글 이름으로 변환합니다.
     *
     * @param championName 챔피언 영문 식별값
     * @return 한글 챔피언 이름
     */
    public String getChampionNameKo(String championName) {
        if (!StringUtils.hasText(championName)) {
            return championName;
        }
        ensureChampionNamesLoaded();
        return championNames.getOrDefault(normalizeChampionIdentifier(championName), championName);
    }

    /**
     * 챔피언 영문 식별값을 Data Dragon 아이콘 키로 변환합니다.
     *
     * @param championName 챔피언 영문 식별값
     * @return Data Dragon 아이콘 키
     */
    public String getChampionAssetKey(String championName) {
        if (!StringUtils.hasText(championName)) {
            return championName;
        }
        ensureChampionNamesLoaded();
        return championAssetKeys.getOrDefault(normalizeChampionIdentifier(championName), championName);
    }

    /**
     * 포지션 값을 한글로 변환합니다.
     *
     * @param teamPosition 포지션 값
     * @return 한글 포지션
     */
    public String getTeamPositionKo(String teamPosition) {
        if (!StringUtils.hasText(teamPosition)) {
            return null;
        }

        return switch (teamPosition) {
            case "TOP" -> "탑";
            case "JUNGLE" -> "정글";
            case "MIDDLE" -> "미드";
            case "BOTTOM" -> "원딜";
            case "UTILITY" -> "서포터";
            default -> teamPosition;
        };
    }

    /**
     * 큐 ID를 한글 이름으로 변환합니다.
     *
     * @param queueId 큐 ID
     * @return 한글 큐 이름
     */
    public String getQueueNameKo(Integer queueId) {
        if (queueId == null) {
            return null;
        }

        ensureQueueNamesLoaded();
        return queueNames.getOrDefault(queueId, String.valueOf(queueId));
    }

    /**
     * 초 단위 게임 시간을 `n분 n초` 형식으로 변환합니다.
     *
     * @param gameDuration 게임 길이(초)
     * @return 포맷된 게임 시간
     */
    public String formatGameDuration(Integer gameDuration) {
        if (gameDuration == null || gameDuration < 0) {
            return null;
        }

        int minutes = gameDuration / 60;
        int seconds = gameDuration % 60;
        return minutes + "분 " + seconds + "초";
    }

    /**
     * 아이템 ID 목록을 한글 이름 목록으로 변환합니다.
     *
     * @param itemIds 아이템 ID 목록
     * @return 한글 아이템 이름 목록
     */
    public List<String> getItemNames(List<Integer> itemIds) {
        ensureItemNamesLoaded();
        return itemIds.stream()
                .map(this::getItemName)
                .filter(StringUtils::hasText)
                .toList();
    }

    /**
     * 소환사 주문 ID 두 개를 한글 이름 목록으로 변환합니다.
     *
     * @param summoner1Id 첫 번째 소환사 주문 ID
     * @param summoner2Id 두 번째 소환사 주문 ID
     * @return 한글 소환사 주문 이름 목록
     */
    public List<String> getSummonerSpellNames(Integer summoner1Id, Integer summoner2Id) {
        ensureSummonerSpellNamesLoaded();
        return List.of(summoner1Id, summoner2Id).stream()
                .map(this::getSummonerSpellName)
                .filter(StringUtils::hasText)
                .toList();
    }

    /**
     * 룬 스타일 ID를 한글 이름으로 변환합니다.
     *
     * @param runeStyleId 룬 스타일 ID
     * @return 한글 룬 스타일 이름
     */
    public String getRuneStyleName(Integer runeStyleId) {
        if (runeStyleId == null) {
            return null;
        }
        ensureRuneStyleNamesLoaded();
        return runeStyleNames.getOrDefault(runeStyleId, String.valueOf(runeStyleId));
    }

    /**
     * 챔피언 이름 캐시를 로드합니다.
     */
    private synchronized void ensureChampionNamesLoaded() {
        if (!championNames.isEmpty()) {
            return;
        }

        try {
            DataDragonVersions currentVersions = resolveVersions();
            JsonNode root = fetchJson(
                    "/cdn/{version}/data/{locale}/champion.json",
                    currentVersions.championVersion(),
                    currentVersions.locale());
            JsonNode champions = root.path("data");
            Map<String, String> loadedNames = new LinkedHashMap<>();
            Map<String, String> loadedAssetKeys = new LinkedHashMap<>();
            champions.fields().forEachRemaining(entry -> loadedNames.put(
                    normalizeChampionIdentifier(entry.getValue().path("id").asText(entry.getKey())),
                    entry.getValue().path("name").asText(entry.getKey())));
            champions.fields().forEachRemaining(entry -> loadedAssetKeys.put(
                    normalizeChampionIdentifier(entry.getValue().path("id").asText(entry.getKey())),
                    entry.getValue().path("id").asText(entry.getKey())));
            championNames = Map.copyOf(loadedNames);
            championAssetKeys = Map.copyOf(loadedAssetKeys);
        } catch (RuntimeException exception) {
            log.warn("Failed to load champion static data: {}", exception.getMessage());
            championNames = Map.of();
            championAssetKeys = Map.of();
        }
    }

    /**
     * 아이템 이름 캐시를 로드합니다.
     */
    private synchronized void ensureItemNamesLoaded() {
        if (!itemNames.isEmpty()) {
            return;
        }

        try {
            DataDragonVersions currentVersions = resolveVersions();
            JsonNode root = fetchJson(
                    "/cdn/{version}/data/{locale}/item.json",
                    currentVersions.itemVersion(),
                    currentVersions.locale());
            JsonNode items = root.path("data");
            Map<String, String> loadedNames = new LinkedHashMap<>();
            items.fields().forEachRemaining(entry -> loadedNames.put(
                    entry.getKey(),
                    entry.getValue().path("name").asText(entry.getKey())));
            itemNames = Map.copyOf(loadedNames);
        } catch (RuntimeException exception) {
            log.warn("Failed to load item static data: {}", exception.getMessage());
            itemNames = Map.of();
        }
    }

    /**
     * 소환사 주문 이름 캐시를 로드합니다.
     */
    private synchronized void ensureSummonerSpellNamesLoaded() {
        if (!summonerSpellNames.isEmpty()) {
            return;
        }

        try {
            DataDragonVersions currentVersions = resolveVersions();
            JsonNode root = fetchJson(
                    "/cdn/{version}/data/{locale}/summoner.json",
                    currentVersions.summonerVersion(),
                    currentVersions.locale());
            JsonNode summonerSpells = root.path("data");
            Map<String, String> loadedNames = new LinkedHashMap<>();
            summonerSpells.fields().forEachRemaining(entry -> loadedNames.put(
                    entry.getValue().path("key").asText(entry.getKey()),
                    entry.getValue().path("name").asText(entry.getKey())));
            summonerSpellNames = Map.copyOf(loadedNames);
        } catch (RuntimeException exception) {
            log.warn("Failed to load summoner spell static data: {}", exception.getMessage());
            summonerSpellNames = Map.of();
        }
    }

    /**
     * 룬 스타일 이름 캐시를 로드합니다.
     */
    private synchronized void ensureRuneStyleNamesLoaded() {
        if (!runeStyleNames.isEmpty()) {
            return;
        }

        try {
            DataDragonVersions currentVersions = resolveVersions();
            JsonNode root = fetchJson(
                    "/cdn/{version}/data/{locale}/runesReforged.json",
                    currentVersions.runeVersion(),
                    currentVersions.locale());
            Map<Integer, String> loadedNames = new LinkedHashMap<>();
            root.forEach(node -> loadedNames.put(
                    node.path("id").asInt(),
                    node.path("name").asText(String.valueOf(node.path("id").asInt()))));
            runeStyleNames = Map.copyOf(loadedNames);
        } catch (RuntimeException exception) {
            log.warn("Failed to load rune static data: {}", exception.getMessage());
            runeStyleNames = Map.of();
        }
    }

    /**
     * 큐 이름 캐시를 로드합니다.
     */
    private synchronized void ensureQueueNamesLoaded() {
        if (!queueNames.isEmpty()) {
            return;
        }

        try {
            JsonNode root = queueDataClient.get()
                    .uri(QUEUES_JSON_URL)
                    .retrieve()
                    .body(JsonNode.class);

            if (root == null || !root.isArray()) {
                throw new IllegalStateException("Queue static data response is invalid.");
            }

            Map<Integer, String> loadedNames = new LinkedHashMap<>();
            root.forEach(node -> {
                int queueId = node.path("queueId").asInt();
                String description = node.path("description").asText(null);
                loadedNames.put(queueId, toQueueNameKo(queueId, description));
            });
            queueNames = Map.copyOf(loadedNames);
        } catch (RuntimeException exception) {
            log.warn("Failed to load queue static data: {}", exception.getMessage());
            queueNames = Map.of();
        }
    }

    /**
     * 아이템 ID를 한글 이름으로 변환합니다.
     *
     * @param itemId 아이템 ID
     * @return 한글 아이템 이름
     */
    private String getItemName(Integer itemId) {
        if (itemId == null || itemId <= 0) {
            return null;
        }
        return itemNames.getOrDefault(String.valueOf(itemId), String.valueOf(itemId));
    }

    /**
     * 소환사 주문 ID를 한글 이름으로 변환합니다.
     *
     * @param summonerSpellId 소환사 주문 ID
     * @return 한글 소환사 주문 이름
     */
    private String getSummonerSpellName(Integer summonerSpellId) {
        if (summonerSpellId == null || summonerSpellId <= 0) {
            return null;
        }
        return summonerSpellNames.getOrDefault(String.valueOf(summonerSpellId), String.valueOf(summonerSpellId));
    }

    /**
     * 챔피언 식별값을 정규화합니다.
     *
     * @param championName 챔피언 영문 이름
     * @return 정규화된 키
     */
    private String normalizeChampionIdentifier(String championName) {
        return championName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    /**
     * queueId와 설명을 한글 큐 이름으로 변환합니다.
     *
     * @param queueId 큐 ID
     * @param description Riot 설명
     * @return 한글 큐 이름
     */
    private String toQueueNameKo(int queueId, String description) {
        return switch (queueId) {
            case 400 -> "일반 드래프트";
            case 420 -> "솔로 랭크";
            case 430 -> "일반 게임";
            case 440 -> "자유 랭크";
            case 450 -> "칼바람 나락";
            case 480 -> "신속 대전";
            case 490 -> "빠른 대전";
            case 700 -> "소환사의 협곡 클래시";
            case 720 -> "칼바람 나락 클래시";
            case 830, 870 -> "입문 봇";
            case 840, 880 -> "초급 봇";
            case 850, 890 -> "중급 봇";
            case 900 -> "무작위 우르프";
            case 1020 -> "원포올";
            case 1090 -> "전략적 팀 전투";
            case 1100 -> "전략적 팀 전투 랭크";
            case 1110 -> "전략적 팀 전투 튜토리얼";
            case 1300 -> "넥서스 돌격";
            case 1400 -> "궁극기 주문서";
            case 1700, 1710 -> "아레나";
            case 1810, 1820, 1830, 1840 -> "무리";
            case 1900 -> "우르프";
            case 2300 -> "난투";
            case 2400 -> "칼바람 아수라장";
            default -> StringUtils.hasText(description) ? description : String.valueOf(queueId);
        };
    }

    /**
     * Data Dragon 버전 정보를 조회합니다.
     *
     * @return Data Dragon 버전 정보
     */
    private DataDragonVersions resolveVersions() {
        if (versions != null) {
            return versions;
        }

        synchronized (this) {
            if (versions != null) {
                return versions;
            }

            JsonNode realm = fetchJson("/realms/{realm}.json", riotApiProperties.getDataDragonRealm());
            JsonNode versionNode = realm.path("n");
            String cdnVersion = realm.path("v").asText(versionNode.path("champion").asText());
            String locale = realm.path("l").asText(riotApiProperties.getDataDragonLocale());

            versions = new DataDragonVersions(
                    versionNode.path("champion").asText(),
                    versionNode.path("item").asText(),
                    versionNode.path("summoner").asText(),
                    cdnVersion,
                    StringUtils.hasText(riotApiProperties.getDataDragonLocale())
                            ? riotApiProperties.getDataDragonLocale()
                            : locale);
            return versions;
        }
    }

    /**
     * Data Dragon JSON 응답을 조회합니다.
     *
     * @param path 요청 경로
     * @param uriVariables URI 변수
     * @return JSON 응답
     */
    private JsonNode fetchJson(String path, Object... uriVariables) {
        JsonNode response = dataDragonClient.get()
                .uri(path, uriVariables)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException("Static data response is empty: " + path);
        }

        return response;
    }

    /**
     * Data Dragon 버전 정보를 담는 내부 DTO입니다.
     *
     * @param championVersion 챔피언 데이터 버전
     * @param itemVersion 아이템 데이터 버전
     * @param summonerVersion 소환사 주문 데이터 버전
     * @param runeVersion 룬 데이터 버전
     * @param locale locale 값
     */
    private record DataDragonVersions(
            String championVersion,
            String itemVersion,
            String summonerVersion,
            String runeVersion,
            String locale) {

        private DataDragonVersions {
            Objects.requireNonNull(championVersion);
            Objects.requireNonNull(itemVersion);
            Objects.requireNonNull(summonerVersion);
            Objects.requireNonNull(runeVersion);
            Objects.requireNonNull(locale);
        }
    }
}
