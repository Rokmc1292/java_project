package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자용 사용자 목록 DTO
 */
@Data
public class AdminUserDto {
    private Long userId;
    private String username;
    private String nickname;
    private String email;
    private String tier;
    private Integer tierScore;
    private Boolean isActive;
    private Boolean isAdmin;
    private Integer postCount;               // 작성한 게시글 수
    private Integer commentCount;            // 작성한 댓글 수
    private Integer predictionCount;         // 예측 참여 수
    private LocalDateTime lastLoginAt;       // 마지막 로그인
    private LocalDateTime createdAt;         // 가입일
}
