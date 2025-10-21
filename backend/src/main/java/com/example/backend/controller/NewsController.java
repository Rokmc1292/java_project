package com.example.backend.controller;

import com.example.backend.dto.NewsDto;
import com.example.backend.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 스포츠 뉴스 Controller
 */
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class NewsController {

    private final NewsService newsService;

    /**
     * 전체 뉴스 조회
     * GET /api/news?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<NewsDto>> getAllNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsDto> news = newsService.getAllNews(pageable);
        return ResponseEntity.ok(news);
    }

    /**
     * 종목별 뉴스 조회
     * GET /api/news/sport/FOOTBALL?page=0&size=20
     */
    @GetMapping("/sport/{sportName}")
    public ResponseEntity<Page<NewsDto>> getNewsBySport(
            @PathVariable String sportName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsDto> news = newsService.getNewsBySport(sportName, pageable);
        return ResponseEntity.ok(news);
    }

    /**
     * 인기 뉴스 조회 (TOP 10)
     * GET /api/news/popular
     */
    @GetMapping("/popular")
    public ResponseEntity<List<NewsDto>> getPopularNews() {
        List<NewsDto> news = newsService.getPopularNews();
        return ResponseEntity.ok(news);
    }

    /**
     * 뉴스 상세 조회
     * GET /api/news/1
     */
    @GetMapping("/{newsId}")
    public ResponseEntity<NewsDto> getNews(@PathVariable Long newsId) {
        NewsDto news = newsService.getNews(newsId);
        return ResponseEntity.ok(news);
    }
}
