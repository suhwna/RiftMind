# RiftMind 플랫폼 개요

## Related Docs

- [Development Rules](../development-rules.md)
- [Development Roadmap](./development-roadmap.md)
- [Riot API Flow](./riot-api-flow.md)
- [Summoner Service Design](../summoner-service-design.md)
- [Match Service Design](../match-service-design.md)
- [Frontend Screen Design](../frontend-screen-design.md)

---

## 1. 개요

RiftMind는 Riot API 기반 전적 데이터를 수집하고,
이를 검색/통계/AI 분석으로 확장하기 위한 백엔드 플랫폼이다.

현재 기준 구현된 핵심은 아래와 같다.

- `api-gateway`: 외부 진입점, 라우팅, CORS, 통합 Swagger
- `summoner-service`: Riot ID 기반 소환사 프로필 동기화/조회
- `match-service`: 매치 상세 수집, 저장, 조회
- `search-service`: Elasticsearch 색인 및 조건 검색
- `frontend`: React 기반 전적/회고 웹 클라이언트

RiftMind의 차별화 방향은 단순 전적 조회가 아니라
`개인 플레이 회고와 코칭을 위한 데이터 해석`이다.

---

## 2. 현재 아키텍처

```text
Client
  ↓
API Gateway
  ├─ /api/v1/summoners/** -> summoner-service
  ├─ /api/v1/matches/**   -> match-service
  └─ /api/v1/search/**    -> search-service

summoner-service
  ├─ Riot ID -> PUUID 변환
  ├─ summoner_profile 저장
  └─ match-service 호출

match-service
  ├─ Match-V5 수집
  ├─ match_summary 저장
  └─ match_participant 저장

search-service
  ├─ match-service 검색 소스 API 호출
  ├─ Elasticsearch 색인
  └─ 조건 검색 API 제공
```

런타임 DB는 서비스별 파일 H2로 분리되어 있다.

- `summoner-service`: `data/riftmind.mv.db`
- `match-service`: `data/riftmind-match.mv.db`

목표 운영 DB는 `PostgreSQL` 이다.

---

## 3. 핵심 데이터 흐름

1. 사용자가 `Riot ID(gameName + tagLine)` 입력
2. `summoner-service` 가 Account-V1 / Summoner-V4 호출
3. 소환사 프로필 저장
4. `summoner-service` 가 `match-service` 동기화 API 호출
5. `match-service` 가 Match-V5로 최근 경기 상세 수집
6. 매치/참가자 데이터를 저장
7. `search-service` 가 `match_participant` 기반 검색 문서를 색인
8. 이후 통계 / AI 서비스가 이를 소비

---

## 4. 현재 구현 상태

### 완료

- 멀티모듈 프로젝트 구성
- `match-service` 분리
- `api-gateway` 라우팅
- 게이트웨이 CORS 설정
- 게이트웨이 통합 Swagger
- 소환사 프로필 저장/조회
- 매치 상세 저장/조회
- 참가자 상세 스탯 저장
- Data Dragon / queues 기반 표시값 변환
- `search-service` 생성
- participant 기반 Elasticsearch 색인
- 아이템 / 룬 / 스펠 / 딜량 / 골드 / CS / 시야 검색 API

### 다음 단계

- 개인 플레이 패턴 분석 API
- AI 회고 / 코칭 서비스
- Kafka 기반 비동기 처리

---

## 5. 기술 스택

### Backend

- Java 17
- Spring Boot
- Spring Cloud Gateway
- Spring Data JPA
- Spring Data Elasticsearch

### Data

- H2 (local runtime)
- PostgreSQL (target)
- Elasticsearch (local/runtime for search)

### Infra

- Caffeine Cache
- Kafka (planned)

### AI

- OpenAI API (planned)

---

## 6. 운영 진입점

- Gateway: `http://localhost:18000`
- Swagger UI: `http://localhost:18000/swagger-ui.html`

---

## 7. 한 줄 요약

> 현재 RiftMind는 `summoner-service + match-service + search-service + api-gateway` 기반으로 전적 원천 데이터를 저장/검색하고, 다음 단계로 개인 회고/코칭 기능을 확장하는 단계다.
