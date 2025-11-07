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

    // 예상 점수 (배당률 방식)
    private Integer homeWinPoints; // 홈팀 승 적중 시 획득 점수
    private Integer homeLosePoints; // 홈팀 승 실패 시 감점
    private Integer drawWinPoints; // 무승부 적중 시 획득 점수
    private Integer drawLosePoints; // 무승부 실패 시 감점
    private Integer awayWinPoints; // 원정팀 승 적중 시 획득 점수
    private Integer awayLosePoints; // 원정팀 승 실패 시 감점
}