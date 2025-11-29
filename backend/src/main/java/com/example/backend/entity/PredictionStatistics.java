package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 예측 통계 엔티티
 * - 경기별 예측 투표 통계를 저장
 * - 홈승, 무승부, 원정승 각각의 투표 수
 */
@Entity
@Table(name = "prediction_statistics")
@Getter
@Setter
public class PredictionStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_id")
    private Long statId; // 통계 고유 ID

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private Match match; // 경기 정보

    @Column(name = "home_votes", nullable = false)
    private Integer homeVotes = 0; // 홈팀 승 예측 수

    @Column(name = "draw_votes", nullable = false)
    private Integer drawVotes = 0; // 무승부 예측 수

    @Column(name = "away_votes", nullable = false)
    private Integer awayVotes = 0; // 원정팀 승 예측 수

    @Column(name = "total_votes", nullable = false)
    private Integer totalVotes = 0; // 전체 예측 수

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 통계 업데이트 시간

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }
}