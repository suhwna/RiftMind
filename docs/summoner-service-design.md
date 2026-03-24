# Summoner Service 설계

## Related Docs

- [Development Rules](./development-rules.md)
- [Platform Overview](./common/platform-overview.md)
- [Development Roadmap](./common/development-roadmap.md)
- [Riot API Flow](./common/riot-api-flow.md)
- [Match Service Design](./match-service-design.md)

## 1. 목적

`summoner-service` 는 Riot ID 기준으로 계정/소환사 프로필을 동기화하고,
내부 프로필 조회 API를 제공하는 서비스를 목표로 한다.

핵심 기준은 다음과 같다.

- 사용자 입력 기준은 `Riot ID(gameName + tagLine)` 이다.
- 내부 식별 기준은 `puuid` 이다.
- 매치 수집/저장은 `match-service` 책임이다.
- 검색, 메타 통계, AI 분석은 이 서비스 책임이 아니다.

---

## 2. 책임 범위

### 포함

- Riot ID -> PUUID 변환
- PUUID 기반 summoner 정보 조회
- 소환사 프로필 저장
- 저장된 summoner 조회 API 제공
- `match-service` 동기화 요청 전달
- 최근 경기 목록 조회 진입점 제공

### 제외

- Match-V5 상세 저장
- 매치 상세 재조회
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
-> summoner_profile 저장
-> match-service /api/v1/matches/sync 호출
-> sync 결과 반환
```

조회 API는 가능한 한 DB를 먼저 읽고, 동기화는 별도 API로 분리한다.
경기 목록 조회는 `match-service` 응답을 프록시한다.

---

## 4. API 명세

### 4.1 소환사 동기화

`POST /api/v1/summoners/sync`

Riot API를 호출해 계정/소환사 정보를 수집하고, 이후 `match-service` 에 매치 동기화를 요청한다.

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

---

### 4.3 PUUID 기준 소환사 조회

`GET /api/v1/summoners/{puuid}`

내부 DB에 저장된 소환사 프로필을 PUUID 기준으로 조회한다.

---

### 4.4 최근 경기 목록 조회

`GET /api/v1/summoners/{puuid}/matches?count=20`

#### 비고

- 실제 데이터는 `match-service` 에서 조회한다.
- 응답은 `해당 puuid 참가자 관점` 으로 요약된다.
- `summoner-service` 는 사용자 진입점 역할을 담당한다.

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

### 6.2 sync_history

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

## 7. 서비스 관계

```text
summoner-service
   |
   | puuid / Riot ID
   v
match-service
```

주의:

- `summoner-service` 는 매치 테이블을 직접 저장하지 않는다.
- 매치 데이터의 원천 저장소는 `match-service` 이다.

---

## 8. 구현 우선순위

### 1단계

- `POST /api/v1/summoners/sync`
- `GET /api/v1/summoners/{puuid}`
- `GET /api/v1/summoners/{puuid}/matches`

### 2단계

- `GET /api/v1/summoners/by-riot-id`
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
- 매치 저장/조회 책임은 `match-service` 경계 안에서 유지한다.
