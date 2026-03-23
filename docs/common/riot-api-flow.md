# Riot API 호출 흐름 정리

## Related Docs

- [Platform Overview](./platform-overview.md)
- [Development Rules](../development-rules.md)
- [Development Roadmap](./development-roadmap.md)
- [Summoner Service Design](../summoner-service-design.md)

## 1. 목적

RiftMind는 Riot API를 이용해 플레이어의 최근 경기 데이터를 수집하고,  
이를 기반으로 전적 조회, 검색, 통계, AI 분석 기능을 제공한다.

본 프로젝트의 기본 원칙은 다음과 같다.

- 플레이어 식별은 **Riot ID (`gameName + tagLine`)** 기준으로 한다.
- 내부 식별자는 **PUUID**를 기준으로 관리한다.
- 경기 데이터 수집은 **Match-V5**를 중심으로 한다.
- `summonerName` 기반 조회는 더 이상 주 경로로 사용하지 않는다.

Riot 공식 문서도 Summoner Name에서 Riot ID로 전환되었으며,  
플레이어 조회는 Riot ID를 사용하고 가능한 경우 PUUID 엔드포인트를 사용할 것을 권장한다.  
또한 `by-name` 엔드포인트는 deprecated 상태이므로 애플리케이션의 주 경로로 사용하지 않는 것이 바람직하다. :contentReference[oaicite:0]{index=0}

---

## 2. 권장 API 호출 순서

### Step 1. Riot ID로 PUUID 조회

사용자가 입력한 Riot ID(`gameName + tagLine`)를 기준으로  
먼저 Account-V1에서 PUUID를 조회한다.

#### Endpoint
`GET /riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}`

#### 목적
- 사용자 입력값을 내부 식별자인 `puuid`로 변환
- 이후 모든 경기 조회의 시작점으로 사용

Riot 공식 문서에서도 Riot ID에서 PUUID를 얻는 첫 단계로 이 엔드포인트를 안내한다. :contentReference[oaicite:1]{index=1}

#### 예시
`GET /riot/account/v1/accounts/by-riot-id/Faker/KR1`

#### 응답 활용 예시
- `puuid`
- `gameName`
- `tagLine`

---

### Step 2. PUUID로 소환사 정보 조회

PUUID를 얻은 뒤 Summoner-V4에서 추가 소환사 정보를 조회한다.

#### Endpoint
`GET /lol/summoner/v4/summoners/by-puuid/{puuid}`

#### 목적
- `summonerId`
- `encryptedAccountId`
- `profileIconId`
- `summonerLevel`

등의 보조 정보를 확보

Riot 공식 문서에서는 Riot ID에서 얻은 PUUID를 이용해 Summoner-V4 `by-puuid` 엔드포인트로 summoner 데이터를 조회할 수 있다고 설명한다. :contentReference[oaicite:2]{index=2}

#### 응답 활용 예시
- 프로필 아이콘 표시
- 소환사 레벨 표시
- 내부 보조 식별자 저장

---

### Step 3. PUUID로 최근 경기 ID 목록 조회

이제 Match-V5에서 특정 플레이어의 최근 경기 ID 목록을 조회한다.

#### Endpoint
`GET /lol/match/v5/matches/by-puuid/{puuid}/ids`

#### 목적
- 최근 N경기 matchId 확보
- 이후 상세 경기 수집 대상 결정

#### 예시 파라미터
- `start`
- `count`
- 필요 시 queue, type 등 조건 추가 검토

#### 응답 활용 예시
- `KR_1234567890`
- `KR_1234567891`

---

### Step 4. matchId로 경기 상세 조회

각 matchId에 대해 상세 경기 데이터를 수집한다.

#### Endpoint
`GET /lol/match/v5/matches/{matchId}`

#### 목적
- 경기 공통 정보 수집
- 참가자 10명의 상세 스탯 수집
- DB 저장 및 Elasticsearch 색인 원천 데이터 확보

#### 주요 활용 필드 예시
- `metadata.matchId`
- `info.gameCreation`
- `info.queueId`
- `info.gameMode`
- `info.gameVersion`
- `info.participants[]`

---

## 3. 프로젝트 내부 데이터 흐름

RiftMind 기준 권장 흐름은 아래와 같다.

```text
Riot ID 입력
→ Account-V1
→ PUUID 조회

→ Summoner-V4
→ 소환사 상세 정보 조회

→ Match-V5 (by-puuid)
→ 최근 matchId 목록 조회

→ Match-V5 (by matchId)
→ 경기 상세 조회

→ PostgreSQL 저장
→ Elasticsearch 색인
→ 통계 / 검색 / AI 분석
