package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 크롤링된 경기 데이터 DTO
 * 크롤러에서 추출한 데이터를 서비스로 전달
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchCrawlDto {

    // 경기 날짜 및 시간
    private LocalDateTime matchDate;

    // 홈팀 이름 (크롤링한 원본 텍스트)
    private String homeTeamName;

    // 원정팀 이름 (크롤링한 원본 텍스트)
    private String awayTeamName;

    // 홈팀 점수
    private Integer homeScore;

    // 원정팀 점수
    private Integer awayScore;

    // 경기 상태 (SCHEDULED, LIVE, FINISHED)
    private String status;

    // 경기장 이름
    private String venue;
}