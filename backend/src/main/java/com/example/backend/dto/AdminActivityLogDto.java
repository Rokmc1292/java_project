package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자용 활동 로그 DTO
 */
@Data
public class AdminActivityLogDto {
    private Long activityId;
    private String activityType;             // USER_REGISTER, POST_CREATE, REPORT_SUBMIT, etc.
    private String description;              // 활동 설명
    private String username;                 // 활동한 사용자
    private String targetType;               // 대상 타입 (USER, POST, COMMENT, etc.)
    private Long targetId;                   // 대상 ID
    private LocalDateTime createdAt;         // 활동 시간
}
