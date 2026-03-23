package com.riftmind.summoner.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.riftmind.summoner.domain.sync.SyncHistory;

/**
 * 동기화 이력 저장소입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
public interface SyncHistoryRepository extends JpaRepository<SyncHistory, Long> {
}
