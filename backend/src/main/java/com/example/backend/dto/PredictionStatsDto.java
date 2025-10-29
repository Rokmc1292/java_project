package com.example.backend.dto;

import lombok.Data;

/**
 * 승부예측 통계 DTO
 * 총 예측 횟수, 성공/실패 수, 승률
 */
@Data
public class PredictionStatsDto {
    private Integer totalPredictions;       // 총 예측 횟수
    private Integer correctPredictions;     // 맞춘 예측 수
    private Integer incorrectPredictions;   // 틀린 예측 수
    private Double winRate;                 // 승률 (백분율)
}