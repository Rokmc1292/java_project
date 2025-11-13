package com.example.backend.service;

import com.example.backend.dto.NewsDto;
import com.example.backend.entity.News;
import com.example.backend.entity.Sport;
import com.example.backend.entity.User;
import com.example.backend.repository.NewsLikeRepository;
import com.example.backend.repository.NewsRepository;
import com.example.backend.repository.SportRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final SportRepository sportRepository;
    private final NewsLikeRepository newsLikeRepository;
    private final UserRepository userRepository;

    /**
     * 전체 뉴스 조회
     */
    @Transactional(readOnly = true)
    public Page<NewsDto> getAllNews(Pageable pageable, Long userId) {
        return newsRepository.findAllByOrderByPublishedAtDesc(pageable)
                .map(news -> convertToDto(news, userId));
    }

    /**
     * 종목별 뉴스 조회
     */
    @Transactional(readOnly = true)
    public Page<NewsDto> getNewsBySport(String sportName, Pageable pageable, Long userId) {
        Sport sport = sportRepository.findBySportName(sportName)
                .orElseThrow(() -> new RuntimeException("종목을 찾을 수 없습니다."));

        return newsRepository.findBySportOrderByPublishedAtDesc(sport, pageable)
                .map(news -> convertToDto(news, userId));
    }

    /**
     * 인기 뉴스 조회 (TOP 10)
     */
    @Transactional(readOnly = true)
    public List<NewsDto> getPopularNews(Long userId) {
        return newsRepository.findTop10ByOrderByViewCountDescPublishedAtDesc()
                .stream()
                .map(news -> convertToDto(news, userId))
                .collect(Collectors.toList());
    }

    /**
     * 뉴스 상세 조회 (조회수 증가 포함)
     */
    @Transactional
    public NewsDto getNews(Long newsId, Long userId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다."));

        // 조회수 증가
        news.setViewCount(news.getViewCount() + 1);
        newsRepository.save(news);

        return convertToDto(news, userId);
    }

    /**
     * 뉴스 검색
     */
    @Transactional(readOnly = true)
    public Page<NewsDto> searchNews(String keyword, Pageable pageable, Long userId) {
        return newsRepository.searchByKeyword(keyword, pageable)
                .map(news -> convertToDto(news, userId));
    }

    /**
     * 종목별 뉴스 검색
     */
    @Transactional(readOnly = true)
    public Page<NewsDto> searchNewsBySport(String sportName, String keyword, Pageable pageable, Long userId) {
        Sport sport = sportRepository.findBySportName(sportName)
                .orElseThrow(() -> new RuntimeException("종목을 찾을 수 없습니다."));

        return newsRepository.searchBySportAndKeyword(sport, keyword, pageable)
                .map(news -> convertToDto(news, userId));
    }

    /**
     * 조회수만 증가 (상세 조회 없이)
     */
    @Transactional
    public void increaseViewCount(Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다."));

        news.setViewCount(news.getViewCount() + 1);
        newsRepository.save(news);
    }

    /**
     * Entity를 DTO로 변환 (좋아요 상태 포함)
     */
    private NewsDto convertToDto(News news, Long userId) {
        NewsDto dto = new NewsDto();
        dto.setNewsId(news.getNewsId());
        if (news.getSport() != null) {
            dto.setSportName(news.getSport().getSportName());
            dto.setSportDisplayName(news.getSport().getDisplayName());
        }
        dto.setTitle(news.getTitle());
        dto.setContent(news.getContent());
        dto.setThumbnailUrl(news.getThumbnailUrl());
        dto.setSourceUrl(news.getSourceUrl());
        dto.setSourceName(news.getSourceName());
        dto.setPublishedAt(news.getPublishedAt());
        dto.setViewCount(news.getViewCount());
        dto.setLikeCount(news.getLikeCount());

        // 좋아요 상태 확인
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                dto.setIsLiked(newsLikeRepository.existsByNewsAndUser(news, user));
            } else {
                dto.setIsLiked(false);
            }
        } else {
            dto.setIsLiked(false);
        }

        return dto;
    }
}