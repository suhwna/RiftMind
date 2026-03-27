# RiftMind

LoL 전적 데이터 기반 AI 분석 · 메타 탐색 플랫폼 백엔드 프로젝트.

현재는 `모노레포 멀티모듈` 구조로 관리하며, 기능이 안정화된 뒤 점진적으로 MSA를 확장하는 방향을 따른다.

## Modules

- `api-gateway`: 외부 진입점, 라우팅, 향후 공통 필터/정책
- `summoner-service`: Riot ID 기반 소환사 프로필 동기화 및 조회
- `match-service`: 매치 상세 수집, 저장, 조회
- `search-service`: Elasticsearch 색인 및 검색 API
- `frontend`: React 기반 웹 클라이언트
- `common`: 아주 얇은 공통 유틸/포맷 전용

## Current Direction

- Git strategy: monorepo + task-based branches
- Architecture strategy: single solid service first, then real service split
- Primary user identifier: `Riot ID (gameName + tagLine)`
- Internal identifier: `PUUID`
- Product direction: `전적 조회 서비스` 보다 `개인 회고/코칭 서비스`
- Current backend priority: `participant 기반 검색 안정화 -> 패턴 분석 -> AI 회고/코칭`

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

Start only backend services without gateway:

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

Local Elasticsearch / Kibana:

```powershell
.\scripts\start-local-search.ps1
```

- Elasticsearch: `http://localhost:19200`
- Kibana: `http://localhost:15601`

Stop local search stack:

```powershell
.\scripts\stop-local-search.ps1
```

Service ports:

- `api-gateway`: `18000`
- `summoner-service`: `18080`
- `match-service`: `18081`
- `search-service`: `18082`

Runtime DB files:

- `summoner-service`: `data/riftmind.mv.db`
- `match-service`: `data/riftmind-match.mv.db`

Root Maven wrapper is the canonical build entrypoint for the monorepo.

### Frontend

Install dependencies:

```powershell
cd frontend
npm install
```

Run the local dev server:

```powershell
cd frontend
npm run dev
```

Build the frontend:

```powershell
cd frontend
npm run build
```

Frontend defaults:

- dev server: `http://localhost:3000`
- backend gateway base URL: `http://localhost:18000`

If needed, override the API base URL with `VITE_API_BASE_URL`.

## Agent Rule

Repository automation or coding agents should follow:

- [AGENT.md](./AGENT.md)
