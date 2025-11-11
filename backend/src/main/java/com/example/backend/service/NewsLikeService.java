package com.example.backend.service;

import com.example.backend.dto.NewsDto;
import com.example.backend.entity.News;
import com.example.backend.entity.NewsLike;
import com.example.backend.entity.User;
import com.example.backend.repository.NewsLikeRepository;
import com.example.backend.repository.NewsRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsLikeService {

    private final NewsLikeRepository newsLikeRepository;
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final NewsService newsService;

    /**
     * 뉴스 좋아요 토글
     */
    @Transactional
    public boolean toggleLike(Long newsId, Long userId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다."));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미 좋아요를 눌렀는지 확인
        if (newsLikeRepository.existsByNewsAndUser(news, user)) {
            // 좋아요 취소
            NewsLike newsLike = newsLikeRepository.findByNewsAndUser(news, user)
                    .orElseThrow();
            newsLikeRepository.delete(newsLike);
            
            // 좋아요 개수 감소
            news.setLikeCount(news.getLikeCount() - 1);
            newsRepository.save(news);
            
            log.info("뉴스 좋아요 취소: newsId={}, userId={}", newsId, userId);
            return false; // 좋아요 취소됨
        } else {
            // 좋아요 추가
            NewsLike newsLike = new NewsLike();
            newsLike.setNews(news);
            newsLike.setUser(user);
            newsLikeRepository.save(newsLike);
            
            // 좋아요 개수 증가
            news.setLikeCount(news.getLikeCount() + 1);
            newsRepository.save(news);
            
            log.info("뉴스 좋아요: newsId={}, userId={}", newsId, userId);
            return true; // 좋아요 추가됨
        }
    }

    /**
     * 사용자가 좋아요한 뉴스 목록
     */
    /**
     * 사용자가 좋아요한 뉴스 목록
     */
    @Transactional(readOnly = true)
    public Page<NewsDto> getLikedNews(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return newsLikeRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(newsLike -> {
                    News news = newsLike.getNews();
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
                    return dto;
                });
    }
}
