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
        @Schema(description = "소환사 주문 1 ID", example = "4")
        int summoner1Id,
        @Schema(description = "소환사 주문 2 ID", example = "12")
        int summoner2Id,
        @Schema(description = "소환사 주문 이름 목록")
        List<String> summonerSpellNames,
        @Schema(description = "주 룬 스타일 ID", example = "8100")
        Integer primaryRune,
        @Schema(description = "주 룬 스타일 이름", example = "지배")
        String primaryRuneName,
        @Schema(description = "보조 룬 스타일 ID", example = "8300")
        Integer secondaryRune,
        @Schema(description = "보조 룬 스타일 이름", example = "영감")
        String secondaryRuneName,
        @Schema(description = "총 받은 피해량", example = "18422")
        int totalDamageTaken) {

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
                view.summoner1Id(),
                view.summoner2Id(),
                staticDataService.getSummonerSpellNames(view.summoner1Id(), view.summoner2Id()),
                view.primaryRune(),
                staticDataService.getRuneStyleName(view.primaryRune()),
                view.secondaryRune(),
                staticDataService.getRuneStyleName(view.secondaryRune()),
                view.totalDamageTaken());
    }
}
