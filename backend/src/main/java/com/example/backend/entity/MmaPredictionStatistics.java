package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * MMA 예측 통계 엔티티
 * - MMA 경기별 예측 투표 통계를 저장
 * - 파이터1 승, 파이터2 승 각각의 투표 수
 */
@Entity
@Table(name = "mma_prediction_statistics")
@Getter
@Setter
public class MmaPredictionStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_id")
    private Long statId; // 통계 고유 ID

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fight_id", nullable = false, unique = true)
    private MmaFight fight; // MMA 경기 정보

    @Column(name = "fighter1_votes", nullable = false)
    private Integer fighter1Votes = 0; // 파이터1 승 예측 수

    @Column(name = "fighter2_votes", nullable = false)
    private Integer fighter2Votes = 0; // 파이터2 승 예측 수

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
