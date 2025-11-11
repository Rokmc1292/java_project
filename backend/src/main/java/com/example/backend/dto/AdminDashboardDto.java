package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 대시보드 DTO
 */
@Data
public class AdminDashboardDto {
    // 전체 통계
    private Integer totalUsers;              // 전체 회원 수
    private Integer activeUsers;             // 활성 회원 수
    private Integer todayNewUsers;           // 오늘 신규 가입자
    private Integer totalPosts;              // 전체 게시글 수
    private Integer todayNewPosts;           // 오늘 신규 게시글
    private Integer totalPredictions;        // 전체 예측 수
    private Integer todayNewPredictions;     // 오늘 신규 예측
    private Integer pendingReports;          // 대기 중인 신고

    // 최근 7일 통계
    private List<DailyStats> weeklyStats;

    // 인기 게시글 TOP 5
    private List<PopularPost> popularPosts;

    // 활동적인 유저 TOP 5
    private List<ActiveUser> activeUserList;

    @Data
    public static class DailyStats {
        private String date;                 // 날짜 (yyyy-MM-dd)
        private Integer newUsers;            // 신규 가입자
        private Integer newPosts;            // 신규 게시글
        private Integer newPredictions;      // 신규 예측
    }

    @Data
    public static class PopularPost {
        private Long postId;
        private String title;
        private String categoryName;
        private Integer viewCount;
        private Integer likeCount;
        private LocalDateTime createdAt;
    }

    @Data
    public static class ActiveUser {
        private String username;
        private String nickname;
        private String tier;
        private Integer postCount;
        private Integer commentCount;
    }
}
