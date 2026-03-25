package com.riftmind.match.domain.match;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.riftmind.match.domain.common.BaseTimeEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * 매치 기본 정보와 참가자 목록을 저장하는 엔티티입니다.
 *
 * @author 정수환
 * @since 2026-03-23
 */
@Entity
@Table(name = "match_summary")
public class MatchSummary extends BaseTimeEntity {

    @Id
    @Column(name = "match_id", length = 30, nullable = false)
    private String matchId;

    @Column(name = "game_creation", nullable = false)
    private LocalDateTime gameCreation;

    @Column(name = "game_duration", nullable = false)
    private Integer gameDuration;

    @Column(name = "queue_id", nullable = false)
    private Integer queueId;

    @Column(name = "game_mode", length = 30, nullable = false)
    private String gameMode;

    @Column(name = "game_version", length = 20, nullable = false)
    private String gameVersion;

    @OneToMany(mappedBy = "matchSummary", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MatchParticipant> participants = new ArrayList<>();

    protected MatchSummary() {
    }

    public MatchSummary(String matchId) {
        this.matchId = matchId;
    }

    /**
     * 매치 기본 정보를 갱신합니다.
     *
     * @param gameCreation 게임 시작 시각
     * @param gameDuration 게임 길이
     * @param queueId 큐 ID
     * @param gameMode 게임 모드
     * @param gameVersion 게임 버전
     */
    public void updateSummary(
            LocalDateTime gameCreation,
            Integer gameDuration,
            Integer queueId,
            String gameMode,
            String gameVersion) {
        this.gameCreation = gameCreation;
        this.gameDuration = gameDuration;
        this.queueId = queueId;
        this.gameMode = gameMode;
        this.gameVersion = gameVersion;
    }

    /**
     * 매치 참가자 목록을 새 목록으로 교체합니다.
     *
     * @param newParticipants 새 참가자 목록
     */
    public void replaceParticipants(List<MatchParticipant> newParticipants) {
        this.participants.clear();
        for (MatchParticipant participant : newParticipants) {
            participant.assignTo(this);
            this.participants.add(participant);
        }
    }

    public String getMatchId() {
        return matchId;
    }

    public LocalDateTime getGameCreation() {
        return gameCreation;
    }

    public Integer getGameDuration() {
        return gameDuration;
    }

    public Integer getQueueId() {
        return queueId;
    }

    public String getGameMode() {
        return gameMode;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public List<MatchParticipant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }
}
