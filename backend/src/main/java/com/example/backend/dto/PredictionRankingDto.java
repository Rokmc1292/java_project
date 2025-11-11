package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 예측 랭킹 DTO
 * - 전체/종목별 랭킹 정보
 */
@Getter
@Setter
public class PredictionRankingDto {
    private String username; // 사용자 아이디
    private String nickname; // 사용자 닉네임
    private String tier; // 티어
    private Integer tierScore; // 티어 점수
    private Integer totalPredictions; // 총 예측 횟수
    private Integer correctPredictions; // 맞춘 횟수
    private Double accuracy; // 정확도 (%)
    private Integer consecutiveCorrect; // 연속 적중 횟수
    private Integer rank; // 순위
}