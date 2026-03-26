package com.riftmind.match.application.dto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.riftmind.match.domain.match.MatchParticipant;

/**
 * 같은 경기 참가자 상대 비교를 기준으로 경기 해석 태그를 계산합니다.
 *
 * @author 정수환
 * @since 2026-03-26
 */
final class MatchInterpretationTagResolver {

    private MatchInterpretationTagResolver() {
    }

    /**
     * 참가자와 같은 경기 참가자 목록을 기준으로 최대 2개의 해석 태그를 계산합니다.
     *
     * @param participant 기준 참가자
     * @return 경기 해석 태그 목록
     */
    static List<String> resolve(MatchParticipant participant) {
        List<MatchParticipant> participants = participant.getMatchSummary().getParticipants();
        List<String> tags = new ArrayList<>();

        int damageRank = rankDescending(participants, MatchParticipant::getTotalDamageDealtToChampions, participant);
        int visionRank = rankDescending(participants, MatchParticipant::getVisionScore, participant);
        int goldRank = rankDescending(participants, MatchParticipant::getGoldEarned, participant);
        int csRank = rankDescending(participants, MatchInterpretationTagResolver::totalCs, participant);
        int assistsRank = rankDescending(participants, MatchParticipant::getAssists, participant);
        int deathsRank = rankDescending(participants, MatchParticipant::getDeaths, participant);
        int lowDeathRank = rankAscending(participants, MatchParticipant::getDeaths, participant);
        double kda = calculateKda(participant);

        if (participant.isWin() && damageRank <= 2 && kda >= 3.5) {
            tags.add("캐리");
        } else if (!participant.isWin() && deathsRank <= 2 && kda < 2.0) {
            tags.add("고전");
        }

        if (tags.size() < 2 && isVisionContribution(participant, visionRank)) {
            tags.add("시야 기여");
        }

        if (tags.size() < 2 && isGrowthEdge(participant, csRank, goldRank)) {
            tags.add("성장 우위");
        }

        if (tags.size() < 2 && (damageRank <= 3 || assistsRank <= 2)) {
            tags.add("교전 기여");
        }

        if (tags.size() < 2 && lowDeathRank <= 2 && kda >= 3.0) {
            tags.add("안정적");
        }

        return tags.stream().distinct().limit(2).toList();
    }

    private static boolean isVisionContribution(MatchParticipant participant, int visionRank) {
        if ("UTILITY".equals(participant.getTeamPosition())) {
            return visionRank <= 2;
        }

        if ("JUNGLE".equals(participant.getTeamPosition())) {
            return visionRank <= 3;
        }

        return visionRank <= 2 && participant.getVisionScore() >= 20;
    }

    private static boolean isGrowthEdge(MatchParticipant participant, int csRank, int goldRank) {
        if ("UTILITY".equals(participant.getTeamPosition())) {
            return goldRank <= 3;
        }

        if ("JUNGLE".equals(participant.getTeamPosition())) {
            return csRank <= 3 || goldRank <= 3;
        }

        return csRank <= 3 || goldRank <= 3;
    }

    private static int rankDescending(
            List<MatchParticipant> participants,
            java.util.function.ToIntFunction<MatchParticipant> extractor,
            MatchParticipant target
    ) {
        return rank(participants, extractor, target, Comparator.reverseOrder());
    }

    private static int rankAscending(
            List<MatchParticipant> participants,
            java.util.function.ToIntFunction<MatchParticipant> extractor,
            MatchParticipant target
    ) {
        return rank(participants, extractor, target, Comparator.naturalOrder());
    }

    private static int rank(
            List<MatchParticipant> participants,
            java.util.function.ToIntFunction<MatchParticipant> extractor,
            MatchParticipant target,
            Comparator<Integer> comparator
    ) {
        List<MatchParticipant> sorted = participants.stream()
                .sorted((left, right) -> comparator.compare(extractor.applyAsInt(left), extractor.applyAsInt(right)))
                .toList();

        for (int index = 0; index < sorted.size(); index++) {
            if (sorted.get(index).getId().equals(target.getId())) {
                return index + 1;
            }
        }

        return sorted.size();
    }

    private static int totalCs(MatchParticipant participant) {
        return participant.getTotalMinionsKilled() + participant.getNeutralMinionsKilled();
    }

    private static double calculateKda(MatchParticipant participant) {
        if (participant.getDeaths() == 0) {
            return participant.getKills() + participant.getAssists();
        }
        return (double) (participant.getKills() + participant.getAssists()) / participant.getDeaths();
    }
}
