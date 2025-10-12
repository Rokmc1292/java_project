package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 * 로그인 성공 시 또는 사용자 정보 조회 시 프론트엔드로 전달하는 객체
 * 비밀번호 같은 민감한 정보는 제외
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    // 사용자 ID
    private Long userId;

    // 로그인 아이디
    private String username;

    // 닉네임
    private String nickname;

    // 이메일
    private String email;

    // 프로필 이미지 URL
    private String profileImage;

    // 티어 등급
    private String tier;

    // 티어 점수
    private Integer tierScore;

    // 관리자 여부
    private Boolean isAdmin;

    // 가입일
    private LocalDateTime createdAt;
}