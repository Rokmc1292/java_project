package com.example.backend.service;

import com.example.backend.dto.NewsDto;
import com.example.backend.entity.News;
import com.example.backend.entity.Sport;
import com.example.backend.repository.NewsRepository;
import com.example.backend.repository.SportRepository;
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

    /**
     * 전체 뉴스 조회
     */
    @Transactional(readOnly = true)
    public Page<NewsDto> getAllNews(Pageable pageable) {
        return newsRepository.findAllByOrderByPublishedAtDesc(pageable)
                .map(this::convertToDto);
    }

    /**
     * 종목별 뉴스 조회
     */
    @Transactional(readOnly = true)
    public Page<NewsDto> getNewsBySport(String sportName, Pageable pageable) {
        Sport sport = sportRepository.findBySportName(sportName)
                .orElseThrow(() -> new RuntimeException("종목을 찾을 수 없습니다."));

        return newsRepository.findBySportOrderByPublishedAtDesc(sport, pageable)
                .map(this::convertToDto);
    }

    /**
     * 인기 뉴스 조회 (TOP 10)
     */
    @Transactional(readOnly = true)
    public List<NewsDto> getPopularNews() {
        return newsRepository.findTop10ByOrderByViewCountDescPublishedAtDesc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 뉴스 상세 조회
     */
    @Transactional
    public NewsDto getNews(Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다."));

        // 조회수 증가
        news.setViewCount(news.getViewCount() + 1);
        newsRepository.save(news);

        return convertToDto(news);
    }

    /**
     * Entity를 DTO로 변환
     */
    private NewsDto convertToDto(News news) {
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
        return dto;
    }
}
