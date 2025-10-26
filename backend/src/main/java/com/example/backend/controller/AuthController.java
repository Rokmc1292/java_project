package com.example.backend.controller;

import com.example.backend.config.JwtUtil;
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
 * JWT 토큰 기반 인증 시스템
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입 API
     * POST /api/auth/signup
     * 요청 본문: SignupRequest (username, password, passwordConfirm, nickname, email)
     * 응답: 생성된 사용자 정보 (UserResponse)
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        UserResponse userResponse = userService.signup(request);
        return ResponseEntity.ok(userResponse);
    }

    /**
     * 로그인 API (JWT 토큰 발급)
     * POST /api/auth/login
     * 요청 본문: LoginRequest (username, password)
     * 응답: 사용자 정보 + JWT 토큰
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 로그인 검증
        UserResponse userResponse = userService.login(request);

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(request.getUsername());

        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("user", userResponse);
        response.put("token", token);

        return ResponseEntity.ok(response);
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
     * GET /api/auth/check-nickname?nickname=테스터
     * 응답: { "isDuplicate": true/false }
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestParam String nickname) {
        boolean isDuplicate = userService.checkNicknameDuplicate(nickname);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     * GET /api/auth/me
     * 헤더: Authorization: Bearer {token}
     * 응답: 사용자 정보
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        // Bearer 토큰에서 실제 토큰 추출
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);

        UserResponse userResponse = userService.getUserByUsername(username);
        return ResponseEntity.ok(userResponse);
    }
}