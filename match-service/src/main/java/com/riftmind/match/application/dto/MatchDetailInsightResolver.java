package com.riftmind.match.application.dto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.riftmind.match.domain.match.MatchParticipant;
import com.riftmind.match.domain.match.MatchSummary;

/**
 * 특정 참가자 기준으로 매치 상세 인사이트 문장을 생성합니다.
 *
 * @author 정수환
 * @since 2026-03-27
 */
final class MatchDetailInsightResolver {

    private MatchDetailInsightResolver() {
    }

    /**
     * 포커스 참가자 기준으로 매치 인사이트를 계산합니다.
     *
     * @param matchSummary 매치 엔티티
     * @param focusPuuid 포커스 참가자 PUUID
     * @return 매치 인사이트 목록
     */
    static MatchDetailInsightSummary resolve(MatchSummary matchSummary, String focusPuuid) {
        if (focusPuuid == null || focusPuuid.isBlank()) {
            return new MatchDetailInsightSummary(List.of(), List.of());
        }

        return matchSummary.getParticipants().stream()
                .filter(participant -> focusPuuid.equals(participant.getPuuid()))
                .findFirst()
                .map(MatchDetailInsightResolver::buildInsights)
                .orElse(new MatchDetailInsightSummary(List.of(), List.of()));
    }

    private static MatchDetailInsightSummary buildInsights(MatchParticipant participant) {
        List<MatchParticipant> participants = participant.getMatchSummary().getParticipants();
        List<MatchParticipant> teammates = participants.stream()
                .filter(current -> current.getTeamId() != null && current.getTeamId().equals(participant.getTeamId()))
                .toList();
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();

        int damageRank = rankDescending(participants, MatchParticipant::getTotalDamageDealtToChampions, participant);
        int visionRank = rankDescending(participants, MatchParticipant::getVisionScore, participant);
        int goldRank = rankDescending(participants, MatchParticipant::getGoldEarned, participant);
        int csRank = rankDescending(participants, MatchDetailInsightResolver::totalCs, participant);
        int assistsRank = rankDescending(participants, MatchParticipant::getAssists, participant);
        int lowDeathRank = rankAscending(participants, MatchParticipant::getDeaths, participant);
        int highDeathRank = rankDescending(participants, MatchParticipant::getDeaths, participant);
        int teamDamageRank = rankDescending(teammates, MatchParticipant::getTotalDamageDealtToChampions, participant);
        int teamVisionRank = rankDescending(teammates, MatchParticipant::getVisionScore, participant);
        int teamCsRank = rankDescending(teammates, MatchDetailInsightResolver::totalCs, participant);
        int teamObjectiveDamageRank = rankDescending(teammates, MatchParticipant::getDamageDealtToObjectives, participant);
        int teamTurretDamageRank = rankDescending(teammates, MatchParticipant::getDamageDealtToTurrets, participant);
        int teamShieldRank = rankDescending(teammates, MatchParticipant::getTotalDamageShieldedOnTeammates, participant);
        int teamHealRank = rankDescending(teammates, MatchParticipant::getTotalHealsOnTeammates, participant);
        int teamDamageTakenRank = rankDescending(teammates, MatchParticipant::getTotalDamageTaken, participant);
        double kda = calculateKda(participant);
        int totalCs = totalCs(participant);

        addPositionStrengths(
                strengths,
                participant,
                damageRank,
                visionRank,
                goldRank,
                csRank,
                assistsRank,
                lowDeathRank,
                teamDamageRank,
                teamVisionRank,
                teamCsRank,
                teamObjectiveDamageRank,
                teamTurretDamageRank,
                teamShieldRank,
                teamHealRank,
                teamDamageTakenRank,
                kda,
                totalCs);
        addWeaknesses(
                weaknesses,
                participant,
                damageRank,
                visionRank,
                goldRank,
                csRank,
                highDeathRank,
                kda,
                totalCs,
                teamObjectiveDamageRank);

        if (strengths.size() < 3 && participant.isWin() && damageRank <= 2 && kda >= 3.5) {
            strengths.add("딜량 " + formatNumber(participant.getTotalDamageDealtToChampions())
                    + "로 10명 중 " + damageRank + "위, KDA " + formatDecimal(kda)
                    + "를 기록해 승리에 직접 기여했습니다.");
        }

        if (strengths.size() < 3 && damageRank <= 3) {
            strengths.add("챔피언 피해량 " + formatNumber(participant.getTotalDamageDealtToChampions())
                    + "로 10명 중 " + damageRank + "위, 팀 내 " + teamDamageRank + "위였습니다.");
        }

        if (strengths.size() < 3 && lowDeathRank <= 2 && kda >= 3.0) {
            strengths.add("데스 " + formatNumber(participant.getDeaths()) + "회로 가장 적은 축에 속했고, KDA "
                    + formatDecimal(kda) + "로 안정적으로 마무리했습니다.");
        }

        if (strengths.size() < 3 && isVisionStrength(participant, visionRank)) {
            strengths.add("시야 점수 " + formatNumber(participant.getVisionScore()) + ", 와드 설치 "
                    + formatNumber(participant.getWardsPlaced()) + "회로 팀 내 " + teamVisionRank + "위였습니다.");
        }

        if (strengths.size() < 3 && isGrowthStrength(participant, csRank, goldRank)) {
            strengths.add("총 CS " + formatNumber(totalCs) + ", 골드 " + formatNumber(participant.getGoldEarned())
                    + "로 각각 10명 중 " + csRank + "위, " + goldRank + "위였습니다.");
        }

        if (strengths.size() < 3 && assistsRank <= 2) {
            strengths.add("어시스트 " + formatNumber(participant.getAssists()) + "회로 10명 중 " + assistsRank
                    + "위였고, 교전 관여도가 높았습니다.");
        }

        if (strengths.size() < 3 && teamCsRank <= 2) {
            strengths.add("팀 내 성장 속도도 빨랐습니다. 총 CS " + formatNumber(totalCs)
                    + "로 팀 내 " + teamCsRank + "위였습니다.");
        }

        if (weaknesses.isEmpty() && !participant.isWin() && highDeathRank <= 3) {
            weaknesses.add("데스 " + formatNumber(participant.getDeaths()) + "회로 10명 중 " + highDeathRank
                    + "위였고, 교전 손실이 큰 경기였습니다.");
        }

        return new MatchDetailInsightSummary(
                strengths.stream().distinct().limit(3).toList(),
                weaknesses.stream().distinct().limit(2).toList());
    }

    private static void addPositionStrengths(
            List<String> strengths,
            MatchParticipant participant,
            int damageRank,
            int visionRank,
            int goldRank,
            int csRank,
            int assistsRank,
            int lowDeathRank,
            int teamDamageRank,
            int teamVisionRank,
            int teamCsRank,
            int teamObjectiveDamageRank,
            int teamTurretDamageRank,
            int teamShieldRank,
            int teamHealRank,
            int teamDamageTakenRank,
            double kda,
            int totalCs
    ) {
        if ("UTILITY".equals(participant.getTeamPosition())) {
            addSupportInsight(strengths, participant, teamShieldRank, teamHealRank);
            if (strengths.size() < 3 && isVisionStrength(participant, visionRank)) {
                strengths.add("시야 점수 " + formatNumber(participant.getVisionScore()) + ", 와드 설치 "
                        + formatNumber(participant.getWardsPlaced()) + "회로 팀 내 " + teamVisionRank + "위였습니다.");
            }
            if (strengths.size() < 3 && assistsRank <= 2) {
                strengths.add("어시스트 " + formatNumber(participant.getAssists()) + "회로 팀 교전에 꾸준히 관여했습니다.");
            }
        } else if ("JUNGLE".equals(participant.getTeamPosition())) {
            addObjectiveInsight(strengths, participant, teamObjectiveDamageRank, teamTurretDamageRank);
            if (strengths.size() < 3 && isVisionStrength(participant, visionRank)) {
                strengths.add("정글러 기준 시야 점수 " + formatNumber(participant.getVisionScore())
                        + "로 10명 중 " + visionRank + "위였습니다.");
            }
            if (strengths.size() < 3 && isGrowthStrength(participant, csRank, goldRank)) {
                strengths.add("총 CS " + formatNumber(totalCs) + ", 골드 " + formatNumber(participant.getGoldEarned())
                        + "로 성장 속도가 좋았습니다.");
            }
        } else {
            addMultiKillInsight(strengths, participant);
            if (strengths.size() < 3 && damageRank <= 3) {
                strengths.add("챔피언 피해량 " + formatNumber(participant.getTotalDamageDealtToChampions())
                        + "로 10명 중 " + damageRank + "위, 팀 내 " + teamDamageRank + "위였습니다.");
            }
            if (strengths.size() < 3 && isGrowthStrength(participant, csRank, goldRank)) {
                strengths.add("총 CS " + formatNumber(totalCs) + ", 골드 " + formatNumber(participant.getGoldEarned())
                        + "로 각각 10명 중 " + csRank + "위, " + goldRank + "위였습니다.");
            }
        }

        addOpeningFightInsight(strengths, participant);
        addFrontlineInsight(strengths, participant, teamDamageTakenRank);

        if (strengths.size() < 3 && lowDeathRank <= 2 && kda >= 3.0) {
            strengths.add("데스 " + formatNumber(participant.getDeaths()) + "회로 가장 적은 축에 속했고, KDA "
                    + formatDecimal(kda) + "로 안정적으로 마무리했습니다.");
        }

        if (strengths.size() < 3 && teamCsRank <= 2 && !"UTILITY".equals(participant.getTeamPosition())) {
            strengths.add("팀 내 성장 속도도 빨랐습니다. 총 CS " + formatNumber(totalCs)
                    + "로 팀 내 " + teamCsRank + "위였습니다.");
        }
    }

    private static void addWeaknesses(
            List<String> weaknesses,
            MatchParticipant participant,
            int damageRank,
            int visionRank,
            int goldRank,
            int csRank,
            int highDeathRank,
            double kda,
            int totalCs,
            int teamObjectiveDamageRank
    ) {
        if (highDeathRank <= 3 && kda < 2.0) {
            weaknesses.add("데스 " + formatNumber(participant.getDeaths()) + "회로 10명 중 " + highDeathRank
                    + "위였고, KDA " + formatDecimal(kda) + "로 교전 효율이 낮았습니다.");
        }

        if ("UTILITY".equals(participant.getTeamPosition())) {
            if (weaknesses.size() < 2 && visionRank >= 8 && participant.getVisionScore() < 20) {
                weaknesses.add("서포터 포지션 기준 시야 점수 " + formatNumber(participant.getVisionScore())
                        + "는 낮은 편이어서 맵 장악이 부족했습니다.");
            }
        } else if ("JUNGLE".equals(participant.getTeamPosition())) {
            if (weaknesses.size() < 2 && participant.getDamageDealtToObjectives() < 5000 && teamObjectiveDamageRank >= 3) {
                weaknesses.add("오브젝트 피해량 " + formatNumber(participant.getDamageDealtToObjectives())
                        + "로 정글 포지션 기준 오브젝트 관여가 약했습니다.");
            }
        } else {
            if (weaknesses.size() < 2 && damageRank >= 8) {
                weaknesses.add("챔피언 피해량 " + formatNumber(participant.getTotalDamageDealtToChampions())
                        + "로 10명 중 " + damageRank + "위에 그쳐 딜 기여가 약했습니다.");
            }
            if (weaknesses.size() < 2 && csRank >= 8 && goldRank >= 8) {
                weaknesses.add("총 CS " + formatNumber(totalCs) + ", 골드 " + formatNumber(participant.getGoldEarned())
                        + "로 성장 속도가 느린 편이었습니다.");
            }
        }
    }

    private static void addMultiKillInsight(List<String> insights, MatchParticipant participant) {
        if (participant.getPentaKills() > 0) {
            insights.add("펜타킬을 기록한 경기였습니다. 멀티킬 최고 단계도 "
                    + formatNumber(participant.getLargestMultiKill()) + "단계였습니다.");
            return;
        }

        if (participant.getQuadraKills() > 0) {
            insights.add("쿼드라킬 " + formatNumber(participant.getQuadraKills())
                    + "회를 기록하며 한타 마무리 능력이 돋보였습니다.");
            return;
        }

        if (participant.getTripleKills() > 0) {
            insights.add("트리플킬 " + formatNumber(participant.getTripleKills())
                    + "회로 교전 결정력을 보여준 경기였습니다.");
            return;
        }

        if (participant.getDoubleKills() >= 2 || participant.getLargestKillingSpree() >= 4) {
            insights.add("더블킬 " + formatNumber(participant.getDoubleKills()) + "회, 최대 킬링 스프리 "
                    + formatNumber(participant.getLargestKillingSpree()) + "로 연속 교전 흐름을 만들었습니다.");
        }
    }

    private static void addOpeningFightInsight(List<String> insights, MatchParticipant participant) {
        if (participant.isFirstBloodKill() && participant.isFirstTowerKill()) {
            insights.add("퍼스트 블러드와 퍼스트 타워에 모두 직접 관여해 초반 주도권을 잡았습니다.");
            return;
        }

        if (participant.isFirstBloodKill()) {
            insights.add("퍼스트 블러드를 기록하며 초반 라인전 흐름을 먼저 가져왔습니다.");
            return;
        }

        if (participant.isFirstBloodAssist()) {
            insights.add("퍼스트 블러드에 관여하며 초반 교전 개시에 힘을 실었습니다.");
            return;
        }

        if (participant.isFirstTowerKill() || participant.isFirstTowerAssist()) {
            insights.add("퍼스트 타워에 관여해 초반 오브젝트 주도권을 만드는 데 기여했습니다.");
        }
    }

    private static void addObjectiveInsight(
            List<String> insights,
            MatchParticipant participant,
            int teamObjectiveDamageRank,
            int teamTurretDamageRank
    ) {
        if (participant.getObjectivesStolen() > 0) {
            insights.add("오브젝트 스틸 " + formatNumber(participant.getObjectivesStolen())
                    + "회를 기록한 경기였습니다.");
            return;
        }

        if (participant.getDamageDealtToObjectives() >= 10000 || participant.getDamageDealtToTurrets() >= 3000) {
            insights.add("오브젝트 피해량 " + formatNumber(participant.getDamageDealtToObjectives())
                    + ", 포탑 피해량 " + formatNumber(participant.getDamageDealtToTurrets())
                    + "로 각각 팀 내 " + teamObjectiveDamageRank + "위, " + teamTurretDamageRank + "위였습니다.");
            return;
        }

        if (participant.getTurretKills() > 0 || participant.getInhibitorKills() > 0) {
            insights.add("포탑 " + formatNumber(participant.getTurretKills()) + "개, 억제기 "
                    + formatNumber(participant.getInhibitorKills()) + "개를 직접 파괴했습니다.");
        }
    }

    private static void addSupportInsight(
            List<String> insights,
            MatchParticipant participant,
            int teamShieldRank,
            int teamHealRank
    ) {
        if (participant.getTotalDamageShieldedOnTeammates() >= 1500) {
            insights.add("아군 보호막량 " + formatNumber(participant.getTotalDamageShieldedOnTeammates())
                    + "로 팀 내 " + teamShieldRank + "위였습니다.");
            return;
        }

        if (participant.getTotalHealsOnTeammates() >= 1000) {
            insights.add("아군 회복량 " + formatNumber(participant.getTotalHealsOnTeammates())
                    + "로 팀 내 " + teamHealRank + "위였습니다.");
        }
    }

    private static void addFrontlineInsight(
            List<String> insights,
            MatchParticipant participant,
            int teamDamageTakenRank
    ) {
        if (participant.getTotalDamageTaken() >= 25000 && teamDamageTakenRank <= 2) {
            insights.add("받은 피해량 " + formatNumber(participant.getTotalDamageTaken())
                    + "로 팀 내 " + teamDamageTakenRank + "위였고, 전면에서 버텨낸 경기였습니다.");
        }
    }

    private static boolean isVisionStrength(MatchParticipant participant, int visionRank) {
        if ("UTILITY".equals(participant.getTeamPosition())) {
            return visionRank <= 2;
        }

        if ("JUNGLE".equals(participant.getTeamPosition())) {
            return visionRank <= 3;
        }

        return visionRank <= 2 && participant.getVisionScore() >= 20;
    }

    private static boolean isGrowthStrength(MatchParticipant participant, int csRank, int goldRank) {
        if ("UTILITY".equals(participant.getTeamPosition())) {
            return goldRank <= 3;
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

    private static String formatNumber(int value) {
        return String.format(Locale.KOREA, "%,d", value);
    }

    private static String formatDecimal(double value) {
        return String.format(Locale.KOREA, "%.2f", value);
    }
}
