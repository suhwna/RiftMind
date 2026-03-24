# RiftMind

LoL 전적 데이터 기반 AI 분석 · 메타 탐색 플랫폼 백엔드 프로젝트.

현재는 `모노레포 멀티모듈` 구조로 관리하며, 기능이 안정화된 뒤 점진적으로 MSA를 확장하는 방향을 따른다.

## Modules

- `api-gateway`: 외부 진입점, 라우팅, 향후 공통 필터/정책
- `summoner-service`: Riot ID 기반 소환사 프로필 동기화 및 조회
- `match-service`: 매치 상세 수집, 저장, 조회
- `common`: 아주 얇은 공통 유틸/포맷 전용

## Docs

### Core

- [Development Rules](./docs/development-rules.md)
- [Platform Overview](./docs/common/platform-overview.md)
- [Development Roadmap](./docs/common/development-roadmap.md)
- [Riot API Flow](./docs/common/riot-api-flow.md)

### Service Design

- [Summoner Service Design](./docs/summoner-service-design.md)
- [Match Service Design](./docs/match-service-design.md)

## Current Direction

- Git strategy: monorepo + task-based branches
- Architecture strategy: single solid service first, then real service split
- Primary user identifier: `Riot ID (gameName + tagLine)`
- Internal identifier: `PUUID`
- Current backend priority: `match-service` 기반 데이터 축적 -> 검색/통계 확장

## Local Development

### Backend

Current defaults:

- local runtime DB: `H2`
- target production DB: `PostgreSQL`
- cache: `Caffeine`

Run tests:

```powershell
.\mvnw.cmd -q test
```

Start all local backend services:

```powershell
.\scripts\start-local-backend.ps1
```

Start only `summoner-service` + `match-service` without gateway:

```powershell
.\scripts\start-local-backend.ps1 -NoGateway
```

Preview what the script will launch:

```powershell
.\scripts\start-local-backend.ps1 -DryRun
```

Stop local backend services started on the default ports:

```powershell
.\scripts\stop-local-backend.ps1
```

Gateway-integrated Swagger UI:

```text
http://localhost:18000/swagger-ui.html
```

Service ports:

- `api-gateway`: `18000`
- `summoner-service`: `18080`
- `match-service`: `18081`

Runtime DB files:

- `summoner-service`: `data/riftmind.mv.db`
- `match-service`: `data/riftmind-match.mv.db`

Root Maven wrapper is the canonical build entrypoint for the monorepo.

## Agent Rule

Repository automation or coding agents should follow:

- [AGENT.md](./AGENT.md)

and treat [Development Rules](./docs/development-rules.md) as the primary project rule document.
