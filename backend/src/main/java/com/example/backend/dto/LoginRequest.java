package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 로그인 요청 DTO
 * 프론트엔드에서 로그인 폼 데이터를 받아오는 객체
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    // 로그인 아이디
    private String username;

    // 비밀번호
    private String password;
}