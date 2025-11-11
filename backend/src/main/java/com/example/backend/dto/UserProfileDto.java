package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 사용자 프로필 정보 DTO
 * 프로필 이미지, 닉네임, 티어, 점수, 가입일
 */
@Data
public class UserProfileDto {
    private Long userId;                    // 사용자 ID
    private String nickname;                // 닉네임
    private String profileImage;            // 프로필 이미지 URL
    private String tier;                    // 티어 등급
    private Integer tierScore;              // 현재 티어 점수
    private Integer nextTierScore;          // 다음 티어까지 필요한 점수
    private LocalDateTime createdAt;        // 가입일
}