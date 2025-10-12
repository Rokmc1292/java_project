package com.example.backend.controller;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.SignupRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 인증 관련 API Controller
 * 회원가입, 로그인, 중복 체크 엔드포인트 제공
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 회원가입 API
     * POST /api/auth/signup
     * 요청 본문: SignupRequest (username, password, passwordConfirm, nickname, email)
     * 응답: 생성된 사용자 정보 (UserResponse)
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            UserResponse userResponse = userService.signup(request);
            return ResponseEntity.ok(userResponse);
        } catch (IllegalArgumentException e) {
            // 중복 체크 실패, 비밀번호 불일치 등의 에러 처리
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 로그인 API
     * POST /api/auth/login
     * 요청 본문: LoginRequest (username, password)
     * 응답: 로그인한 사용자 정보 (UserResponse)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            UserResponse userResponse = userService.login(request);
            return ResponseEntity.ok(userResponse);
        } catch (IllegalArgumentException e) {
            // 로그인 실패 에러 처리
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 아이디 중복 체크 API
     * GET /api/auth/check-username?username=test
     * 응답: { "isDuplicate": true/false }
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        boolean isDuplicate = userService.checkUsernameDuplicate(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        return ResponseEntity.ok(response);
    }

    /**
     * 이메일 중복 체크 API
     * GET /api/auth/check-email?email=test@example.com
     * 응답: { "isDuplicate": true/false }
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean isDuplicate = userService.checkEmailDuplicate(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        return ResponseEntity.ok(response);
    }

    /**
     * 닉네임 중복 체크 API
     * GET /api/auth/check-nickname?nickname=테스트
     * 응답: { "isDuplicate": true/false }
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestParam String nickname) {
        boolean isDuplicate = userService.checkNicknameDuplicate(nickname);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        return ResponseEntity.ok(response);
    }
}