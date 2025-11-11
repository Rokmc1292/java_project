package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 경기 정보 DTO
 * 프론트엔드로 경기 데이터를 전달하기 위한 객체
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchDto {

    // 경기 기본 정보
    private Long matchId;              // 경기 ID
    private String sportType;          // 종목 타입 (FOOTBALL, BASKETBALL, BASEBALL, LOL, MMA)

    // 리그 정보
    private LeagueInfo league;

    // 팀/파이터 정보
    private TeamInfo teams;

    // 경기 상세 정보
    private MatchDetail detail;

    // 점수 정보
    private Score score;

    /**
     * 리그 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeagueInfo {
        private Long leagueId;         // 리그 ID
        private String name;           // 리그 이름
        private String country;        // 국가
        private String logo;           // 리그 로고 URL
    }

    /**
     * 팀/파이터 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamInfo {
        private Team home;             // 홈팀 또는 파이터1
        private Team away;             // 원정팀 또는 파이터2
    }

    /**
     * 개별 팀/파이터
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Team {
        private Long id;               // 팀/파이터 ID
        private String name;           // 팀/파이터 이름
        private String logo;           // 로고/사진 URL
        private String country;        // 국가
        private String weightClass;    // 체급 (UFC만)
        private String record;         // 전적 (UFC만)
    }

    /**
     * 경기 상세 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchDetail {
        private LocalDateTime matchDate;  // 경기 날짜 및 시간
        private String venue;             // 경기장
        private String status;            // 경기 상태 (SCHEDULED, LIVE, FINISHED)
        private String eventName;         // 이벤트 이름 (UFC만)
        private String winner;            // 승자 이름 (UFC 종료 경기만)
        private String method;            // 승리 방법 (UFC만)
        private Integer round;            // 라운드 (UFC만)
    }

    /**
     * 점수 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Score {
        private Integer home;          // 홈팀 점수
        private Integer away;          // 원정팀 점수
    }
}