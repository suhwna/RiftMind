package com.riftmind.match.api.response;

import java.util.List;

import com.riftmind.match.application.dto.MatchParticipantView;
import com.riftmind.match.application.service.StaticDataService;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 매치 참가자 정보를 담는 API 응답 DTO입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Schema(description = "매치 참가자 응답")
public record MatchParticipantResponse(
        @Schema(description = "Riot PUUID", example = "sample-puuid-value")
        String puuid,
        @Schema(description = "참가자 Riot ID", example = "Hide on bush#KR1")
        String summonerName,
        @Schema(description = "챔피언 이름", example = "Ahri")
        String championName,
        @Schema(description = "챔피언 아이콘 키", example = "Ahri")
        String championKey,
        @Schema(description = "챔피언 한글 이름", example = "아리")
        String championNameKo,
        @Schema(description = "팀 ID", example = "100")
        Integer teamId,
        @Schema(description = "라인 포지션", example = "MIDDLE")
        String teamPosition,
        @Schema(description = "라인 포지션 한글 이름", example = "미드")
        String teamPositionKo,
        @Schema(description = "킬 수", example = "10")
        int kills,
        @Schema(description = "데스 수", example = "2")
        int deaths,
        @Schema(description = "어시스트 수", example = "8")
        int assists,
        @Schema(description = "승리 여부", example = "true")
        boolean win,
        @Schema(description = "챔피언 대상 총 피해량", example = "28451")
        int totalDamageDealtToChampions,
        @Schema(description = "획득 골드", example = "15234")
        int goldEarned,
        @Schema(description = "총 미니언 처치 수", example = "238")
        int totalMinionsKilled,
        @Schema(description = "정글 몬스터 처치 수", example = "12")
        int neutralMinionsKilled,
        @Schema(description = "시야 점수", example = "27")
        int visionScore,
        @Schema(description = "설치 와드 수", example = "9")
        int wardsPlaced,
        @Schema(description = "제거 와드 수", example = "3")
        int wardsKilled,
        @Schema(description = "챔피언 레벨", example = "17")
        int champLevel,
        @Schema(description = "아이템 0", example = "6655")
        int item0,
        @Schema(description = "아이템 1", example = "3020")
        int item1,
        @Schema(description = "아이템 2", example = "3102")
        int item2,
        @Schema(description = "아이템 3", example = "3089")
        int item3,
        @Schema(description = "아이템 4", example = "3135")
        int item4,
        @Schema(description = "아이템 5", example = "4645")
        int item5,
        @Schema(description = "아이템 6", example = "3363")
        int item6,
        @Schema(description = "구매 아이템 이름 목록")
        List<String> itemNames,
        @Schema(description = "구매 아이템 아이콘 URL 목록")
        List<String> itemIconUrls,
        @Schema(description = "구매 아이템 설명 목록")
        List<String> itemDescriptions,
        @Schema(description = "소환사 주문 1 ID", example = "4")
        int summoner1Id,
        @Schema(description = "소환사 주문 2 ID", example = "12")
        int summoner2Id,
        @Schema(description = "소환사 주문 이름 목록")
        List<String> summonerSpellNames,
        @Schema(description = "소환사 주문 아이콘 URL 목록")
        List<String> summonerSpellIconUrls,
        @Schema(description = "소환사 주문 설명 목록")
        List<String> summonerSpellDescriptions,
        @Schema(description = "주 룬 스타일 ID", example = "8100")
        Integer primaryRune,
        @Schema(description = "주 룬 스타일 이름", example = "지배")
        String primaryRuneName,
        @Schema(description = "주 룬 스타일 아이콘 URL")
        String primaryRuneIconUrl,
        @Schema(description = "주 룬 스타일 설명")
        String primaryRuneDescription,
        @Schema(description = "보조 룬 스타일 ID", example = "8300")
        Integer secondaryRune,
        @Schema(description = "보조 룬 스타일 이름", example = "영감")
        String secondaryRuneName,
        @Schema(description = "보조 룬 스타일 아이콘 URL")
        String secondaryRuneIconUrl,
        @Schema(description = "보조 룬 스타일 설명")
        String secondaryRuneDescription,
        @Schema(description = "경기 해석 태그 목록")
        List<String> interpretationTags,
        @Schema(description = "총 받은 피해량", example = "18422")
        int totalDamageTaken,
        @Schema(description = "더블킬 횟수", example = "1")
        int doubleKills,
        @Schema(description = "트리플킬 횟수", example = "0")
        int tripleKills,
        @Schema(description = "쿼드라킬 횟수", example = "0")
        int quadraKills,
        @Schema(description = "펜타킬 횟수", example = "0")
        int pentaKills,
        @Schema(description = "최대 킬링 스프리", example = "4")
        int largestKillingSpree,
        @Schema(description = "최대 멀티킬 등급", example = "2")
        int largestMultiKill,
        @Schema(description = "킬링 스프리 횟수", example = "2")
        int killingSprees,
        @Schema(description = "퍼스트 블러드 킬 여부", example = "false")
        boolean firstBloodKill,
        @Schema(description = "퍼스트 블러드 어시스트 여부", example = "true")
        boolean firstBloodAssist,
        @Schema(description = "퍼스트 타워 관여 킬 여부", example = "false")
        boolean firstTowerKill,
        @Schema(description = "퍼스트 타워 관여 어시스트 여부", example = "false")
        boolean firstTowerAssist,
        @Schema(description = "포탑 파괴 수", example = "3")
        int turretKills,
        @Schema(description = "억제기 파괴 수", example = "1")
        int inhibitorKills,
        @Schema(description = "오브젝트 피해량", example = "18344")
        int damageDealtToObjectives,
        @Schema(description = "포탑 피해량", example = "5240")
        int damageDealtToTurrets,
        @Schema(description = "오브젝트 스틸 횟수", example = "0")
        int objectivesStolen,
        @Schema(description = "오브젝트 스틸 어시스트 횟수", example = "0")
        int objectivesStolenAssists,
        @Schema(description = "총 회복량", example = "4120")
        int totalHeal,
        @Schema(description = "아군 대상 총 회복량", example = "1200")
        int totalHealsOnTeammates,
        @Schema(description = "아군 대상 총 보호막량", example = "3400")
        int totalDamageShieldedOnTeammates) {

    /**
     * 참가자 조회 DTO를 API 응답 DTO로 변환합니다.
     *
     * @param view 참가자 조회 DTO
     * @param staticDataService 정적 데이터 서비스
     * @return 참가자 응답 DTO
     */
    public static MatchParticipantResponse from(MatchParticipantView view, StaticDataService staticDataService) {
        return new MatchParticipantResponse(
                view.puuid(),
                view.summonerName(),
                view.championName(),
                staticDataService.getChampionAssetKey(view.championName()),
                staticDataService.getChampionNameKo(view.championName()),
                view.teamId(),
                view.teamPosition(),
                staticDataService.getTeamPositionKo(view.teamPosition()),
                view.kills(),
                view.deaths(),
                view.assists(),
                view.win(),
                view.totalDamageDealtToChampions(),
                view.goldEarned(),
                view.totalMinionsKilled(),
                view.neutralMinionsKilled(),
                view.visionScore(),
                view.wardsPlaced(),
                view.wardsKilled(),
                view.champLevel(),
                view.item0(),
                view.item1(),
                view.item2(),
                view.item3(),
                view.item4(),
                view.item5(),
                view.item6(),
                staticDataService.getItemNames(List.of(
                        view.item0(),
                        view.item1(),
                        view.item2(),
                        view.item3(),
                        view.item4(),
                        view.item5(),
                        view.item6())),
                staticDataService.getItemIconUrls(List.of(
                        view.item0(),
                        view.item1(),
                        view.item2(),
                        view.item3(),
                        view.item4(),
                        view.item5(),
                        view.item6())),
                staticDataService.getItemDescriptions(List.of(
                        view.item0(),
                        view.item1(),
                        view.item2(),
                        view.item3(),
                        view.item4(),
                        view.item5(),
                        view.item6())),
                view.summoner1Id(),
                view.summoner2Id(),
                staticDataService.getSummonerSpellNames(view.summoner1Id(), view.summoner2Id()),
                staticDataService.getSummonerSpellIconUrls(view.summoner1Id(), view.summoner2Id()),
                staticDataService.getSummonerSpellDescriptions(view.summoner1Id(), view.summoner2Id()),
                view.primaryRune(),
                staticDataService.getRuneStyleName(view.primaryRune()),
                staticDataService.getRuneStyleIconUrl(view.primaryRune()),
                staticDataService.getRuneStyleDescription(view.primaryRune()),
                view.secondaryRune(),
                staticDataService.getRuneStyleName(view.secondaryRune()),
                staticDataService.getRuneStyleIconUrl(view.secondaryRune()),
                staticDataService.getRuneStyleDescription(view.secondaryRune()),
                view.interpretationTags(),
                view.totalDamageTaken(),
                view.doubleKills(),
                view.tripleKills(),
                view.quadraKills(),
                view.pentaKills(),
                view.largestKillingSpree(),
                view.largestMultiKill(),
                view.killingSprees(),
                view.firstBloodKill(),
                view.firstBloodAssist(),
                view.firstTowerKill(),
                view.firstTowerAssist(),
                view.turretKills(),
                view.inhibitorKills(),
                view.damageDealtToObjectives(),
                view.damageDealtToTurrets(),
                view.objectivesStolen(),
                view.objectivesStolenAssists(),
                view.totalHeal(),
                view.totalHealsOnTeammates(),
                view.totalDamageShieldedOnTeammates());
    }
}
