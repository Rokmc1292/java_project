package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자용 신고 상세 DTO
 */
@Data
public class AdminReportDetailDto {
    private Long reportId;

    // 신고자 정보
    private ReporterInfo reporter;

    // 대상 정보
    private TargetInfo target;

    // 신고 내용
    private String reason;
    private String description;
    private String status;
    private LocalDateTime createdAt;

    // 처리 정보
    private String processedBy;              // 처리한 관리자
    private String adminNote;                // 관리자 메모
    private LocalDateTime processedAt;       // 처리일

    @Data
    public static class ReporterInfo {
        private Long userId;
        private String username;
        private String nickname;
        private String tier;
        private Integer reportCount;         // 이 사용자의 총 신고 횟수
    }

    @Data
    public static class TargetInfo {
        private String targetType;           // POST, COMMENT
        private Long targetId;
        private String content;              // 신고 대상의 전체 내용
        private String authorUsername;
        private String authorNickname;
        private Integer authorReportCount;   // 작성자가 받은 총 신고 횟수
        private LocalDateTime createdAt;
    }
}
