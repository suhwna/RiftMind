package com.riftmind.ai.application.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.riftmind.ai.global.exception.ApiErrorCode;
import com.riftmind.ai.global.exception.ApiException;
import com.riftmind.ai.infrastructure.match.MatchDetailResponse;
import com.riftmind.ai.infrastructure.match.MatchParticipantResponse;
import com.riftmind.ai.infrastructure.match.MatchServiceClient;
import com.riftmind.ai.infrastructure.openai.OpenAiReviewClient;

/**
 * match-service 경기 상세 데이터를 AI 회고 입력으로 변환합니다.
 *
 * @author 정수환
 * @since 2026-04-14
 */
@Service
public class MatchReviewService {

    private final MatchServiceClient matchServiceClient;
    private final OpenAiReviewClient openAiReviewClient;
    private final ObjectMapper objectMapper;

    public MatchReviewService(
            MatchServiceClient matchServiceClient,
            OpenAiReviewClient openAiReviewClient,
            ObjectMapper objectMapper) {
        this.matchServiceClient = matchServiceClient;
        this.openAiReviewClient = openAiReviewClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 특정 플레이어 기준 경기 회고를 생성합니다.
     *
     * @param matchId Riot matchId
     * @param puuid 회고 대상 PUUID
     * @return 구조화된 AI 회고 결과
     */
    public MatchReviewResult generateReview(String matchId, String puuid) {
        MatchDetailResponse matchDetail = matchServiceClient.getMatchDetail(matchId, puuid);
        MatchParticipantResponse focusParticipant = matchDetail.participants().stream()
                .filter(participant -> participant.puuid().equals(puuid))
                .findFirst()
                .orElseThrow(() -> new ApiException(
                        ApiErrorCode.INVALID_REQUEST,
                        "Focus participant not found for puuid: " + puuid));
        MatchParticipantResponse laneOpponent = resolveLaneOpponent(matchDetail.participants(), focusParticipant);

        try {
            return openAiReviewClient.generateReview(objectMapper.writeValueAsString(buildPromptPayload(
                    matchDetail,
                    focusParticipant,
                    laneOpponent)));
        } catch (JsonProcessingException exception) {
            throw new ApiException(ApiErrorCode.OPENAI_API_ERROR, "Failed to serialize match review payload.");
        }
    }

    private Map<String, Object> buildPromptPayload(
            MatchDetailResponse matchDetail,
            MatchParticipantResponse focusParticipant,
            MatchParticipantResponse laneOpponent) {
        Map<String, Object> payload = new LinkedHashMap<>();
        Map<String, Object> matchPayload = new LinkedHashMap<>();
        matchPayload.put("matchId", matchDetail.matchId());
        matchPayload.put("queueNameKo", matchDetail.queueNameKo());
        matchPayload.put("gameDurationText", matchDetail.gameDurationText());
        matchPayload.put("gameVersion", matchDetail.gameVersion());
        matchPayload.put("existingStrengths", safeList(matchDetail.focusStrengths()));
        matchPayload.put("existingWeaknesses", safeList(matchDetail.focusWeaknesses()));
        payload.put("match", matchPayload);
        payload.put("player", buildParticipantPayload(focusParticipant));
        payload.put("laneOpponent", laneOpponent == null ? null : buildParticipantPayload(laneOpponent));
        return payload;
    }

    private Map<String, Object> buildParticipantPayload(MatchParticipantResponse participant) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("summonerName", participant.summonerName());
        payload.put("championName", participant.championName());
        payload.put("championNameKo", participant.championNameKo());
        payload.put("teamPosition", participant.teamPosition());
        payload.put("teamPositionKo", participant.teamPositionKo());
        payload.put("win", participant.win());
        payload.put("kda", calculateKda(participant));
        payload.put("kills", participant.kills());
        payload.put("deaths", participant.deaths());
        payload.put("assists", participant.assists());
        payload.put("damageToChampions", participant.totalDamageDealtToChampions());
        payload.put("damageTaken", participant.totalDamageTaken());
        payload.put("goldEarned", participant.goldEarned());
        payload.put("totalCs", participant.totalMinionsKilled() + participant.neutralMinionsKilled());
        payload.put("visionScore", participant.visionScore());
        payload.put("wardsPlaced", participant.wardsPlaced());
        payload.put("wardsKilled", participant.wardsKilled());
        payload.put("champLevel", participant.champLevel());
        payload.put("interpretationTags", safeList(participant.interpretationTags()));
        payload.put("items", safeList(participant.itemNames()));
        payload.put("summonerSpells", safeList(participant.summonerSpellNames()));
        payload.put("runes", java.util.stream.Stream.of(participant.primaryRuneName(), participant.secondaryRuneName())
                .filter(rune -> rune != null && !rune.isBlank())
                .toList());
        payload.put("multiKills", Map.of(
                "doubleKills", participant.doubleKills(),
                "tripleKills", participant.tripleKills(),
                "quadraKills", participant.quadraKills(),
                "pentaKills", participant.pentaKills(),
                "largestKillingSpree", participant.largestKillingSpree()));
        payload.put("objectiveContribution", Map.of(
                "firstBloodKill", participant.firstBloodKill(),
                "firstBloodAssist", participant.firstBloodAssist(),
                "firstTowerKill", participant.firstTowerKill(),
                "firstTowerAssist", participant.firstTowerAssist(),
                "turretKills", participant.turretKills(),
                "inhibitorKills", participant.inhibitorKills(),
                "damageToObjectives", participant.damageDealtToObjectives(),
                "damageToTurrets", participant.damageDealtToTurrets(),
                "objectivesStolen", participant.objectivesStolen(),
                "objectivesStolenAssists", participant.objectivesStolenAssists()));
        payload.put("supportContribution", Map.of(
                "totalHeal", participant.totalHeal(),
                "healsOnTeammates", participant.totalHealsOnTeammates(),
                "shieldedOnTeammates", participant.totalDamageShieldedOnTeammates()));
        return payload;
    }

    private MatchParticipantResponse resolveLaneOpponent(
            List<MatchParticipantResponse> participants,
            MatchParticipantResponse focusParticipant) {
        if (focusParticipant.teamPosition() == null || focusParticipant.teamPosition().isBlank()) {
            return null;
        }
        if (focusParticipant.teamId() == null) {
            return null;
        }

        return participants.stream()
                .filter(participant -> participant.teamId() != null)
                .filter(participant -> !participant.teamId().equals(focusParticipant.teamId()))
                .filter(participant -> focusParticipant.teamPosition().equals(participant.teamPosition()))
                .findFirst()
                .orElse(null);
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }

    private double calculateKda(MatchParticipantResponse participant) {
        if (participant.deaths() == 0) {
            return participant.kills() + participant.assists();
        }
        return (double) (participant.kills() + participant.assists()) / participant.deaths();
    }
}
