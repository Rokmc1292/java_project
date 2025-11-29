package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 예측 코멘트 추천/비추천 엔티티
 * - 사용자가 다른 사용자의 예측 코멘트를 추천/비추천
 * - 1인 1회 제한 (UNIQUE 제약)
 */
@Entity
@Table(name = "prediction_votes")
@Getter
@Setter
public class PredictionVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_id")
    private Long voteId; // 투표 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prediction_id", nullable = false)
    private Prediction prediction; // 예측 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 투표한 사용자

    @Column(name = "vote_type", nullable = false, length = 10)
    private String voteType; // 투표 타입 (LIKE, DISLIKE)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 투표 생성일

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }
}