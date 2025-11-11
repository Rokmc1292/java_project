package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 예측 통계 DTO
 * - 경기별 예측 투표 비율 정보
 * - 실시간 투표 비율 표시에 사용
 */
@Getter
@Setter
public class PredictionStatisticsDto {
    private Long matchId; // 경기 ID
    private Integer homeVotes; // 홈팀 승 예측 수
    private Integer drawVotes; // 무승부 예측 수
    private Integer awayVotes; // 원정팀 승 예측 수
    private Integer totalVotes; // 전체 예측 수
    private Double homePercentage; // 홈팀 승 예측 비율 (%)
    private Double drawPercentage; // 무승부 예측 비율 (%)
    private Double awayPercentage; // 원정팀 승 예측 비율 (%)
}