# RiftMind

LoL 전적 데이터 기반 AI 분석 · 메타 탐색 플랫폼 백엔드 프로젝트.

현재는 `모노레포 멀티모듈` 구조로 관리하며, 기능이 안정화된 뒤 점진적으로 MSA를 확장하는 방향을 따른다.

## Modules

- `api-gateway`: 외부 진입점, 라우팅, 향후 공통 필터/정책
- `summoner-service`: Riot API 연동, 전적 수집/저장/조회
- `common`: 아주 얇은 공통 유틸/포맷 전용

## Docs

### Core

- [Development Rules](./docs/development-rules.md)
- [Platform Overview](./docs/common/platform-overview.md)
- [Development Roadmap](./docs/common/development-roadmap.md)
- [Riot API Flow](./docs/common/riot-api-flow.md)

### Service Design

- [Summoner Service Design](./docs/summoner-service-design.md)

## Current Direction

- Git strategy: monorepo + task-based branches
- Architecture strategy: single solid service first, then real service split
- Primary user identifier: `Riot ID (gameName + tagLine)`
- Internal identifier: `PUUID`
- Current backend priority: `summoner-service`

## Local Development

### Summoner Service

Current defaults:

- local runtime DB: `H2`
- target production DB: `PostgreSQL`
- cache: `Caffeine`

Run tests:

```powershell
.\mvnw.cmd -pl summoner-service test
```

Root Maven wrapper is the canonical build entrypoint for the monorepo.

## Agent Rule

Repository automation or coding agents should follow:

- [AGENT.md](./AGENT.md)

and treat [Development Rules](./docs/development-rules.md) as the primary project rule document.
