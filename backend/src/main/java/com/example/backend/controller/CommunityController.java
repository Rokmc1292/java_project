package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.entity.BoardCategory;
import com.example.backend.repository.BoardCategoryRepository;
import com.example.backend.service.CommunityService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final BoardCategoryRepository boardCategoryRepository;

    private String getUsernameFromSession(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }
        return username;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<BoardCategoryDto>> getCategories() {
        List<BoardCategory> categories = boardCategoryRepository.findAllByOrderByDisplayOrderAsc();
        List<BoardCategoryDto> dtos = categories.stream().map(cat -> {
            BoardCategoryDto dto = new BoardCategoryDto();
            dto.setCategoryId(cat.getCategoryId());
            dto.setCategoryName(cat.getCategoryName());
            dto.setDisplayOrder(cat.getDisplayOrder());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<PostDto>> getPosts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        // categoryId가 있으면 categoryName으로 변환
        if (categoryName == null && categoryId != null) {
            BoardCategory category = boardCategoryRepository.findById(categoryId).orElse(null);
            if (category != null) {
                categoryName = category.getCategoryName();
            }
        }

        Page<PostDto> posts;

        // type에 따라 분기
        if ("popular".equals(type) || "인기글".equals(type)) {
            // 인기글 조회
            posts = communityService.getPopularPostsByCategory(categoryName, pageable);
        } else {
            // 일반 게시글 조회
            posts = communityService.getPostsByCategory(categoryName, pageable);
        }

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostDto> getPost(@PathVariable Long postId) {
        PostDto post = communityService.getPost(postId);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/posts/search")
    public ResponseEntity<Page<PostDto>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> posts = communityService.searchPosts(keyword, pageable);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/posts")
    public ResponseEntity<PostDto> createPost(
            @RequestBody PostRequest request,
            HttpSession session) {
        String username = getUsernameFromSession(session);

        // ⭐ categoryName을 직접 사용 (categoryId 변환 로직 제거)
        String categoryName = request.getCategoryName();

        // categoryName이 null이면 기본값 설정
        if (categoryName == null || categoryName.isEmpty()) {
            categoryName = "자유게시판";
        }

        log.debug("게시글 작성 요청 - 카테고리: {}, 제목: {}", categoryName, request.getTitle());

        PostDto post = communityService.createPost(username, categoryName, request.getTitle(), request.getContent());
        return ResponseEntity.ok(post);
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable Long postId,
            @RequestBody PostRequest request,
            HttpSession session) {
        String username = getUsernameFromSession(session);
        PostDto post = communityService.updatePost(postId, username, request.getTitle(), request.getContent());
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId,
            HttpSession session) {
        String username = getUsernameFromSession(session);
        communityService.deletePost(postId, username);
        return ResponseEntity.ok(Map.of("message", "게시글이 삭제되었습니다."));
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> likePost(
            @PathVariable Long postId,
            HttpSession session) {
        String username = getUsernameFromSession(session);
        communityService.likePost(postId, username);
        return ResponseEntity.ok(Map.of("message", "추천했습니다."));
    }

    @PostMapping("/posts/{postId}/dislike")
    public ResponseEntity<?> dislikePost(
            @PathVariable Long postId,
            HttpSession session) {
        String username = getUsernameFromSession(session);
        communityService.dislikePost(postId, username);
        return ResponseEntity.ok(Map.of("message", "비추천했습니다."));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long postId) {
        List<CommentDto> comments = communityService.getComments(postId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long postId,
            @RequestBody CommentRequest request,
            HttpSession session) {
        String username = getUsernameFromSession(session);
        CommentDto comment = communityService.createComment(postId, username, request.getContent(), request.getParentCommentId());
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequest request,
            HttpSession session) {
        String username = getUsernameFromSession(session);
        CommentDto comment = communityService.updateComment(commentId, username, request.getContent());
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            HttpSession session) {
        String username = getUsernameFromSession(session);
        communityService.deleteComment(commentId, username);
        return ResponseEntity.ok(Map.of("message", "댓글이 삭제되었습니다."));
    }

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<?> likeComment(
            @PathVariable Long commentId,
            HttpSession session) {
        String username = getUsernameFromSession(session);
        communityService.likeComment(commentId, username);
        return ResponseEntity.ok(Map.of("message", "추천했습니다."));
    }

    @PostMapping("/comments/{commentId}/dislike")
    public ResponseEntity<?> dislikeComment(
            @PathVariable Long commentId,
            HttpSession session) {
        String username = getUsernameFromSession(session);
        communityService.dislikeComment(commentId, username);
        return ResponseEntity.ok(Map.of("message", "비추천했습니다."));
    }

    @PostMapping("/posts/{postId}/scrap")
    public ResponseEntity<?> scrapPost(
            @PathVariable Long postId,
            HttpSession session) {
        String username = getUsernameFromSession(session);
        communityService.scrapPost(postId, username);
        return ResponseEntity.ok(Map.of("message", "스크랩했습니다."));
    }

    @DeleteMapping("/posts/{postId}/scrap")
    public ResponseEntity<?> unscrapPost(
            @PathVariable Long postId,
            HttpSession session) {
        String username = getUsernameFromSession(session);
        communityService.unscrapPost(postId, username);
        return ResponseEntity.ok(Map.of("message", "스크랩을 취소했습니다."));
    }

    @PostMapping("/posts/{postId}/report")
    public ResponseEntity<?> reportPost(
            @PathVariable Long postId,
            @RequestBody Map<String, String> request,
            HttpSession session) {
        String username = getUsernameFromSession(session);
        String reason = request.get("reason");
        String description = request.get("description");
        communityService.reportPost(postId, username, reason, description);
        return ResponseEntity.ok(Map.of("message", "신고가 접수되었습니다."));
    }

    @PostMapping("/comments/{commentId}/report")
    public ResponseEntity<?> reportComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request,
            HttpSession session) {
        String username = getUsernameFromSession(session);
        String reason = request.get("reason");
        String description = request.get("description");
        communityService.reportComment(commentId, username, reason, description);
        return ResponseEntity.ok(Map.of("message", "신고가 접수되었습니다."));
    }

    @PostMapping("/users/{targetUsername}/block")
    public ResponseEntity<?> blockUser(
            @PathVariable String targetUsername,
            HttpSession session) {
        String username = getUsernameFromSession(session);
        communityService.blockUser(username, targetUsername);
        return ResponseEntity.ok(Map.of("message", "사용자를 차단했습니다."));
    }

    @DeleteMapping("/users/{targetUsername}/block")
    public ResponseEntity<?> unblockUser(
            @PathVariable String targetUsername,
            HttpSession session) {
        String username = getUsernameFromSession(session);
        communityService.unblockUser(username, targetUsername);
        return ResponseEntity.ok(Map.of("message", "차단을 해제했습니다."));
    }
}