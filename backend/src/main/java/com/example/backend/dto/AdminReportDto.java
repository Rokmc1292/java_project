package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자용 신고 목록 DTO
 */
@Data
public class AdminReportDto {
    private Long reportId;
    private String reporterUsername;         // 신고자
    private String reporterNickname;
    private String targetType;               // POST, COMMENT
    private Long targetId;
    private String targetTitle;              // 대상 제목/내용 미리보기
    private String targetAuthor;             // 대상 작성자
    private String reason;                   // 신고 사유
    private String status;                   // PENDING, APPROVED, REJECTED
    private LocalDateTime createdAt;         // 신고일
}
