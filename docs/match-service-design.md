# Match Service 설계

## Related Docs

- [Development Rules](./development-rules.md)
- [Platform Overview](./common/platform-overview.md)
- [Development Roadmap](./common/development-roadmap.md)
- [Riot API Flow](./common/riot-api-flow.md)

## 1. 목적

`match-service` 는 Riot `Match-V5` 상세 데이터를 수집하고,
매치/참가자 단위로 저장 및 재조회하는 서비스다.

핵심 기준은 다음과 같다.

- 입력 기준은 `puuid` 또는 `matchId`
- 내부 저장 중심은 `matchId`
- 표시용 변환은 Data Dragon / queues metadata 기반
- 검색, 메타 통계, AI 분석은 이 서비스 책임이 아니다

---

## 2. 책임 범위

### 포함

- `puuid` 기준 최근 매치 동기화
- `matchId` 기준 상세 조회
- 매치 참가자 상세 스탯 저장
- 챔피언 / 포지션 / 큐 / 아이템 / 스펠 / 룬 표시값 변환

### 제외

- Riot ID -> PUUID 변환
- 소환사 프로필 저장
- Elasticsearch 색인/검색
- AI 코칭 / RAG

---

## 3. 권장 내부 흐름

```text
Client
-> POST /api/v1/matches/sync
-> puuid 입력

match-service
-> Match-V5: by-puuid/{puuid}/ids
-> Match-V5: /matches/{matchId}
-> match_summary 저장
-> match_participant 저장
-> sync 결과 반환
```

---

## 4. API 명세

### 4.1 매치 동기화

`POST /api/v1/matches/sync`

```json
{
  "puuid": "xxxx",
  "matchCount": 20
}
```

### 4.2 최근 경기 조회

`GET /api/v1/matches/by-puuid?puuid={puuid}&count=20`

- 응답은 `해당 puuid 참가자 관점` 경기 요약이다.
- `queueNameKo`, `championNameKo`, `teamPositionKo` 같은 표시용 필드를 포함한다.

### 4.3 경기 상세 조회

`GET /api/v1/matches/{matchId}`

- 매치 공통 메타 + 참가자 10명 상세 정보를 반환한다.
- 참가자 응답에는 딜량, 골드, CS, 시야, 아이템, 스펠, 룬 계열 등 상세 값이 포함된다.

---

## 5. 저장 모델

### 5.1 match_summary

- 경기 공통 메타 저장
- 주요 필드:
  - `match_id`
  - `game_creation`
  - `game_duration`
  - `queue_id`
  - `game_mode`
  - `game_version`

### 5.2 match_participant

- 참가자 관점 상세 스탯 저장
- 주요 필드:
  - `puuid`
  - `summoner_name` (`gameName#tagLine` 우선)
  - `champion_name`
  - `team_position`
  - `kills/deaths/assists`
  - `total_damage_dealt_to_champions`
  - `gold_earned`
  - `total_minions_killed`
  - `neutral_minions_killed`
  - `vision_score`
  - `wards_placed`
  - `wards_killed`
  - `champ_level`
  - `item_0 ~ item_6`
  - `summoner_1_id`, `summoner_2_id`
  - `primary_rune`, `secondary_rune`
  - `total_damage_taken`

### 5.3 sync_history

- 매치 동기화 작업 이력 저장

---

## 6. 표시 정책

- queue 표시는 `queueId + queues.json` 기준
- 챔피언 / 아이템 / 스펠 / 룬 표시는 Data Dragon 기준
- 포지션은 유효값만 저장
  - `TOP`, `JUNGLE`, `MIDDLE`, `BOTTOM`, `UTILITY`
- `Invalid`, 빈 문자열, null 은 저장하지 않는다

---

## 7. 서비스 경계

```text
summoner-service
-> Riot ID / summoner profile
-> match-service 호출

match-service
-> Riot Match-V5 수집
-> match 저장 / 조회
```

즉 `summoner-service` 는 진입점,
`match-service` 는 경기 데이터의 원천 저장소 역할을 가진다.

---

## 8. 후속 구현 메모

- 3주차부터는 `match-service` 저장 데이터를 Elasticsearch에 색인
- queue 한글명은 인게임 표기 기준으로 계속 보정 가능
- 통계 / 메타 / AI 서비스는 `match-service` 원천 데이터를 소비하는 구조로 확장
