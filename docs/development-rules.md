# RiftMind 공통 개발 규칙

## Related Docs

- [Platform Overview](./common/platform-overview.md)
- [Development Roadmap](./common/development-roadmap.md)
- [Riot API Flow](./common/riot-api-flow.md)
- [Summoner Service Design](./summoner-service-design.md)
- [Search Service Design](./search-service-design.md)

## 1. 목적

이 문서는 RiftMind 프로젝트의 공통 개발 기준을 정의한다.
구현 중 설계가 흔들리거나 작업 방식이 엇갈리지 않도록
브랜치 전략, 서비스 경계, 문서 규칙, 개발 환경 원칙을 고정한다.

---

## 2. 저장소 운영 원칙

- 현재 프로젝트는 `모노레포 멀티모듈` 구조로 관리한다.
- `api-gateway`, `summoner-service`, `match-service`, `search-service`, `common` 은 하나의 Git 저장소 안에서 관리한다.
- MSA를 지향하더라도 현재 단계에서는 서비스별 Git 저장소로 분리하지 않는다.
- 서비스별 브랜치를 상시 운영하지 않는다.
- Maven wrapper는 루트에서 하나만 관리한다.

---

## 3. 브랜치 전략

### 기본 원칙

- 브랜치는 `서비스 기준` 이 아니라 `작업/기능 기준` 으로 생성한다.
- `main` 은 항상 비교적 안정된 상태를 유지한다.
- 새 작업은 `main` 에서 분기한 뒤 완료되면 `main` 으로 병합한다.
- 병합 후 작업 브랜치는 삭제한다.

### 브랜치 예시

- `feature/summoner-service-bootstrap`
- `feature/riot-account-client`
- `feature/match-sync`
- `feature/gateway-routing`
- `fix/riot-api-timeout`
- `docs/api-flow-update`

### 금지/비권장

- `summoner-service`, `match-service`, `api-gateway` 같은 영구 서비스 브랜치 운영
- 하나의 브랜치를 너무 오래 유지
- 테스트되지 않은 상태를 바로 `main` 에 직접 커밋

---

## 4. 서비스 경계 원칙

### api-gateway

- 외부 진입점
- 라우팅
- 향후 인증/필터/공통 정책 담당

### summoner-service

- Riot ID 기준 계정/소환사 프로필 동기화
- Riot ID -> PUUID 변환
- 소환사 프로필 저장
- `match-service` 호출 진입점
- 저장된 프로필 조회 API 제공

### match-service

- Match-V5 상세 수집
- 매치/참가자 데이터 저장
- 저장된 경기/참가자 조회 API 제공
- 표시용 정적 데이터 변환

### search-service

- Elasticsearch 색인
- participant 기반 검색 문서 생성
- 조건 검색 API 제공
- 검색 전용 필드 구조 관리

### 추후 분리 대상

- `ai-service`: LLM 분석/코칭
- `ingest-service`: 비동기 수집/색인

### 금지 원칙

- `summoner-service` 에 검색/AI 책임을 같이 넣지 않는다.
- `match-service` 에 소환사 프로필 책임을 같이 넣지 않는다.
- `search-service` 에 Riot 직접 수집 책임을 같이 넣지 않는다.
- 서비스 간 경계가 흐려지는 공용 도메인 설계를 피한다.

---

## 5. common 모듈 규칙

- `common` 은 아주 얇게 유지한다.
- 넣어도 되는 것:
  - 공통 예외 포맷
  - 공통 응답 포맷
  - 범용 유틸
  - 단순 상수
- 넣으면 안 되는 것:
  - 엔티티
  - 서비스별 도메인 모델
  - 서비스 전용 DTO
  - 서비스 핵심 비즈니스 로직

`common` 이 비대해지면 MSA 서비스 경계가 무너진다.

---

## 6. API 설계 원칙

- API는 먼저 명세를 문서로 정의한 뒤 구현한다.
- 조회 API와 동기화 API를 분리한다.
- 외부 Riot API 호출 결과를 그대로 노출하지 않고 내부 응답 형태로 정제한다.
- 사용자 입력 기준은 `Riot ID(gameName + tagLine)` 이다.
- 내부 식별 기준은 `PUUID` 이다.
- `summonerName` 기반 조회는 주 경로로 사용하지 않는다.

---

## 7. DB 설계 원칙

- 초기 구현은 JPA 중심으로 진행한다.
- 로컬 개발/테스트는 `H2` 를 사용할 수 있다.
- 목표 운영 DB는 `PostgreSQL` 이다.
- H2 사용 시에도 PostgreSQL 전환 가능성을 항상 고려한다.
- 서비스가 분리되면 DB도 서비스 소유 원칙에 맞게 분리한다.
- 다른 서비스 DB를 직접 조회하는 방식은 지양한다.

---

## 8. 캐시 및 외부 호출 원칙

- Riot API는 rate limit이 있으므로 캐시 전략을 고려한다.
- 초기 캐시는 애플리케이션 내부 메모리 캐시(`Caffeine`) 를 사용한다.
- 같은 Riot ID/PUUID/matchId에 대한 중복 호출을 줄인다.
- 외부 API 호출은 서비스 계층이 직접 흩어져 호출하지 않고 전용 client/gateway 계층으로 모은다.

---

## 9. 문서 규칙

- `docs` 디렉터리 문서 파일명은 ASCII 기반 영문으로 관리한다.
- 설계 변경 시 코드보다 문서를 먼저 또는 함께 갱신한다.
- 최소 유지 문서:
  - 플랫폼 개요
  - 개발 로드맵
  - Riot API 흐름
  - 서비스별 설계 문서

### Javadoc 작성 규칙

- 공개 API는 Javadoc을 기본으로 작성한다.
  - 대상: Controller, public Service 메서드, 외부 연동 Client
- 설명은 `무엇을 한다`보다 `어떤 책임을 가지는지`와 `왜 필요한지`를 우선한다.
- DTO `from(...)` 같은 단순 변환 메서드는 목적만 짧게 설명한다.
- private helper는 로직이 복잡하거나 의도가 바로 드러나지 않을 때만 작성한다.
- getter처럼 이름만으로 의미가 충분한 자명한 메서드에는 Javadoc을 반복 작성하지 않는다.

---

## 10. 커밋 규칙

- 의미 있는 단위로 자주 커밋한다.
- 초기 접두사는 아래 형식을 권장한다.
  - `chore`
  - `feature`
  - `fix`
  - `docs`
  - `refactor`
- 커밋 메시지는 짧고 목적이 드러나게 작성한다.

예시:

- `chore: RiftMind 프로젝트 초기 설정`
- `feature: summoner-service 기본 구조 추가`
- `docs: summoner-service 설계 문서 추가`

---

## 11. 개발 진행 원칙

- 기능 구현 전에 책임 범위와 API/DB 설계를 먼저 확정한다.
- 동작하는 최소 단위를 먼저 만들고 점진적으로 확장한다.
- 검색 기능을 먼저 안정화한 뒤 AI 기능을 추가한다.
- 비동기 처리와 Kafka는 필요한 시점에 도입한다.
- 관측성/운영성은 서비스 분리가 의미 있을 때 확장한다.
- 모듈 테스트/빌드는 루트 wrapper 기준으로 실행한다.

---

## 12. 현재 단계 기준 우선순위

1. `summoner-service` / `match-service` 경계 유지
2. Riot API 연동
3. DB 저장/조회 안정화
4. `search-service` 검색 정확도 / 문서 구조 안정화
5. `ai-service` 분리
6. Kafka/관측성 확장

---

## 13. 한 줄 원칙

지금 RiftMind는 `기능이 있는 단단한 서비스 하나를 먼저 만들고, 필요해질 때 진짜 MSA로 분리한다`.
