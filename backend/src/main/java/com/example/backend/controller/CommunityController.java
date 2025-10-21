package com.example.backend.controller;

import com.example.backend.dto.PostDto;
import com.example.backend.dto.CommentDto;
import com.example.backend.service.CommunityService;
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

    /**
     * 전체 게시글 조회
     * GET /api/community/posts?page=0&size=20
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<PostDto>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> posts = communityService.getAllPosts(pageable);
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
     * 인기 게시글 조회
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
     * 게시글 상세 조회
     * GET /api/community/posts/1
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostDto> getPost(@PathVariable Long postId) {
        PostDto post = communityService.getPost(postId);
        return ResponseEntity.ok(post);
    }

    /**
     * 게시글 생성
     * POST /api/community/posts
     */
    @PostMapping("/posts")
    public ResponseEntity<PostDto> createPost(@RequestBody Map<String, String> request) {
        String categoryName = request.get("categoryName");
        String username = request.get("username");
        String title = request.get("title");
        String content = request.get("content");

        PostDto post = communityService.createPost(categoryName, username, title, content);
        return ResponseEntity.ok(post);
    }

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
}
