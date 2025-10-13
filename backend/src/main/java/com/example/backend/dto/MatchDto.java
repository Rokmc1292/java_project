package com.example.backend.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 경기 정보 DTO
 * 프론트엔드로 전달할 경기 데이터
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDto {

    private Long matchId;
    private String sport;

    // 리그 정보
    private LeagueInfo league;

    // 팀 정보
    private TeamInfo teams;

    // 경기 상태
    private MatchStatus fixture;

    // 점수
    private Score goals;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LeagueInfo {
        private String name;
        private String country;
        private String logo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamInfo {
        private Team home;
        private Team away;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Team {
        private String name;
        private String logo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MatchStatus {
        private Long id;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime date;

        private Status status;
        private Venue venue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Status {
        private String shortStatus;
        private String longStatus;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Venue {
        private String name;
        private String city;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Score {
        private Integer home;
        private Integer away;
    }
}