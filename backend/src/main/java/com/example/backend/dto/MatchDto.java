package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 경기 정보 DTO
 * 모든 종목의 경기 정보를 통일된 형식으로 반환
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchDto {

    private Long matchId;              // 경기 고유 ID
    private String sport;              // 종목 (FOOTBALL, BASKETBALL, BASEBALL, ESPORTS)
    private LeagueInfo league;         // 리그 정보
    private TeamInfo teams;            // 팀 정보
    private MatchStatus fixture;       // 경기 상태
    private Score goals;               // 점수

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LeagueInfo {
        private String name;           // 리그 이름
        private String country;        // 국가
        private String logo;           // 리그 로고
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TeamInfo {
        private Team home;             // 홈팀
        private Team away;             // 원정팀
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Team {
        private String name;           // 팀 이름
        private String logo;           // 팀 로고
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MatchStatus {
        private Long id;               // 경기 ID
        private LocalDateTime date;    // 경기 날짜/시간
        private Status status;         // 경기 상태
        private Venue venue;           // 경기장
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Status {
        private String shortStatus;    // 짧은 상태 (예: FT, NS, LIVE)
        private String longStatus;     // 긴 상태 (예: Match Finished)
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Venue {
        private String name;           // 경기장 이름
        private String city;           // 도시
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Score {
        private Integer home;          // 홈팀 점수
        private Integer away;          // 원정팀 점수
    }
}