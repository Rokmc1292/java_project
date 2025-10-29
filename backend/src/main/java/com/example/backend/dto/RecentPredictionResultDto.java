package com.example.backend.dto;

import lombok.Data;

/**
 * 최근 10경기 예측 결과 DTO
 * O/X 표시용
 */
@Data
public class RecentPredictionResultDto {
    private Long predictionId;              // 예측 ID
    private Long matchId;                   // 경기 ID
    private String homeTeam;                // 홈팀 이름
    private String awayTeam;                // 원정팀 이름
    private String predictedResult;         // 예측 결과 (HOME, DRAW, AWAY)
    private Boolean isCorrect;              // 정답 여부
}