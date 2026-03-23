# 🎮 RiftMind

> LoL 전적 데이터 기반 AI 분석 · 메타 탐색 플랫폼

---

## 🚀 Overview

RiftMind는 Riot API 기반 전적 데이터를 수집하고  
Elasticsearch 검색엔진과 LLM을 결합하여  
플레이 분석 및 AI 코칭 기능을 제공하는 백엔드 시스템입니다.

단순 전적 조회를 넘어 검색, 데이터 파이프라인, AI 분석을 통합한 구조를 목표로 합니다.

---

## 🔥 Features

### 🔍 전적 검색
- 소환사 조회 및 매치 기록 조회
- 챔피언 / KDA / 승패 기반 조건 검색

### 📊 메타 분석
- 챔피언 승률 및 포지션 통계
- Elasticsearch Aggregation 활용

### 🤖 AI 코칭
- 최근 경기 기반 플레이 분석
- 문제점 도출 및 개선 방향 제시
- RAG 기반 데이터 중심 분석

---

## 🏗 Architecture

```
Client
  ↓
API Gateway
  ↓
----------------------------
| Summoner Service         |
| Match Service            |
| Search Service           |
| AI Service               |
----------------------------
  ↓
PostgreSQL / Elasticsearch / Kafka
```

---

## ⚙️ Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring Cloud Gateway

### Data
- PostgreSQL
- Elasticsearch
- Kibana

### Messaging
- Kafka

### AI
- OpenAI API (RAG)

---

## 🔄 Data Flow

1. Riot API → 매치 데이터 수집
2. DB 저장 (PostgreSQL)
3. Kafka → 이벤트 처리
4. Elasticsearch → 검색/집계
5. LLM → 분석 및 코칭 생성

---

## 💡 Key Points

- Elasticsearch 기반 검색 시스템 구현
- Kafka 기반 비동기 데이터 처리
- LLM + RAG 기반 AI 분석 기능
- MSA 아키텍처 설계 및 적용

---

## 🎯 Goal

> 검색 · 데이터 · AI를 결합한 실무형 백엔드 시스템 구현

---

## 📌 Status

- [ ] Riot API 연동
- [ ] 매치 데이터 저장
- [ ] Elasticsearch 검색 구현
- [ ] AI 분석 기능 추가
- [ ] Kafka 비동기 처리

---

## 🧠 What I Learned (예정)

- MSA 서비스 분리 전략
- Elasticsearch 인덱스 설계
- 이벤트 기반 데이터 흐름 설계
- LLM + RAG 구조 적용

---

## 🔥 한 줄 요약

> 전적 데이터를 검색하고, AI가 분석해주는 시스템