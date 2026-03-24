# Riot API 호출 흐름 정리

## Related Docs

- [Platform Overview](./platform-overview.md)
- [Development Rules](../development-rules.md)
- [Development Roadmap](./development-roadmap.md)
- [Summoner Service Design](../summoner-service-design.md)
- [Match Service Design](../match-service-design.md)

## 1. 목적

RiftMind는 Riot API를 이용해 플레이어의 최근 경기 데이터를 수집하고,
이를 소환사 프로필 / 매치 상세 데이터로 분리 저장한다.

기본 원칙은 아래와 같다.

- 플레이어 식별은 `Riot ID(gameName + tagLine)` 기준
- 내부 식별자는 `PUUID` 기준
- 경기 데이터 수집은 `Match-V5` 기준
- `summonerName` 기반 조회는 주 경로로 사용하지 않음

---

## 2. 권장 API 호출 순서

### Step 1. Riot ID로 PUUID 조회

사용자가 입력한 Riot ID를 기준으로 `Account-V1` 에서 `puuid`를 조회한다.

#### Endpoint

`GET /riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}`

#### 목적

- 사용자 입력값을 내부 식별자 `puuid` 로 변환
- 이후 모든 조회의 시작점으로 사용

---

### Step 2. PUUID로 소환사 정보 조회

PUUID를 얻은 뒤 `Summoner-V4` 에서 추가 소환사 정보를 조회한다.

#### Endpoint

`GET /lol/summoner/v4/summoners/by-puuid/{puuid}`

#### 목적

- `summonerId`
- `encryptedAccountId`
- `profileIconId`
- `summonerLevel`

같은 보조 정보를 확보

---

### Step 3. PUUID로 최근 경기 ID 목록 조회

이제 `Match-V5` 에서 특정 플레이어의 최근 경기 ID 목록을 조회한다.

#### Endpoint

`GET /lol/match/v5/matches/by-puuid/{puuid}/ids`

#### 목적

- 최근 N경기 `matchId` 확보
- 이후 상세 경기 수집 대상 결정

---

### Step 4. matchId로 경기 상세 조회

각 `matchId` 에 대해 상세 경기 데이터를 수집한다.

#### Endpoint

`GET /lol/match/v5/matches/{matchId}`

#### 목적

- 경기 공통 정보 수집
- 참가자 10명의 상세 스탯 수집
- 내부 저장 원천 데이터 확보

#### 주요 활용 필드 예시

- `metadata.matchId`
- `info.gameCreation`
- `info.queueId`
- `info.gameMode`
- `info.gameVersion`
- `info.participants[]`

---

## 3. RiftMind 내부 서비스 흐름

```text
Riot ID 입력
-> summoner-service

summoner-service
-> Account-V1
-> PUUID 조회

-> Summoner-V4
-> 소환사 프로필 조회

-> summoner_profile 저장

-> match-service /api/v1/matches/sync 호출

match-service
-> Match-V5 by-puuid
-> 최근 matchId 목록 조회

-> Match-V5 by matchId
-> 경기 상세 조회

-> match_summary 저장
-> match_participant 저장
```

---

## 4. 서비스별 책임

### summoner-service

- Riot ID 입력 처리
- 계정 / 소환사 프로필 동기화
- `puuid` 확보
- `match-service` 동기화 트리거

### match-service

- 최근 matchId 조회
- 매치 상세 조회
- 매치/참가자 상세 저장
- 표시용 정적 데이터 변환

---

## 5. 저장 후 활용 흐름

1. `summoner_profile` 로 사용자 기본 정보 조회
2. `match_summary`, `match_participant` 로 최근 경기/상세 조회
3. 이후 `search-service` 가 원천 데이터를 색인
4. 통계 / 메타 / AI 서비스가 이를 소비

---

## 6. 구현 메모

- `queueId` 는 표시용 게임 타입 분류의 기준값이다
- 게임 타입 한글 표기는 `queues.json` 기준으로 해석한다
- 챔피언 / 아이템 / 스펠 / 룬 표시는 Data Dragon 기반으로 해석한다
- 포지션은 유효한 라인 값만 저장한다

---

## 7. 한 줄 요약

> Riot ID로 시작해 `puuid`를 얻고, 이후 `match-service` 가 Match-V5 상세 데이터를 수집해 경기 원천 데이터를 저장한다.
