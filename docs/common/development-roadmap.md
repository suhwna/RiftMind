# 🚀 RiftMind 개발 로드맵 (개선판)

## Related Docs

- [Platform Overview](./platform-overview.md)
- [Development Rules](../development-rules.md)
- [Riot API Flow](./riot-api-flow.md)
- [Summoner Service Design](../summoner-service-design.md)

---

## 📌 0. 개발 전략

- 단일 구조 → 점진적 MSA 전환
- 동기 처리 → 비동기 처리 확장
- 기능 구현 → 구조 고도화 순서
- 검색 기능 우선 → AI 기능 후순위

---

## ✅ 1주차 — 기반 구축 & Riot API 연동

### 🎯 목표
데이터를 외부에서 가져올 수 있는 상태 만들기

### 📌 작업
- 멀티모듈 프로젝트 구성
  - common
  - summoner-service
  - api-gateway (선택)
- Riot API 연동
  - 소환사 조회
  - 매치 ID 목록 조회

### ✅ 산출물
- `/summoners/{name}` API 동작
- Riot API 정상 호출
- 상태: 완료

---

## ✅ 2주차 — 매치 데이터 수집 & 저장

### 🎯 목표
외부 데이터를 내부 시스템에 저장

### 📌 작업
- match-service 생성
- 매치 상세 데이터 수집
- 데이터 정규화
  - KDA
  - 챔피언
  - 포지션
- PostgreSQL 테이블 설계
- 데이터 저장 API 구현

### ✅ 산출물
- 매치 데이터 DB 저장
- 재조회 가능
- 상태: 완료
- 비고:
  - `match-service` 분리 완료
  - `api-gateway` 라우팅 / CORS / 통합 Swagger 구성 완료
  - `summoner-service` 는 소환사 프로필 중심 서비스로 역할 축소

---

## 🟡 3주차 — Elasticsearch 검색 구축 (진행 중)

### 🎯 목표
검색 기능 구현 (프로젝트 핵심)

### 📌 작업
- Elasticsearch 환경 구성
- 데이터 색인 구조 설계
- search-service 생성
- 검색 API 구현
  - 챔피언 필터
  - KDA 필터
  - 승패 필터
  - 아이템 / 룬 / 스펠 필터
  - 딜량 / 골드 / CS / 시야 필터

### ✅ 산출물
- `/search/matches` API
- 조건 검색 가능
- 상태: 진행 중
- 비고:
  - `search-service` 생성 완료
  - Elasticsearch / Kibana 로컬 실행 환경 구성 완료
  - `api-gateway` 라우팅 / 통합 Swagger 연결 완료
  - 초기 summary 기반 색인 구현 후 `MATCH_PARTICIPANT` 기반 색인으로 전환 완료
  - 현재 인덱스: `match-search-v3`
- 선행 조건:
  - 검색 정확도/응답 형태 polish
  - README / 설계 문서 최종 정리

---

## 🟢 4주차 — 플레이 패턴 분석

### 🎯 목표
최근 경기 데이터를 기반으로 개인 플레이 패턴을 분석

### 📌 작업
- Elasticsearch Aggregation 쿼리
- 최근 10판 기준 승/패 패턴 분석
- 챔피언 / 포지션별 성과 차이 분석
- 반복 습관 탐지
  - 데스
  - 시야
  - CS
  - 딜량
- 회고용 기초 통계 API 구현

### ✅ 산출물
- `/stats/patterns`
- `/stats/champions`
- 최근 경기 기준 개인 패턴 요약

---

## 🟢 5주차 — AI 회고 / 코칭

### 🎯 목표
플레이 데이터를 자연어 회고와 개선 포인트로 변환

### 📌 작업
- ai-service 생성
- Elasticsearch 기반 RAG 구성
- 경기 회고 카드 프롬프트 설계
- 잘한 점 / 아쉬운 점 / 개선 포인트 생성
- 최근 N판 패턴 요약 생성
- 코칭형 응답 API 구현

### ✅ 산출물
- `/ai/analyze` API
- 경기 회고 카드
- 최근 10판 개인 코칭 요약

---

## 🟢 6주차 — Kafka 기반 비동기 처리

### 🎯 목표
이벤트 기반 데이터 처리 구조 도입

### 📌 작업
- Kafka 환경 구성
- Producer / Consumer 구현
- 매치 데이터 수집 비동기 처리

### ✅ 산출물
- 비동기 데이터 파이프라인 구축

---

## 🟢 7주차 — API Gateway & 관측성

### 🎯 목표
운영 가능한 시스템 구성

### 📌 작업
- API Gateway (WebFlux) 구성
- 서비스 라우팅 설정
- ELK 스택 구성
- Kibana 대시보드 구축
  - API 호출량
  - 에러율
  - 인기 챔피언

### ✅ 산출물
- Gateway 기반 라우팅
- 모니터링 가능 시스템

---

## 🟢 8주차 — 포트폴리오 완성

### 🎯 목표
외부에 보여줄 수 있는 수준 완성

### 📌 작업
- README 정리
- 아키텍처 다이어그램 작성
- 시연 데이터 구성
- 데모 영상 제작

### ✅ 산출물
- GitHub 프로젝트 완성
- 포트폴리오 제출 가능

---

## 🔥 핵심 흐름 요약

1. Riot API 연동
2. DB 저장
3. Elasticsearch 검색
4. 패턴 분석
5. AI 회고 / 코칭
6. Kafka 비동기 처리

---

## 🎯 성공 기준

- 전적 조회 기능 정상 동작
- 검색 기능 정상 동작
- 개인 패턴 분석 결과 제공 가능
- 경기 회고 / 코칭 결과 생성
- 전체 데이터 흐름 설명 가능

---

## 💡 핵심 전략 요약

> 검색 기반 확보 → 개인 패턴 분석 → AI 회고/코칭 → 비동기 구조 도입

---
