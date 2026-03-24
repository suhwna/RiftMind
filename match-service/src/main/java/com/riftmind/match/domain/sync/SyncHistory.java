package com.riftmind.match.domain.sync;

import com.riftmind.match.domain.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 소환사 동기화 이력을 저장하는 엔티티입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Entity
@Table(name = "sync_history")
public class SyncHistory extends BaseTimeEntity {

    /**
     * 동기화 처리 상태를 나타냅니다.
     */
    public enum SyncStatus {
        SUCCESS,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String puuid;

    @Column(name = "requested_match_count", nullable = false)
    private int requestedMatchCount;

    @Column(name = "saved_match_count", nullable = false)
    private int savedMatchCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncStatus status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    protected SyncHistory() {
    }

    private SyncHistory(
            String puuid,
            int requestedMatchCount,
            int savedMatchCount,
            SyncStatus status,
            String errorMessage) {
        this.puuid = puuid;
        this.requestedMatchCount = requestedMatchCount;
        this.savedMatchCount = savedMatchCount;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    /**
     * 성공 이력을 생성합니다.
     *
     * @param puuid Riot PUUID
     * @param requestedMatchCount 요청한 경기 수
     * @param savedMatchCount 저장한 경기 수
     * @return 성공 이력 엔티티
     */
    public static SyncHistory success(String puuid, int requestedMatchCount, int savedMatchCount) {
        return new SyncHistory(puuid, requestedMatchCount, savedMatchCount, SyncStatus.SUCCESS, null);
    }

    /**
     * 실패 이력을 생성합니다.
     *
     * @param puuid Riot PUUID
     * @param requestedMatchCount 요청한 경기 수
     * @param errorMessage 오류 메시지
     * @return 실패 이력 엔티티
     */
    public static SyncHistory failure(String puuid, int requestedMatchCount, String errorMessage) {
        return new SyncHistory(puuid, requestedMatchCount, 0, SyncStatus.FAILED, errorMessage);
    }
}
