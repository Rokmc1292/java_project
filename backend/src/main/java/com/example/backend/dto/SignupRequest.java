package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 회원가입 요청 DTO
 * 프론트엔드에서 회원가입 폼 데이터를 받아오는 객체
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    // 로그인 아이디
    private String username;

    // 비밀번호 (평문으로 받아서 서버에서 암호화)
    private String password;

    // 비밀번호 확인
    private String passwordConfirm;

    // 닉네임
    private String nickname;

    // 이메일
    private String email;
}