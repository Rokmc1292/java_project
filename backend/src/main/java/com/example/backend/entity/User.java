package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * User 엔티티 클래스
 * DB의 users 테이블과 매핑되는 JPA 엔티티
 * 사용자 기본 정보를 담고 있음
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    // 사용자 고유 ID (Primary Key, 자동 증가)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // 로그인 아이디 (unique 제약조건)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // 암호화된 비밀번호
    @Column(nullable = false)
    private String password;

    // 커뮤니티 활동 닉네임 (unique 제약조건)
    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    // 이메일 주소 (unique 제약조건)
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // 프로필 이미지 URL (기본값: 기본 프로필 이미지)
    @Column(name = "profile_image")
    private String profileImage = "/images/default-profile.png";

    // 티어 등급 (BRONZE, SILVER, GOLD, PLATINUM, DIAMOND)
    @Column(length = 20)
    private String tier = "BRONZE";

    // 티어 점수 (승부예측 성공/실패에 따라 증감)
    @Column(name = "tier_score")
    private Integer tierScore = 0;

    // 계정 활성화 상태
    @Column(name = "is_active")
    private Boolean isActive = true;

    // 관리자 여부
    @Column(name = "is_admin")
    private Boolean isAdmin = false;

    // 가입일 (자동 생성)
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 정보 수정일 (자동 업데이트)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 엔티티가 저장되기 전에 실행되는 메서드
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // 엔티티가 업데이트되기 전에 실행되는 메서드
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}