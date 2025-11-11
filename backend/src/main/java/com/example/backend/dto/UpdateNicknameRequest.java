package com.example.backend.dto;

import lombok.Data;

/**
 * 닉네임 변경 요청 DTO
 */
@Data
public class UpdateNicknameRequest {
    private String nickname;                // 변경할 닉네임
}