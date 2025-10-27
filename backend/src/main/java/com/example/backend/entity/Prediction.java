package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 승부예측 엔티티
 * - 사용자가 경기 결과를 예측한 정보를 저장
 * - 예측 결과: HOME(홈승), DRAW(무승부), AWAY(원정승)
 */
@Entity
@Table(name = "predictions")
@Getter
@Setter
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prediction_id")
    private Long predictionId; // 예측 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match; // 경기 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 예측한 사용자

    @Column(name = "predicted_result", nullable = false, length = 10)
    private String predictedResult; // 예측 결과 (HOME, DRAW, AWAY)

    @Column(name = "comment", nullable = false, columnDefinition = "TEXT")
    private String comment; // 예측 코멘트 (필수)

    @Column(name = "is_correct")
    private Boolean isCorrect; // 예측 정답 여부 (null: 경기 미종료, true: 정답, false: 오답)

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0; // 코멘트 추천수

    @Column(name = "dislike_count", nullable = false)
    private Integer dislikeCount = 0; // 코멘트 비추천수

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 예측 생성일

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}