package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 승부예측 DTO
 * - 프론트엔드와 데이터 주고받을 때 사용
 * - 예측 정보 + 사용자 정보 포함
 */
@Getter
@Setter
public class PredictionDto {
    private Long predictionId; // 예측 ID
    private Long matchId; // 경기 ID
    private String username; // 사용자 아이디
    private String nickname; // 사용자 닉네임
    private String userTier; // 사용자 티어
    private String predictedResult; // 예측 결과 (HOME, DRAW, AWAY)
    private String comment; // 예측 코멘트
    private Boolean isCorrect; // 예측 정답 여부
    private Integer likeCount; // 추천수
    private Integer dislikeCount; // 비추천수
    private LocalDateTime createdAt; // 생성일
}