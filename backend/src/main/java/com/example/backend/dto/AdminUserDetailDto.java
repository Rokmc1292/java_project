package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자용 사용자 상세 DTO
 */
@Data
public class AdminUserDetailDto {
    private Long userId;
    private String username;
    private String nickname;
    private String email;
    private String profileImage;
    private String tier;
    private Integer tierScore;
    private Boolean isActive;
    private Boolean isAdmin;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    // 활동 통계
    private UserActivityStats activityStats;

    // 최근 활동 내역
    private List<RecentActivity> recentActivities;

    // 신고 내역 (받은 신고)
    private List<ReportSummary> reports;

    @Data
    public static class UserActivityStats {
        private Integer totalPosts;
        private Integer totalComments;
        private Integer totalPredictions;
        private Integer correctPredictions;
        private Double predictionAccuracy;
        private Integer receivedLikes;       // 받은 추천 수
        private Integer receivedReports;     // 받은 신고 수
    }

    @Data
    public static class RecentActivity {
        private String activityType;         // POST, COMMENT, PREDICTION
        private String description;
        private LocalDateTime createdAt;
    }

    @Data
    public static class ReportSummary {
        private Long reportId;
        private String reason;
        private String status;
        private LocalDateTime createdAt;
    }
}
