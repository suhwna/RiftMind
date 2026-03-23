# Summoner Service 설계

## Related Docs

- [Development Rules](./development-rules.md)
- [Platform Overview](./common/platform-overview.md)
- [Development Roadmap](./common/development-roadmap.md)
- [Riot API Flow](./common/riot-api-flow.md)

## 1. 목적

`summoner-service` 는 Riot API 원천 데이터를 수집하고 내부 저장소에 적재한 뒤,
플레이어/경기 조회용 API를 제공하는 서비스를 목표로 한다.

핵심 기준은 다음과 같다.

- 사용자 입력 기준은 `Riot ID(gameName + tagLine)` 이다.
- 내부 식별 기준은 `puuid` 이다.
- 경기 수집 기준은 `Match-V5` 이다.
- 검색, 메타 통계, AI 분석은 이 서비스 책임이 아니다.

---

## 2. 책임 범위

### 포함

- Riot ID -> PUUID 변환
- PUUID 기반 summoner 정보 조회
- 최근 matchId 목록 조회
- match 상세 데이터 수집
- PostgreSQL 저장
- 저장된 summoner / match 조회 API 제공

### 제외

- Elasticsearch 색인/검색
- 챔피언 메타 집계
- AI 코칭 / RAG
- Kafka 기반 비동기 파이프라인

---

## 3. 권장 내부 흐름

```text
Client
-> POST /api/v1/summoners/sync
-> Riot ID 입력

summoner-service
-> Account-V1: by-riot-id
-> Summoner-V4: by-puuid
-> Match-V5: by-puuid/{puuid}/ids
-> Match-V5: /matches/{matchId}
-> PostgreSQL 저장
-> sync 결과 반환
```

조회 API는 가능한 한 DB를 먼저 읽고, 동기화는 별도 API로 분리한다.

---

## 4. API 명세

### 4.1 소환사 동기화

`POST /api/v1/summoners/sync`

Riot API를 호출해 플레이어와 최근 경기 데이터를 수집하고 저장한다.

#### Request

```json
{
  "gameName": "Faker",
  "tagLine": "KR1",
  "matchCount": 20
}
```

#### Response 200

```json
{
  "puuid": "xxxx",
  "gameName": "Faker",
  "tagLine": "KR1",
  "requestedMatchCount": 20,
  "savedMatchCount": 20,
  "syncedAt": "2026-03-23T16:00:00"
}
```

#### 검증 규칙

- `gameName`: 필수, 공백 불가
- `tagLine`: 필수, 공백 불가
- `matchCount`: 기본값 20, 최대 20 권장

---

### 4.2 Riot ID 기준 소환사 조회

`GET /api/v1/summoners/by-riot-id?gameName={gameName}&tagLine={tagLine}`

내부 DB에 저장된 소환사 정보를 Riot ID 기준으로 조회한다.

#### Response 200

```json
{
  "puuid": "xxxx",
  "gameName": "Faker",
  "tagLine": "KR1",
  "summonerLevel": 999,
  "profileIconId": 1234,
  "lastSyncedAt": "2026-03-23T16:00:00"
}
```

#### 비고

- 기본 정책은 DB 조회이다.
- 데이터가 없을 경우 `404` 를 반환하고, 클라이언트가 `sync` API를 호출하게 한다.

---

### 4.3 PUUID 기준 소환사 조회

`GET /api/v1/summoners/{puuid}`

#### Response 200

```json
{
  "puuid": "xxxx",
  "gameName": "Faker",
  "tagLine": "KR1",
  "summonerId": "xxxx",
  "accountId": "xxxx",
  "summonerLevel": 999,
  "profileIconId": 1234,
  "lastSyncedAt": "2026-03-23T16:00:00"
}
```

---

### 4.4 최근 경기 목록 조회

`GET /api/v1/summoners/{puuid}/matches?count=20`

#### Response 200

```json
{
  "puuid": "xxxx",
  "count": 2,
  "matches": [
    {
      "matchId": "KR_1234567890",
      "gameCreation": "2026-03-23T15:00:00",
      "queueId": 420,
      "gameMode": "CLASSIC",
      "championName": "Ahri",
      "teamPosition": "MIDDLE",
      "kills": 10,
      "deaths": 2,
      "assists": 8,
      "win": true
    },
    {
      "matchId": "KR_1234567891",
      "gameCreation": "2026-03-23T14:10:00",
      "queueId": 420,
      "gameMode": "CLASSIC",
      "championName": "Orianna",
      "teamPosition": "MIDDLE",
      "kills": 4,
      "deaths": 1,
      "assists": 12,
      "win": true
    }
  ]
}
```

#### 비고

- 응답은 `해당 puuid 참가자 관점` 으로 요약한다.
- 상세 참가자 10명 전체 정보는 별도 경기 상세 API에서 제공한다.

---

### 4.5 경기 상세 조회

`GET /api/v1/matches/{matchId}`

#### Response 200

```json
{
  "matchId": "KR_1234567890",
  "gameCreation": "2026-03-23T15:00:00",
  "gameDuration": 1860,
  "queueId": 420,
  "gameMode": "CLASSIC",
  "gameVersion": "15.6.1",
  "participants": [
    {
      "puuid": "xxxx",
      "summonerName": "Hide on bush",
      "championName": "Ahri",
      "teamPosition": "MIDDLE",
      "kills": 10,
      "deaths": 2,
      "assists": 8,
      "win": true
    }
  ]
}
```

---

## 5. 에러 응답 원칙

### 공통 포맷 예시

```json
{
  "code": "SUMMONER_NOT_FOUND",
  "message": "Summoner not found",
  "timestamp": "2026-03-23T16:00:00"
}
```

### 주요 에러 코드

- `INVALID_REQUEST`
- `SUMMONER_NOT_FOUND`
- `MATCH_NOT_FOUND`
- `RIOT_API_ERROR`
- `RIOT_RATE_LIMITED`

---

## 6. DB 테이블 설계

### 6.1 summoner_profile

플레이어 기본 프로필 저장

| 컬럼명 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| puuid | varchar(100) | PK | Riot 내부 고유 식별자 |
| game_name | varchar(50) | not null | Riot ID gameName |
| tag_line | varchar(10) | not null | Riot ID tagLine |
| summoner_id | varchar(100) | null | Summoner-V4 식별자 |
| account_id | varchar(100) | null | encryptedAccountId |
| profile_icon_id | integer | null | 프로필 아이콘 |
| summoner_level | bigint | null | 소환사 레벨 |
| last_synced_at | timestamp | not null | 최근 동기화 시각 |
| created_at | timestamp | not null | 생성 시각 |
| updated_at | timestamp | not null | 수정 시각 |

#### 인덱스

- `uk_summoner_profile_riot_id(game_name, tag_line)`

---

### 6.2 match_summary

경기 공통 메타 저장

| 컬럼명 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| match_id | varchar(30) | PK | 경기 ID |
| game_creation | timestamp | not null | 경기 시작 시각 |
| game_duration | integer | not null | 경기 길이(초) |
| queue_id | integer | not null | 큐 ID |
| game_mode | varchar(30) | not null | 게임 모드 |
| game_version | varchar(20) | not null | 패치 버전 |
| created_at | timestamp | not null | 생성 시각 |
| updated_at | timestamp | not null | 수정 시각 |

#### 인덱스

- `idx_match_summary_game_creation(game_creation desc)`
- `idx_match_summary_queue_id(queue_id)`

---

### 6.3 match_participant

경기 참가자별 상세 스탯 저장

| 컬럼명 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| id | bigint | PK | 내부 식별자 |
| match_id | varchar(30) | FK | 경기 ID |
| puuid | varchar(100) | not null | 참가자 PUUID |
| summoner_name | varchar(100) | null | 표시용 소환사명 |
| champion_name | varchar(50) | not null | 챔피언 |
| team_position | varchar(20) | null | TOP/JUNGLE/MIDDLE/BOTTOM/UTILITY |
| kills | integer | not null | 킬 |
| deaths | integer | not null | 데스 |
| assists | integer | not null | 어시스트 |
| win | boolean | not null | 승패 |
| created_at | timestamp | not null | 생성 시각 |
| updated_at | timestamp | not null | 수정 시각 |

#### 인덱스

- `idx_match_participant_puuid(puuid)`
- `idx_match_participant_match_id(match_id)`
- `idx_match_participant_puuid_game(match_id, puuid)`

---

### 6.4 sync_history

동기화 작업 이력 저장

| 컬럼명 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| id | bigint | PK | 내부 식별자 |
| puuid | varchar(100) | not null | 대상 플레이어 |
| requested_match_count | integer | not null | 요청 경기 수 |
| saved_match_count | integer | not null | 저장 경기 수 |
| status | varchar(20) | not null | SUCCESS / FAILED |
| error_message | varchar(500) | null | 실패 메시지 |
| created_at | timestamp | not null | 실행 시각 |

---

## 7. 테이블 관계

```text
summoner_profile (1)
   |
   | puuid
   v
match_participant (N)

match_summary (1)
   |
   | match_id
   v
match_participant (N)
```

주의:

- `match_summary` 와 `match_participant` 는 경기 기준 저장이다.
- `summoner_profile` 과 직접 경기 목록 테이블을 따로 두지 않아도 `match_participant.puuid` 로 역조회 가능하다.

---

## 8. 구현 우선순위

### 1단계

- `POST /api/v1/summoners/sync`
- `GET /api/v1/summoners/{puuid}`
- `GET /api/v1/summoners/{puuid}/matches`

### 2단계

- `GET /api/v1/summoners/by-riot-id`
- `GET /api/v1/matches/{matchId}`
- 캐시 적용

### 3단계

- Kafka 이벤트 발행
- 비동기 수집 전환

---

## 9. 후속 구현 메모

- Riot API 호출은 `RestClient` 기반으로 구현한다.
- 조회 API와 동기화 API를 분리해 rate limit 부담을 줄인다.
- `common` 모듈에는 엔티티를 두지 않는다.
- `search-service` 는 향후 별도 저장소/인덱싱 책임으로 분리한다.
