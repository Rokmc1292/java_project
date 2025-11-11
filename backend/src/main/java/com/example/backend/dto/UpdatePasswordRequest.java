package com.example.backend.dto;

import lombok.Data;

/**
 * 비밀번호 변경 요청 DTO
 */
@Data
public class UpdatePasswordRequest {
    private String currentPassword;         // 현재 비밀번호
    private String newPassword;             // 새 비밀번호
    private String confirmPassword;         // 비밀번호 확인
}