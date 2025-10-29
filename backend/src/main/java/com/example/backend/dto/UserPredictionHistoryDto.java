package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 사용자 예측 내역 DTO
 * 진행중/완료된 예측 목록 표시용
 */
@Data
public class UserPredictionHistoryDto {
    private Long predictionId;              // 예측 ID
    private Long matchId;                   // 경기 ID
    private String sportName;               // 종목 이름
    private String homeTeam;                // 홈팀 이름
    private String awayTeam;                // 원정팀 이름
    private String homeTeamLogo;            // 홈팀 로고 URL
    private String awayTeamLogo;            // 원정팀 로고 URL
    private String predictedResult;         // 예측 결과
    private String comment;                 // 예측 코멘트
    private LocalDateTime matchDate;        // 경기 날짜
    private String matchStatus;             // 경기 상태
    private Boolean isCorrect;              // 예측 정답 여부
    private Integer homeScore;              // 홈팀 점수
    private Integer awayScore;              // 원정팀 점수
    private LocalDateTime createdAt;        // 예측 작성일
}