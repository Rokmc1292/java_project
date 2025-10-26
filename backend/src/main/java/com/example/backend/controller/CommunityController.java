package com.example.backend.controller;

import com.example.backend.dto.CommentDto;
import com.example.backend.dto.PostDto;
import com.example.backend.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 커뮤니티 Controller
 * 게시글 CRUD, 댓글, 추천/비추천, 스크랩, 신고 등 모든 기능 제공
 */
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    // ========== 게시글 조회 ==========

    /**
     * 전체 게시글 조회 (페이징, 검색)
     * GET /api/community/posts?page=0&size=20&search=제목
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<PostDto>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);

        Page<PostDto> posts;
        if (search != null && !search.isEmpty()) {
            posts = communityService.searchPosts(search, pageable);
        } else {
            posts = communityService.getAllPosts(pageable);
        }

        return ResponseEntity.ok(posts);
    }

    /**
     * 카테고리별 게시글 조회
     * GET /api/community/posts/category/축구?page=0&size=20
     */
    @GetMapping("/posts/category/{categoryName}")
    public ResponseEntity<Page<PostDto>> getPostsByCategory(
            @PathVariable String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> posts = communityService.getPostsByCategory(categoryName, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 인기 게시글 조회 (전체)
     * GET /api/community/posts/popular?page=0&size=20
     */
    @GetMapping("/posts/popular")
    public ResponseEntity<Page<PostDto>> getPopularPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> posts = communityService.getPopularPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 카테고리별 인기 게시글 조회
     * GET /api/community/posts/category/축구/popular
     */
    @GetMapping("/posts/category/{categoryName}/popular")
    public ResponseEntity<Page<PostDto>> getPopularPostsByCategory(
            @PathVariable String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> posts = communityService.getPopularPostsByCategory(categoryName, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 게시글 상세 조회 (조회수 증가)
     * GET /api/community/posts/123
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostDto> getPost(@PathVariable Long postId) {
        PostDto post = communityService.getPost(postId);
        return ResponseEntity.ok(post);
    }

    // ========== 게시글 작성/수정/삭제 ==========

    /**
     * 게시글 작성 (인증 필요)
     * POST /api/community/posts
     * 요청: { "categoryName": "축구", "title": "제목", "content": "내용" }
     */
    @PostMapping("/posts")
    public ResponseEntity<PostDto> createPost(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String username = authentication.getName();
        String categoryName = request.get("categoryName");
        String title = request.get("title");
        String content = request.get("content");

        PostDto post = communityService.createPost(categoryName, username, title, content);
        return ResponseEntity.ok(post);
    }

    /**
     * 게시글 수정 (본인만 가능)
     * PUT /api/community/posts/123
     */
    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable Long postId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String username = authentication.getName();
        String title = request.get("title");
        String content = request.get("content");

        PostDto post = communityService.updatePost(postId, username, title, content);
        return ResponseEntity.ok(post);
    }

    /**
     * 게시글 삭제 (본인만 가능)
     * DELETE /api/community/posts/123
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId,
            Authentication authentication) {

        String username = authentication.getName();
        communityService.deletePost(postId, username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "게시글이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    // ========== 게시글 추천/비추천 ==========

    /**
     * 게시글 추천 (1인 1회 제한)
     * POST /api/community/posts/123/like
     */
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> likePost(
            @PathVariable Long postId,
            Authentication authentication) {

        String username = authentication.getName();
        communityService.likePost(postId, username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "추천했습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 비추천 (1인 1회 제한)
     * POST /api/community/posts/123/dislike
     */
    @PostMapping("/posts/{postId}/dislike")
    public ResponseEntity<?> dislikePost(
            @PathVariable Long postId,
            Authentication authentication) {

        String username = authentication.getName();
        communityService.dislikePost(postId, username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "비추천했습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 추천/비추천 취소
     * DELETE /api/community/posts/123/vote
     */
    @DeleteMapping("/posts/{postId}/vote")
    public ResponseEntity<?> cancelVote(
            @PathVariable Long postId,
            Authentication authentication) {

        String username = authentication.getName();
        communityService.cancelPostVote(postId, username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "추천/비추천을 취소했습니다.");
        return ResponseEntity.ok(response);
    }

    // ========== 댓글 기능 ==========

    /**
     * 게시글의 댓글 목록 조회
     * GET /api/community/posts/123/comments
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long postId) {
        List<CommentDto> comments = communityService.getComments(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 댓글 작성 (인증 필요)
     * POST /api/community/posts/123/comments
     * 요청: { "content": "댓글 내용", "parentCommentId": null }
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        String username = authentication.getName();
        String content = (String) request.get("content");
        Long parentCommentId = request.get("parentCommentId") != null
                ? Long.parseLong(request.get("parentCommentId").toString())
                : null;

        CommentDto comment = communityService.createComment(postId, username, content, parentCommentId);
        return ResponseEntity.ok(comment);
    }

    /**
     * 댓글 수정 (본인만 가능)
     * PUT /api/community/comments/456
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String username = authentication.getName();
        String content = request.get("content");

        CommentDto comment = communityService.updateComment(commentId, username, content);
        return ResponseEntity.ok(comment);
    }

    /**
     * 댓글 삭제 (본인만 가능)
     * DELETE /api/community/comments/456
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {

        String username = authentication.getName();
        communityService.deleteComment(commentId, username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "댓글이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 추천
     * POST /api/community/comments/456/like
     */
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<?> likeComment(
            @PathVariable Long commentId,
            Authentication authentication) {

        String username = authentication.getName();
        communityService.likeComment(commentId, username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "추천했습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 비추천
     * POST /api/community/comments/456/dislike
     */
    @PostMapping("/comments/{commentId}/dislike")
    public ResponseEntity<?> dislikeComment(
            @PathVariable Long commentId,
            Authentication authentication) {

        String username = authentication.getName();
        communityService.dislikeComment(commentId, username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "비추천했습니다.");
        return ResponseEntity.ok(response);
    }

    // ========== 스크랩 기능 ==========

    /**
     * 게시글 스크랩
     * POST /api/community/posts/123/scrap
     */
    @PostMapping("/posts/{postId}/scrap")
    public ResponseEntity<?> scrapPost(
            @PathVariable Long postId,
            Authentication authentication) {

        String username = authentication.getName();
        communityService.scrapPost(postId, username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "스크랩했습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 스크랩 취소
     * DELETE /api/community/posts/123/scrap
     */
    @DeleteMapping("/posts/{postId}/scrap")
    public ResponseEntity<?> unscrapPost(
            @PathVariable Long postId,
            Authentication authentication) {

        String username = authentication.getName();
        communityService.unscrapPost(postId, username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "스크랩을 취소했습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 내 스크랩 목록 조회
     * GET /api/community/scraps
     */
    @GetMapping("/scraps")
    public ResponseEntity<Page<PostDto>> getMyScraps(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> scraps = communityService.getMyScraps(username, pageable);

        return ResponseEntity.ok(scraps);
    }

    // ========== 신고 기능 ==========

    /**
     * 게시글 신고
     * POST /api/community/posts/123/report
     * 요청: { "reason": "욕설", "description": "상세 설명" }
     */
    @PostMapping("/posts/{postId}/report")
    public ResponseEntity<?> reportPost(
            @PathVariable Long postId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String username = authentication.getName();
        String reason = request.get("reason");
        String description = request.get("description");

        communityService.reportPost(postId, username, reason, description);

        Map<String, String> response = new HashMap<>();
        response.put("message", "신고가 접수되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 신고
     * POST /api/community/comments/456/report
     */
    @PostMapping("/comments/{commentId}/report")
    public ResponseEntity<?> reportComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String username = authentication.getName();
        String reason = request.get("reason");
        String description = request.get("description");

        communityService.reportComment(commentId, username, reason, description);

        Map<String, String> response = new HashMap<>();
        response.put("message", "신고가 접수되었습니다.");
        return ResponseEntity.ok(response);
    }

    // ========== 사용자 차단 ==========

    /**
     * 사용자 차단
     * POST /api/community/block
     * 요청: { "blockedUsername": "차단할사용자" }
     */
    @PostMapping("/block")
    public ResponseEntity<?> blockUser(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String username = authentication.getName();
        String blockedUsername = request.get("blockedUsername");

        communityService.blockUser(username, blockedUsername);

        Map<String, String> response = new HashMap<>();
        response.put("message", "사용자를 차단했습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 차단 해제
     * DELETE /api/community/block/{blockedUsername}
     */
    @DeleteMapping("/block/{blockedUsername}")
    public ResponseEntity<?> unblockUser(
            @PathVariable String blockedUsername,
            Authentication authentication) {

        String username = authentication.getName();
        communityService.unblockUser(username, blockedUsername);

        Map<String, String> response = new HashMap<>();
        response.put("message", "차단을 해제했습니다.");
        return ResponseEntity.ok(response);
    }

    // ========== 통계 및 랭킹 ==========

    /**
     * 주간 베스트 게시글
     * GET /api/community/posts/weekly-best
     */
    @GetMapping("/posts/weekly-best")
    public ResponseEntity<List<PostDto>> getWeeklyBestPosts() {
        List<PostDto> posts = communityService.getWeeklyBestPosts();
        return ResponseEntity.ok(posts);
    }

    /**
     * 월간 베스트 게시글
     * GET /api/community/posts/monthly-best
     */
    @GetMapping("/posts/monthly-best")
    public ResponseEntity<List<PostDto>> getMonthlyBestPosts() {
        List<PostDto> posts = communityService.getMonthlyBestPosts();
        return ResponseEntity.ok(posts);
    }

    /**
     * 활동 왕성 유저 조회
     * GET /api/community/users/top-active
     */
    @GetMapping("/users/top-active")
    public ResponseEntity<List<Map<String, Object>>> getTopActiveUsers() {
        List<Map<String, Object>> users = communityService.getTopActiveUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 내가 작성한 게시글 목록
     * GET /api/community/my-posts
     */
    @GetMapping("/my-posts")
    public ResponseEntity<Page<PostDto>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> posts = communityService.getMyPosts(username, pageable);

        return ResponseEntity.ok(posts);
    }

    /**
     * 내가 작성한 댓글 목록
     * GET /api/community/my-comments
     */
    @GetMapping("/my-comments")
    public ResponseEntity<Page<CommentDto>> getMyComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentDto> comments = communityService.getMyComments(username, pageable);

        return ResponseEntity.ok(comments);
    }

    // ========== 관리자 기능 ==========

    /**
     * 게시글 블라인드 처리 (관리자만)
     * POST /api/community/posts/123/blind
     */
    @PostMapping("/posts/{postId}/blind")
    public ResponseEntity<?> blindPost(
            @PathVariable Long postId,
            Authentication authentication) {

        String username = authentication.getName();
        communityService.blindPost(postId, username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "게시글을 블라인드 처리했습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 블라인드 해제 (관리자만)
     * DELETE /api/community/posts/123/blind
     */
    @DeleteMapping("/posts/{postId}/blind")
    public ResponseEntity<?> unblindPost(
            @PathVariable Long postId,
            Authentication authentication) {

        String username = authentication.getName();
        communityService.unblindPost(postId, username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "블라인드를 해제했습니다.");
        return ResponseEntity.ok(response);
    }
}