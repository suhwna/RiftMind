# RiftMind

LoL 전적 데이터 기반 AI 분석 · 메타 탐색 플랫폼.

RiftMind는 Riot API로 수집한 소환사/매치 데이터를 서비스별 저장소와 Elasticsearch에 누적하고, 누적된 검색/분석 기준을 OpenAI 기반 경기 회고에 활용하는 것을 목표로 한다.

## Modules

- `api-gateway`: 외부 진입점, 서비스 라우팅, CORS, 통합 Swagger UI
- `summoner-service`: Riot ID 기반 소환사 프로필 동기화 및 조회
- `match-service`: 매치 상세 수집, 저장, 조회
- `search-service`: Elasticsearch 색인, 조건 검색, 분석 기준 API
- `ai-service`: OpenAI 기반 경기 회고/코칭 API
- `frontend`: React 기반 웹 클라이언트
- `common`: 얇은 공통 유틸/포맷 전용 모듈

## Current Direction

- Git strategy: monorepo + task-based branches
- Architecture strategy: API Gateway + service-per-domain split
- Primary user identifier: `Riot ID (gameName + tagLine)`
- Internal identifier: `PUUID`
- Product direction: 단순 전적 조회보다 개인 회고/코칭 중심
- Current backend priority: 경기 데이터 누적 -> 검색/분석 기준 고도화 -> AI 회고/코칭

Detailed feature, architecture, service, product, and engineering documents are organized in [docs/README.md](./docs/README.md).

## Local Development

### Environment

Create a local `.env` file:

```powershell
Copy-Item .env.example .env
```

Set required keys:

```env
RIOT_API_KEY=RGAPI-...
OPENAI_API_KEY=sk-...
OPENAI_MODEL=gpt-5.2
```

`.env` is ignored by git. Do not commit real API keys.

### Infrastructure

Start PostgreSQL:

```powershell
docker compose -f docker-compose.db.yml up -d
```

Start Elasticsearch and Kibana:

```powershell
docker compose -f docker-compose.search.yml up -d
```

Infrastructure ports:

- PostgreSQL: `localhost:15432`
- Elasticsearch: `http://localhost:19200`
- Kibana: `http://localhost:15601`

PostgreSQL default connection:

- username: `riftmind`
- password: `riftmind`
- summoner DB: `riftmind_summoner`
- match DB: `riftmind_match`

### Backend

Start or restart all backend services with `.env`:

```powershell
.\scripts\start-local-backend-env.ps1 -Restart
```

Start backend services without gateway:

```powershell
.\scripts\start-local-backend-env.ps1 -NoGateway -Restart
```

Preview commands without starting services:

```powershell
.\scripts\start-local-backend-env.ps1 -DryRun
```

Run tests:

```powershell
.\mvnw.cmd -q test
```

Service ports:

- `api-gateway`: `18000`
- `summoner-service`: `18080`
- `match-service`: `18081`
- `search-service`: `18082`
- `ai-service`: `18083`

Swagger UI:

```text
http://localhost:18000/swagger-ui.html
```

Database ownership:

- `summoner-service`: PostgreSQL DB `riftmind_summoner`
- `match-service`: PostgreSQL DB `riftmind_match`
- `search-service`: Elasticsearch index `match-search-v3`
- `ai-service`: no runtime DB
- `api-gateway`: no runtime DB

Root Maven wrapper is the canonical build entrypoint for the backend monorepo.

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

## Quick Check

After starting infrastructure and backend services, verify the main ports:

```powershell
cmd /c netstat -ano | findstr ":18000 :18080 :18081 :18082 :18083 :15432"
```

Test summoner sync through the gateway:

```http
POST http://localhost:18000/api/v1/summoners/sync
Content-Type: application/json

{
  "gameName": "Hide on bush",
  "tagLine": "KR1",
  "matchCount": 1
}
```

## Agent Rule

Repository automation or coding agents should follow:

- [AGENT.md](./AGENT.md)
