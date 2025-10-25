package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.CommunityService;
import com.example.backend.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 커뮤니티 Controller
 */
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CommunityController {

    private final CommunityService communityService;
    private final StatisticsService statisticsService;

    // ==================== 카테고리 ====================

    /**
     * 카테고리 목록 조회
     * GET /api/community/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<BoardCategoryDto>> getAllCategories() {
        List<BoardCategoryDto> categories = communityService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // ==================== 게시글 조회 ====================

    /**
     * 전체 게시글 조회
     * GET /api/community/posts?page=0&size=20&username=user1
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<PostDto>> getAllPosts(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> posts = communityService.getAllPosts(username, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 카테고리별 게시글 조회
     * GET /api/community/posts/category/축구?page=0&size=20&username=user1
     */
    @GetMapping("/posts/category/{categoryName}")
    public ResponseEntity<Page<PostDto>> getPostsByCategory(
            @PathVariable String categoryName,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> posts = communityService.getPostsByCategory(categoryName, username, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 인기 게시글 조회 (전체)
     * GET /api/community/posts/popular?page=0&size=20&username=user1
     */
    @GetMapping("/posts/popular")
    public ResponseEntity<Page<PostDto>> getPopularPosts(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> posts = communityService.getPopularPosts(username, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 카테고리별 인기 게시글 조회
     * GET /api/community/posts/popular/축구?page=0&size=20&username=user1
     */
    @GetMapping("/posts/popular/{categoryName}")
    public ResponseEntity<Page<PostDto>> getPopularPostsByCategory(
            @PathVariable String categoryName,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> posts = communityService.getPopularPostsByCategory(categoryName, username, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 게시글 검색
     * GET /api/community/posts/search?keyword=검색어&searchType=all&username=user1&page=0&size=20
     */
    @GetMapping("/posts/search")
    public ResponseEntity<Page<PostDto>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "all") String searchType,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> posts = communityService.searchPosts(keyword, searchType, username, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 게시글 상세 조회
     * GET /api/community/posts/1?username=user1
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostDto> getPost(
            @PathVariable Long postId,
            @RequestParam(required = false) String username) {
        PostDto post = communityService.getPost(postId, username);
        return ResponseEntity.ok(post);
    }

    // ==================== 게시글 CRUD ====================

    /**
     * 게시글 생성
     * POST /api/community/posts
     */
    @PostMapping("/posts")
    public ResponseEntity<PostDto> createPost(@RequestBody CreatePostRequest request) {
        PostDto post = communityService.createPost(request, request.getUsername());
        return ResponseEntity.ok(post);
    }

    /**
     * 게시글 수정
     * PUT /api/community/posts/1
     */
    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable Long postId,
            @RequestBody UpdatePostRequest request,
            @RequestParam String username) {
        PostDto post = communityService.updatePost(postId, request, username);
        return ResponseEntity.ok(post);
    }

    /**
     * 게시글 삭제
     * DELETE /api/community/posts/1?username=user1
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @RequestParam String username) {
        communityService.deletePost(postId, username);
        return ResponseEntity.ok().build();
    }

    // ==================== 추천/비추천 ====================

    /**
     * 게시글 추천
     * POST /api/community/posts/1/like
     */
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Void> likePost(
            @PathVariable Long postId,
            @RequestBody Map<String, String> request) {
        String username = request.get("username");
        communityService.likePost(postId, username);
        return ResponseEntity.ok().build();
    }

    /**
     * 게시글 비추천
     * POST /api/community/posts/1/dislike
     */
    @PostMapping("/posts/{postId}/dislike")
    public ResponseEntity<Void> dislikePost(
            @PathVariable Long postId,
            @RequestBody Map<String, String> request) {
        String username = request.get("username");
        communityService.dislikePost(postId, username);
        return ResponseEntity.ok().build();
    }

    // ==================== 스크랩 ====================

    /**
     * 게시글 스크랩/스크랩 취소
     * POST /api/community/posts/1/scrap
     */
    @PostMapping("/posts/{postId}/scrap")
    public ResponseEntity<Void> scrapPost(
            @PathVariable Long postId,
            @RequestBody Map<String, String> request) {
        String username = request.get("username");
        communityService.scrapPost(postId, username);
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 스크랩 목록 조회
     * GET /api/community/scraps?username=user1&page=0&size=20
     */
    @GetMapping("/scraps")
    public ResponseEntity<Page<PostDto>> getUserScraps(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> scraps = communityService.getUserScraps(username, pageable);
        return ResponseEntity.ok(scraps);
    }

    // ==================== 신고/차단 ====================

    /**
     * 게시글 신고
     * POST /api/community/posts/1/report
     */
    @PostMapping("/posts/{postId}/report")
    public ResponseEntity<Void> reportPost(
            @PathVariable Long postId,
            @RequestBody Map<String, String> request) {
        String username = request.get("username");
        String reason = request.get("reason");
        String description = request.get("description");
        communityService.reportPost(postId, username, reason, description);
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 차단/차단 해제
     * POST /api/community/block
     */
    @PostMapping("/block")
    public ResponseEntity<Void> blockUser(@RequestBody Map<String, String> request) {
        String blockerUsername = request.get("blockerUsername");
        String blockedUsername = request.get("blockedUsername");
        communityService.blockUser(blockerUsername, blockedUsername);
        return ResponseEntity.ok().build();
    }

    /**
     * 게시글 블라인드 처리 (관리자 전용)
     * POST /api/community/posts/1/blind
     */
    @PostMapping("/posts/{postId}/blind")
    public ResponseEntity<Void> blindPost(
            @PathVariable Long postId,
            @RequestBody Map<String, String> request) {
        String adminUsername = request.get("adminUsername");
        communityService.blindPost(postId, adminUsername);
        return ResponseEntity.ok().build();
    }

    // ==================== 댓글 ====================

    /**
     * 게시글 댓글 조회
     * GET /api/community/posts/1/comments
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long postId) {
        List<CommentDto> comments = communityService.getComments(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 댓글 작성
     * POST /api/community/posts/1/comments
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long postId,
            @RequestBody Map<String, String> request) {
        String username = request.get("username");
        String content = request.get("content");
        Long parentCommentId = request.get("parentCommentId") != null
                ? Long.parseLong(request.get("parentCommentId"))
                : null;

        CommentDto comment = communityService.createComment(postId, username, content, parentCommentId);
        return ResponseEntity.ok(comment);
    }

    /**
     * 댓글 삭제
     * DELETE /api/community/comments/1?username=user1
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestParam String username) {
        communityService.deleteComment(commentId, username);
        return ResponseEntity.ok().build();
    }

    // ==================== 통계 ====================

    /**
     * 주간 베스트 게시글
     * GET /api/community/stats/weekly-best?limit=10
     */
    @GetMapping("/stats/weekly-best")
    public ResponseEntity<List<PostStatisticsDto>> getWeeklyBestPosts(
            @RequestParam(defaultValue = "10") int limit) {
        List<PostStatisticsDto> posts = statisticsService.getWeeklyBestPosts(limit);
        return ResponseEntity.ok(posts);
    }

    /**
     * 월간 베스트 게시글
     * GET /api/community/stats/monthly-best?limit=10
     */
    @GetMapping("/stats/monthly-best")
    public ResponseEntity<List<PostStatisticsDto>> getMonthlyBestPosts(
            @RequestParam(defaultValue = "10") int limit) {
        List<PostStatisticsDto> posts = statisticsService.getMonthlyBestPosts(limit);
        return ResponseEntity.ok(posts);
    }

    /**
     * 활동 왕성 유저 랭킹
     * GET /api/community/stats/active-users?limit=10
     */
    @GetMapping("/stats/active-users")
    public ResponseEntity<List<UserActivityDto>> getActiveUsersRanking(
            @RequestParam(defaultValue = "10") int limit) {
        List<UserActivityDto> users = statisticsService.getActiveUsersRanking(limit);
        return ResponseEntity.ok(users);
    }

    /**
     * 카테고리별 주간 베스트
     * GET /api/community/stats/weekly-best/축구?limit=5
     */
    @GetMapping("/stats/weekly-best/{categoryName}")
    public ResponseEntity<List<PostStatisticsDto>> getWeeklyBestPostsByCategory(
            @PathVariable String categoryName,
            @RequestParam(defaultValue = "5") int limit) {
        List<PostStatisticsDto> posts = statisticsService.getWeeklyBestPostsByCategory(categoryName, limit);
        return ResponseEntity.ok(posts);
    }
}
