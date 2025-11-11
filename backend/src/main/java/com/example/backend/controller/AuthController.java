package com.example.backend.controller;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.SignupRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;      // ✅ 주입

    /** 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        UserResponse userResponse = userService.signup(request);
        return ResponseEntity.ok(userResponse);
    }

    /** 로그인 (세션 기반) */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        UserResponse userResponse = userService.login(request);
        // 세션 저장
        session.setAttribute("username", userResponse.getUsername());
        session.setAttribute("userId", userResponse.getUserId());

        return ResponseEntity.ok(Map.of("user", userResponse));
    }

    /** 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "로그아웃 되었습니다."));
    }

    /** 현재 로그인한 사용자 정보 */
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }
        UserResponse userResponse = userService.getUserByUsername(username);
        return ResponseEntity.ok(userResponse);
    }

    /** 아이디 중복 체크 */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        boolean dup = userService.checkUsernameDuplicate(username);
        return ResponseEntity.ok(Map.of("isDuplicate", dup));
    }

    /** 이메일 중복 체크 */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean dup = userService.checkEmailDuplicate(email);
        return ResponseEntity.ok(Map.of("isDuplicate", dup));
    }

    /** 현재 로그인/권한 확인 (프론트 가드용) */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAuth(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Map<String, Object> res = new HashMap<>();
        res.put("username", user.getUsername());
        res.put("nickname", user.getNickname());
        res.put("email", user.getEmail());
        res.put("tier", user.getTier());
        res.put("isAdmin", Boolean.TRUE.equals(user.getIsAdmin()));
        res.put("isActive", user.getIsActive());
        return ResponseEntity.ok(res);
    }
}
