package com.example.backend.controller;

import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 관리자 페이지 API Controller
 * 대시보드, 사용자 관리, 신고 관리 등
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/page")
@RequiredArgsConstructor
public class AdminPageController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PredictionRepository predictionRepository;
    private final MatchRepository matchRepository;
    private final ReportRepository reportRepository;

    // Repository에 추가 필요한 메서드들 (주석)
    // UserRepository: countByCreatedAtAfter(LocalDateTime date)
    // PostRepository: countByCreatedAtAfter(LocalDateTime date), countByUser(User user)
    // CommentRepository: countByCreatedAtAfter(LocalDateTime date), countByUser(User user)
    // PredictionRepository: countByCreatedAtAfter(LocalDateTime date), countByUser(User user)
    // ReportRepository: countByStatus(String status), findByStatusOrderByCreatedAtDesc(String status, Pageable pageable)

    /**
     * 대시보드 통계 조회
     * GET /api/admin/page/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("관리자 대시보드 통계 조회");

        Map<String, Object> stats = new HashMap<>();

        try {
            // 전체 통계
            stats.put("totalUsers", userRepository.count());
            stats.put("totalPosts", postRepository.count());
            stats.put("totalComments", commentRepository.count());
            stats.put("totalPredictions", predictionRepository.count());
            stats.put("totalMatches", matchRepository.count());
            stats.put("pendingReports", reportRepository.countByStatus("PENDING"));

            // 오늘의 통계
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            stats.put("todayUsers", userRepository.countByCreatedAtAfter(todayStart));
            stats.put("todayPosts", postRepository.countByCreatedAtAfter(todayStart));
            stats.put("todayComments", commentRepository.countByCreatedAtAfter(todayStart));
            stats.put("todayPredictions", predictionRepository.countByCreatedAtAfter(todayStart));

            // 최근 7일 통계
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            stats.put("weekUsers", userRepository.countByCreatedAtAfter(weekAgo));
            stats.put("weekPosts", postRepository.countByCreatedAtAfter(weekAgo));

            // 최근 가입자 5명
            Pageable topFive = PageRequest.of(0, 5, Sort.by("createdAt").descending());
            List<User> recentUsers = userRepository.findAll(topFive).getContent();
            List<Map<String, Object>> recentUserList = new ArrayList<>();
            for (User user : recentUsers) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("userId", user.getUserId());
                userMap.put("username", user.getUsername());
                userMap.put("nickname", user.getNickname());
                userMap.put("email", user.getEmail());
                userMap.put("tier", user.getTier());
                userMap.put("createdAt", user.getCreatedAt());
                recentUserList.add(userMap);
            }
            stats.put("recentUsers", recentUserList);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("대시보드 통계 조회 실패", e);
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * 사용자 목록 조회 (페이징)
     * GET /api/admin/page/users?page=0&size=20
     */
    @GetMapping("/users")
    public ResponseEntity<Page<Map<String, Object>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        log.info("사용자 목록 조회 - page: {}, size: {}, search: {}", page, size, search);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<User> users = userRepository.findAll(pageable);

            Page<Map<String, Object>> result = users.map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("userId", user.getUserId());
                userMap.put("username", user.getUsername());
                userMap.put("nickname", user.getNickname());
                userMap.put("email", user.getEmail());
                userMap.put("tier", user.getTier());
                userMap.put("tierScore", user.getTierScore());
                userMap.put("isActive", user.getIsActive());
                userMap.put("isAdmin", user.getIsAdmin());
                userMap.put("createdAt", user.getCreatedAt());

                // 활동 통계
                userMap.put("postCount", postRepository.countByUser(user));
                userMap.put("commentCount", commentRepository.countByUser(user));
                userMap.put("predictionCount", predictionRepository.countByUser(user));

                return userMap;
            });

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("사용자 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용자 활성화/비활성화
     * PUT /api/admin/page/users/{userId}/status
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long userId) {
        log.info("사용자 상태 변경 - userId: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            user.setIsActive(!user.getIsActive());
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isActive", user.getIsActive());
            response.put("message", user.getIsActive() ? "사용자가 활성화되었습니다." : "사용자가 비활성화되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("사용자 상태 변경 실패", e);
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * 관리자 권한 부여/해제
     * PUT /api/admin/page/users/{userId}/admin
     */
    @PutMapping("/users/{userId}/admin")
    public ResponseEntity<Map<String, Object>> toggleAdminRole(@PathVariable Long userId) {
        log.info("관리자 권한 변경 - userId: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            user.setIsAdmin(!user.getIsAdmin());
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isAdmin", user.getIsAdmin());
            response.put("message", user.getIsAdmin() ? "관리자 권한이 부여되었습니다." : "관리자 권한이 해제되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("관리자 권한 변경 실패", e);
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * 신고 목록 조회 (페이징)
     * GET /api/admin/page/reports?page=0&size=20&status=PENDING
     */
    @GetMapping("/reports")
    public ResponseEntity<Page<Map<String, Object>>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        log.info("신고 목록 조회 - page: {}, size: {}, status: {}", page, size, status);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Report> reports;

            if (status != null && !status.isEmpty()) {
                reports = reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
            } else {
                reports = reportRepository.findAll(pageable);
            }

            Page<Map<String, Object>> result = reports.map(report -> {
                Map<String, Object> reportMap = new HashMap<>();
                reportMap.put("reportId", report.getReportId());
                reportMap.put("targetType", report.getTargetType());
                reportMap.put("targetId", report.getTargetId());
                reportMap.put("reason", report.getReason());
                reportMap.put("description", report.getDescription());
                reportMap.put("status", report.getStatus());
                reportMap.put("createdAt", report.getCreatedAt());

                // 신고 대상 컨텐츠 가져오기
                String targetContent = null;
                String targetAuthor = null;
                if ("POST".equals(report.getTargetType())) {
                    postRepository.findById(report.getTargetId()).ifPresent(post -> {
                        reportMap.put("targetContent", post.getTitle());
                        reportMap.put("targetAuthor", post.getUser().getNickname());
                    });
                } else if ("COMMENT".equals(report.getTargetType())) {
                    commentRepository.findById(report.getTargetId()).ifPresent(comment -> {
                        reportMap.put("targetContent", comment.getContent());
                        reportMap.put("targetAuthor", comment.getUser().getNickname());
                        reportMap.put("targetDeleted", comment.getIsDeleted());
                    });
                }

                // 신고자 정보
                Map<String, Object> reporterMap = new HashMap<>();
                reporterMap.put("userId", report.getReporter().getUserId());
                reporterMap.put("username", report.getReporter().getUsername());
                reporterMap.put("nickname", report.getReporter().getNickname());
                reportMap.put("reporter", reporterMap);

                return reportMap;
            });

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("신고 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 신고 처리
     * PUT /api/admin/page/reports/{reportId}/process
     */
    @PutMapping("/reports/{reportId}/process")
    public ResponseEntity<Map<String, Object>> processReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, String> request) {

        log.info("신고 처리 - reportId: {}", reportId);

        try {
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new RuntimeException("신고를 찾을 수 없습니다."));

            String action = request.get("action"); // PROCESSED, REJECTED

            if ("PROCESSED".equals(action)) {
                report.setStatus("PROCESSED");

                // 신고 대상 처리 (게시글/댓글 블라인드 처리)
                if ("POST".equals(report.getTargetType())) {
                    postRepository.findById(report.getTargetId()).ifPresent(post -> {
                        post.setIsBlinded(true);
                        postRepository.save(post);
                    });
                } else if ("COMMENT".equals(report.getTargetType())) {
                    commentRepository.findById(report.getTargetId()).ifPresent(comment -> {
                        log.info("댓글 삭제 처리 - commentId: {}, 이전 상태: {}", comment.getCommentId(), comment.getIsDeleted());
                        comment.setIsDeleted(true);
                        commentRepository.save(comment);
                        log.info("댓글 삭제 완료 - commentId: {}, 현재 상태: {}", comment.getCommentId(), comment.getIsDeleted());
                    });
                }

            } else if ("REJECTED".equals(action)) {
                report.setStatus("REJECTED");
            }

            reportRepository.save(report);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "신고가 처리되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("신고 처리 실패", e);
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * 게시글 목록 조회 (관리자용)
     * GET /api/admin/page/posts?page=0&size=20
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<Map<String, Object>>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("게시글 목록 조회 (관리자) - page: {}, size: {}", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> posts = postRepository.findAll(pageable);

            Page<Map<String, Object>> result = posts.map(post -> {
                Map<String, Object> postMap = new HashMap<>();
                postMap.put("postId", post.getPostId());
                postMap.put("title", post.getTitle());
                postMap.put("categoryName", post.getCategory().getCategoryName());
                postMap.put("username", post.getUser().getUsername());
                postMap.put("nickname", post.getUser().getNickname());
                postMap.put("viewCount", post.getViewCount());
                postMap.put("likeCount", post.getLikeCount());
                postMap.put("commentCount", post.getCommentCount());
                postMap.put("isBlinded", post.getIsBlinded());
                postMap.put("createdAt", post.getCreatedAt());
                return postMap;
            });

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("게시글 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 게시글 블라인드 처리
     * PUT /api/admin/page/posts/{postId}/blind
     */
    @PutMapping("/posts/{postId}/blind")
    public ResponseEntity<Map<String, Object>> blindPost(@PathVariable Long postId) {
        log.info("게시글 블라인드 처리 - postId: {}", postId);

        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

            post.setIsBlinded(!post.getIsBlinded());
            postRepository.save(post);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isBlinded", post.getIsBlinded());
            response.put("message", post.getIsBlinded() ? "게시글이 블라인드 처리되었습니다." : "블라인드가 해제되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("게시글 블라인드 처리 실패", e);
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}