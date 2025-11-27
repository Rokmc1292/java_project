package com.example.backend.controller;

import com.example.backend.dto.NewsDto;
import com.example.backend.service.NewsCrawlerService;
import com.example.backend.service.NewsService;
import com.example.backend.service.NewsCleanupService;  // 추가!
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;
    private final NewsCrawlerService newsCrawlerService;
    private final NewsCleanupService newsCleanupService;  // 추가!

    /***
     * 전체 뉴스 조회
     */
    @GetMapping
    public ResponseEntity<Page<NewsDto>> getAllNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsDto> news = newsService.getAllNews(pageable, userId);
        return ResponseEntity.ok(news);
    }

    /**
     * 종목별 뉴스 조회
     */
    @GetMapping("/sport/{sportName}")
    public ResponseEntity<Page<NewsDto>> getNewsBySport(
            @PathVariable String sportName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsDto> news = newsService.getNewsBySport(sportName, pageable, userId);
        return ResponseEntity.ok(news);
    }

    /**
     * 인기 뉴스 조회
     */
    @GetMapping("/popular")
    public ResponseEntity<List<NewsDto>> getPopularNews(@RequestParam(required = false) Long userId) {
        List<NewsDto> news = newsService.getPopularNews(userId);
        return ResponseEntity.ok(news);
    }

    /**
     * 뉴스 상세 조회
     */
    @GetMapping("/{newsId}")
    public ResponseEntity<NewsDto> getNews(
            @PathVariable Long newsId,
            @RequestParam(required = false) Long userId) {
        NewsDto news = newsService.getNews(newsId, userId);
        return ResponseEntity.ok(news);
    }

    /**
     * 뉴스 검색
     */
    @GetMapping("/search")
    public ResponseEntity<Page<NewsDto>> searchNews(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsDto> news = newsService.searchNews(keyword, pageable, userId);
        return ResponseEntity.ok(news);
    }

    /**
     * 종목별 뉴스 검색
     */
    @GetMapping("/search/{sportName}")
    public ResponseEntity<Page<NewsDto>> searchNewsBySport(
            @PathVariable String sportName,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsDto> news = newsService.searchNewsBySport(sportName, keyword, pageable, userId);
        return ResponseEntity.ok(news);
    }

    /**
     * 조회수 증가
     */
    @PutMapping("/{newsId}/view")
    public ResponseEntity<Map<String, Object>> increaseViewCount(@PathVariable Long newsId) {
        newsService.increaseViewCount(newsId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 뉴스 크롤링 (수동 실행)
     */
    @PostMapping("/crawl")
    public ResponseEntity<String> crawlNews() {
        newsCrawlerService.crawlNewsManually();
        return ResponseEntity.ok("뉴스 크롤링이 시작되었습니다.");
    }

    /**
     * 뉴스 자동 정리 (수동 실행)
     */
    @PostMapping("/cleanup")
    public ResponseEntity<String> cleanupNews() {
        String result = newsCleanupService.manualCleanup();
        return ResponseEntity.ok(result);
    }
}