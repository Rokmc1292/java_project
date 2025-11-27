package com.example.backend.controller;

import com.example.backend.dto.NewsDto;
import com.example.backend.service.NewsLikeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 뉴스 좋아요 Controller
 */
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsLikeController {

    private final NewsLikeService newsLikeService;

    private Long getUserIdFromSession(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }
        return userId;
    }

    /**
     * 뉴스 좋아요 토글
     * POST /api/news/1/like
     */
    @PostMapping("/{newsId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long newsId,
            HttpSession session) {
        Long userId = getUserIdFromSession(session);
        boolean isLiked = newsLikeService.toggleLike(newsId, userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "isLiked", isLiked,
                "message", isLiked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다."
        ));
    }

    /**
     * 내가 좋아요한 뉴스 목록
     * GET /api/news/liked?page=0&size=20
     */
    @GetMapping("/liked")
    public ResponseEntity<Page<NewsDto>> getLikedNews(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserIdFromSession(session);
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsDto> news = newsLikeService.getLikedNews(userId, pageable);
        return ResponseEntity.ok(news);
    }
}
