# Search Service 설계 문서

## Related Docs

- [Platform Overview](./common/platform-overview.md)
- [Development Roadmap](./common/development-roadmap.md)
- [Development Rules](./development-rules.md)
- [Match Service Design](./match-service-design.md)

## 1. 목적

`search-service` 는 `match-service` 에 저장된 경기/참가자 원천 데이터를
Elasticsearch 검색 문서로 색인하고, 조건 검색 API를 제공한다.

핵심 목표는 아래와 같다.

- 챔피언 / 포지션 / 승패 / KDA 기준 검색
- 아이템 / 룬 / 스펠 기준 검색
- 딜량 / 골드 / CS / 시야 점수 범위 검색
- 이후 통계 / AI 기능이 재사용할 검색 레이어 제공

---

## 2. 현재 책임 범위

### search-service

- `match-service` 검색 소스 API 호출
- participant 기준 검색 문서 생성
- Elasticsearch 인덱스 관리
- 검색 API 제공

### search-service가 하지 않는 일

- Riot API 직접 호출
- 매치 원천 데이터 저장
- 소환사 프로필 관리

---

## 3. 현재 아키텍처 위치

```text
Client
  ↓
API Gateway
  └─ /api/v1/search/** -> search-service

search-service
  ├─ match-service /api/v1/matches/search-source/by-puuid 호출
  ├─ Elasticsearch index: match-search-v3
  └─ /api/v1/search/matches 제공
```

---

## 4. 색인 전략

초기 summary 기반 색인 이후, 현재는 `MATCH_PARTICIPANT` 기반 색인으로 전환했다.

색인 단위는 `한 경기의 한 참가자` 이다.

문서 주요 필드:

- `matchId`
- `puuid`
- `gameCreation`
- `queueId`
- `queueNameKo`
- `gameMode`
- `summonerName`
- `championName`
- `championNameKo`
- `teamPosition`
- `teamPositionKo`
- `kills`
- `deaths`
- `assists`
- `kda`
- `win`
- `totalDamageDealtToChampions`
- `goldEarned`
- `totalMinionsKilled`
- `neutralMinionsKilled`
- `visionScore`
- `wardsPlaced`
- `wardsKilled`
- `champLevel`
- `itemIds`
- `itemNames`
- `summonerSpellIds`
- `summonerSpellNames`
- `primaryRune`
- `primaryRuneName`
- `secondaryRune`
- `secondaryRuneName`
- `totalDamageTaken`

---

## 5. API

### 최근 경기 색인

`POST /api/v1/search/index/matches`

요청:

- `puuid`
- `matchCount`

동작:

1. `match-service` 에서 최근 참가자 검색 소스 조회
2. Elasticsearch 문서로 변환
3. `match-search-v3` 인덱스에 저장

### 매치 검색

`GET /api/v1/search/matches`

현재 지원 필터:

- `puuid`
- `championName`
- `teamPosition`
- `queueId`
- `win`
- `itemName`
- `summonerSpellName`
- `primaryRuneName`
- `secondaryRuneName`
- `minKda` / `maxKda`
- `minDamage` / `maxDamage`
- `minGold` / `maxGold`
- `minCs` / `maxCs`
- `minVisionScore` / `maxVisionScore`

---

## 6. 의존 방향

- `search-service -> match-service`
- `search-service -> Elasticsearch`

역방향 의존은 두지 않는다.

즉 `match-service` 는 검색 구현을 모르고,
`search-service` 가 검색 문서 생성 책임을 가진다.

---

## 7. 향후 확장

- 검색 응답 정렬 옵션 확장
- 다중 필터 조합 UX 개선
- Aggregation 기반 통계 API
- Kafka 이벤트 기반 비동기 색인

---

## 8. 한 줄 요약

> `search-service` 는 `match-service` 참가자 데이터를 Elasticsearch 문서로 색인해 검색 가능한 형태로 변환하는 전용 서비스다.
