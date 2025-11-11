package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.MyPageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 마이페이지 API 컨트롤러
 * 세션 기반 인증 사용
 */
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    /**
     * 세션에서 사용자명 추출 헬퍼 메서드
     */
    private String getUsernameFromSession(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }
        return username;
    }

    /**
     * 사용자 프로필 조회
     * GET /api/mypage/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getUserProfile(HttpSession session) {
        String username = getUsernameFromSession(session);
        UserProfileDto profile = myPageService.getUserProfile(username);
        return ResponseEntity.ok(profile);
    }

    /**
     * 예측 통계 조회
     * GET /api/mypage/stats/predictions
     */
    @GetMapping("/stats/predictions")
    public ResponseEntity<PredictionStatsDto> getPredictionStats(HttpSession session) {
        String username = getUsernameFromSession(session);
        PredictionStatsDto stats = myPageService.getPredictionStats(username);
        return ResponseEntity.ok(stats);
    }

    /**
     * 최근 10경기 예측 결과 조회
     * GET /api/mypage/predictions/recent
     */
    @GetMapping("/predictions/recent")
    public ResponseEntity<List<RecentPredictionResultDto>> getRecentPredictionResults(HttpSession session) {
        String username = getUsernameFromSession(session);
        List<RecentPredictionResultDto> results = myPageService.getRecentPredictionResults(username);
        return ResponseEntity.ok(results);
    }

    /**
     * 사용자 예측 내역 조회 (페이징)
     * GET /api/mypage/predictions/history?page=0&size=10
     */
    @GetMapping("/predictions/history")
    public ResponseEntity<Page<UserPredictionHistoryDto>> getUserPredictionHistory(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String username = getUsernameFromSession(session);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserPredictionHistoryDto> history = myPageService.getUserPredictionHistory(username, pageable);
        return ResponseEntity.ok(history);
    }

    /**
     * 사용자 작성 게시글 조회 (페이징)
     * GET /api/mypage/posts?page=0&size=10
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<UserPostDto>> getUserPosts(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String username = getUsernameFromSession(session);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserPostDto> posts = myPageService.getUserPosts(username, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 사용자 작성 댓글 조회 (페이징)
     * GET /api/mypage/comments?page=0&size=10
     */
    @GetMapping("/comments")
    public ResponseEntity<Page<UserCommentDto>> getUserComments(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String username = getUsernameFromSession(session);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserCommentDto> comments = myPageService.getUserComments(username, pageable);
        return ResponseEntity.ok(comments);
    }

    /**
     * 닉네임 변경
     * PUT /api/mypage/nickname
     */
    @PutMapping("/nickname")
    public ResponseEntity<String> updateNickname(
            HttpSession session,
            @RequestBody UpdateNicknameRequest request) {

        String username = getUsernameFromSession(session);
        myPageService.updateNickname(username, request.getNickname());
        return ResponseEntity.ok("닉네임이 변경되었습니다");
    }

    /**
     * 비밀번호 변경
     * PUT /api/mypage/password
     */
    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(
            HttpSession session,
            @RequestBody UpdatePasswordRequest request) {

        String username = getUsernameFromSession(session);
        myPageService.updatePassword(username, request);
        return ResponseEntity.ok("비밀번호가 변경되었습니다");
    }

    /**
     * 알림 설정 조회
     * GET /api/mypage/settings
     */
    @GetMapping("/settings")
    public ResponseEntity<UpdateSettingsRequest> getUserSettings(HttpSession session) {
        String username = getUsernameFromSession(session);
        UpdateSettingsRequest settings = myPageService.getUserSettings(username);
        return ResponseEntity.ok(settings);
    }

    /**
     * 알림 설정 변경
     * PUT /api/mypage/settings
     */
    @PutMapping("/settings")
    public ResponseEntity<String> updateUserSettings(
            HttpSession session,
            @RequestBody UpdateSettingsRequest request) {

        String username = getUsernameFromSession(session);
        myPageService.updateUserSettings(username, request);
        return ResponseEntity.ok("설정이 변경되었습니다");
    }
}