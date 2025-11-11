package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.AdminPageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 관리자 페이지 Controller
 * 대시보드, 사용자 관리, 신고 관리 등
 */
@Slf4j
@RestController
@RequestMapping("/api/admin-page")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminPageController {

    private final AdminPageService adminPageService;

    /**
     * 관리자 권한 확인 헬퍼 메서드
     */
    private String validateAdmin(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        if (!adminPageService.isAdmin(username)) {
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }

        return username;
    }

    // ========== 대시보드 ==========

    /**
     * 관리자 대시보드 통계 조회
     * GET /api/admin-page/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDto> getDashboard(HttpSession session) {
        validateAdmin(session);
        AdminDashboardDto dashboard = adminPageService.getDashboardStats();
        return ResponseEntity.ok(dashboard);
    }

    /**
     * 최근 활동 로그 조회
     * GET /api/admin-page/recent-activities?page=0&size=20
     */
    @GetMapping("/recent-activities")
    public ResponseEntity<Page<AdminActivityLogDto>> getRecentActivities(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        validateAdmin(session);
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminActivityLogDto> activities = adminPageService.getRecentActivities(pageable);
        return ResponseEntity.ok(activities);
    }

    // ========== 사용자 관리 ==========

    /**
     * 전체 사용자 목록 조회 (검색, 필터링, 정렬)
     * GET /api/admin-page/users?search=&tier=&status=&page=0&size=20&sort=createdAt
     */
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserDto>> getUsers(
            HttpSession session,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tier,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort) {

        validateAdmin(session);
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminUserDto> users = adminPageService.getUsers(search, tier, status, sort, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * 특정 사용자 상세 정보 조회
     * GET /api/admin-page/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserDetailDto> getUserDetail(
            HttpSession session,
            @PathVariable Long userId) {

        validateAdmin(session);
        AdminUserDetailDto userDetail = adminPageService.getUserDetail(userId);
        return ResponseEntity.ok(userDetail);
    }

    /**
     * 사용자 계정 활성화/비활성화
     * PUT /api/admin-page/users/{userId}/status
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            HttpSession session,
            @PathVariable Long userId,
            @RequestBody Map<String, Boolean> request) {

        String adminUsername = validateAdmin(session);
        Boolean isActive = request.get("isActive");

        adminPageService.updateUserStatus(userId, isActive, adminUsername);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", isActive ? "계정이 활성화되었습니다." : "계정이 비활성화되었습니다."
        ));
    }

    /**
     * 사용자 티어 점수 수정
     * PUT /api/admin-page/users/{userId}/tier-score
     */
    @PutMapping("/users/{userId}/tier-score")
    public ResponseEntity<Map<String, Object>> updateUserTierScore(
            HttpSession session,
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> request) {

        String adminUsername = validateAdmin(session);
        Integer tierScore = request.get("tierScore");

        adminPageService.updateUserTierScore(userId, tierScore, adminUsername);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "티어 점수가 수정되었습니다."
        ));
    }

    /**
     * 사용자 관리자 권한 부여/회수
     * PUT /api/admin-page/users/{userId}/admin
     */
    @PutMapping("/users/{userId}/admin")
    public ResponseEntity<Map<String, Object>> updateAdminStatus(
            HttpSession session,
            @PathVariable Long userId,
            @RequestBody Map<String, Boolean> request) {

        String adminUsername = validateAdmin(session);
        Boolean isAdmin = request.get("isAdmin");

        adminPageService.updateAdminStatus(userId, isAdmin, adminUsername);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", isAdmin ? "관리자 권한이 부여되었습니다." : "관리자 권한이 회수되었습니다."
        ));
    }

    // ========== 신고 관리 ==========

    /**
     * 신고 목록 조회 (필터링, 정렬)
     * GET /api/admin-page/reports?status=&type=&page=0&size=20
     */
    @GetMapping("/reports")
    public ResponseEntity<Page<AdminReportDto>> getReports(
            HttpSession session,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        validateAdmin(session);
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminReportDto> reports = adminPageService.getReports(status, type, pageable);
        return ResponseEntity.ok(reports);
    }

    /**
     * 신고 상세 정보 조회
     * GET /api/admin-page/reports/{reportId}
     */
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<AdminReportDetailDto> getReportDetail(
            HttpSession session,
            @PathVariable Long reportId) {

        validateAdmin(session);
        AdminReportDetailDto reportDetail = adminPageService.getReportDetail(reportId);
        return ResponseEntity.ok(reportDetail);
    }

    /**
     * 신고 처리 (승인/반려)
     * PUT /api/admin-page/reports/{reportId}/process
     */
    @PutMapping("/reports/{reportId}/process")
    public ResponseEntity<Map<String, Object>> processReport(
            HttpSession session,
            @PathVariable Long reportId,
            @RequestBody Map<String, String> request) {

        String adminUsername = validateAdmin(session);
        String action = request.get("action"); // APPROVE, REJECT
        String adminNote = request.get("adminNote");

        adminPageService.processReport(reportId, action, adminNote, adminUsername);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "신고가 처리되었습니다."
        ));
    }

    // ========== 게시글 관리 ==========

    /**
     * 전체 게시글 목록 조회 (관리자용)
     * GET /api/admin-page/posts?search=&category=&isBlinded=&page=0&size=20
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<AdminPostDto>> getPosts(
            HttpSession session,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isBlinded,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        validateAdmin(session);
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminPostDto> posts = adminPageService.getPosts(search, category, isBlinded, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 게시글 블라인드 처리
     * PUT /api/admin-page/posts/{postId}/blind
     */
    @PutMapping("/posts/{postId}/blind")
    public ResponseEntity<Map<String, Object>> blindPost(
            HttpSession session,
            @PathVariable Long postId,
            @RequestBody Map<String, String> request) {

        String adminUsername = validateAdmin(session);
        String reason = request.get("reason");

        adminPageService.blindPost(postId, reason, adminUsername);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "게시글이 블라인드 처리되었습니다."
        ));
    }

    /**
     * 게시글 블라인드 해제
     * PUT /api/admin-page/posts/{postId}/unblind
     */
    @PutMapping("/posts/{postId}/unblind")
    public ResponseEntity<Map<String, Object>> unblindPost(
            HttpSession session,
            @PathVariable Long postId) {

        String adminUsername = validateAdmin(session);

        adminPageService.unblindPost(postId, adminUsername);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "게시글 블라인드가 해제되었습니다."
        ));
    }

    /**
     * 게시글 삭제 (관리자)
     * DELETE /api/admin-page/posts/{postId}
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Map<String, Object>> deletePost(
            HttpSession session,
            @PathVariable Long postId) {

        String adminUsername = validateAdmin(session);

        adminPageService.deletePost(postId, adminUsername);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "게시글이 삭제되었습니다."
        ));
    }

    // ========== 통계 및 분석 ==========

    /**
     * 사용자 통계 (일별/월별)
     * GET /api/admin-page/statistics/users?period=daily
     */
    @GetMapping("/statistics/users")
    public ResponseEntity<AdminUserStatisticsDto> getUserStatistics(
            HttpSession session,
            @RequestParam(defaultValue = "daily") String period) {

        validateAdmin(session);
        AdminUserStatisticsDto statistics = adminPageService.getUserStatistics(period);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 게시글/댓글 통계
     * GET /api/admin-page/statistics/content?period=daily
     */
    @GetMapping("/statistics/content")
    public ResponseEntity<AdminContentStatisticsDto> getContentStatistics(
            HttpSession session,
            @RequestParam(defaultValue = "daily") String period) {

        validateAdmin(session);
        AdminContentStatisticsDto statistics = adminPageService.getContentStatistics(period);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 예측 통계
     * GET /api/admin-page/statistics/predictions?period=daily
     */
    @GetMapping("/statistics/predictions")
    public ResponseEntity<AdminPredictionStatisticsDto> getPredictionStatistics(
            HttpSession session,
            @RequestParam(defaultValue = "daily") String period) {

        validateAdmin(session);
        AdminPredictionStatisticsDto statistics = adminPageService.getPredictionStatistics(period);
        return ResponseEntity.ok(statistics);
    }
}