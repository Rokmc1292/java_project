package com.example.backend.controller;

import com.example.backend.dto.NewsDto;
import com.example.backend.service.NewsLikeService;
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
@CrossOrigin(origins = "http://localhost:5173")
public class NewsLikeController {

    private final NewsLikeService newsLikeService;

    /**
     * 뉴스 좋아요 토글
     * POST /api/news/1/like
     * Body: { "userId": 1 }
     */
    @PostMapping("/{newsId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long newsId,
            @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        boolean isLiked = newsLikeService.toggleLike(newsId, userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "isLiked", isLiked,
                "message", isLiked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다."
        ));
    }

    /**
     * 내가 좋아요한 뉴스 목록
     * GET /api/news/liked?userId=1&page=0&size=20
     */
    @GetMapping("/liked")
    public ResponseEntity<Page<NewsDto>> getLikedNews(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsDto> news = newsLikeService.getLikedNews(userId, pageable);
        return ResponseEntity.ok(news);
    }
}
