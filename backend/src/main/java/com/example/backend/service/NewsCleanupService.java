package com.example.backend.service;

import com.example.backend.entity.News;
import com.example.backend.entity.Sport;
import com.example.backend.repository.NewsRepository;
import com.example.backend.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsCleanupService {

    private final NewsRepository newsRepository;
    private final SportRepository sportRepository;

    // 전체 뉴스 최대 개수 (20페이지 × 10개)
    private static final int MAX_TOTAL_NEWS = 200;
    // 종목별 최대 개수 (200 ÷ 5종목 = 40개)
    private static final int MAX_NEWS_PER_SPORT = 40;
    // 종목별 최소 개수 (UFC 등 비인기 종목 보장)
    private static final int MIN_NEWS_PER_SPORT = 20;
    // 자동 삭제 기준 일수
    private static final int DELETE_AFTER_DAYS = 30;

    /**
     * 매일 새벽 3시에 자동 실행
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldNews() {
        log.info("===== 뉴스 자동 정리 시작 =====");
        
        // 전체 뉴스 개수 확인
        long totalNews = newsRepository.count();
        log.info("현재 전체 뉴스 개수: {}", totalNews);
        
        List<Sport> sports = sportRepository.findAll();
        int totalDeleted = 0;

        for (Sport sport : sports) {
            int deleted = cleanupNewsBySport(sport);
            totalDeleted += deleted;
        }

        // 전체 개수가 여전히 200개를 초과하면 추가 정리
        totalNews = newsRepository.count();
        if (totalNews > MAX_TOTAL_NEWS) {
            int excess = (int)(totalNews - MAX_TOTAL_NEWS);
            log.info("전체 개수가 {}개 초과 - 추가로 {}개 삭제", MAX_TOTAL_NEWS, excess);
            totalDeleted += deleteOldestNewsGlobally(excess);
        }

        log.info("===== 뉴스 자동 정리 완료: 총 {}개 삭제 =====", totalDeleted);
        log.info("남은 전체 뉴스 개수: {}", newsRepository.count());
    }

    /**
     * 종목별 뉴스 정리
     */
    private int cleanupNewsBySport(Sport sport) {
        long totalCount = newsRepository.countBySport(sport);
        
        log.info("[{}] 현재 개수: {}", sport.getDisplayName(), totalCount);

        if (totalCount <= MIN_NEWS_PER_SPORT) {
            log.info("[{}] 최소 개수({})보다 적음 - 유지", 
                sport.getDisplayName(), MIN_NEWS_PER_SPORT);
            return 0;
        }

        int deleteCount = 0;

        // 1. 종목별 최대 개수(40개) 초과 시 삭제
        if (totalCount > MAX_NEWS_PER_SPORT) {
            int excessCount = (int)(totalCount - MAX_NEWS_PER_SPORT);
            List<News> oldestNews = newsRepository
                .findTopNBySportOrderByPublishedAtAsc(sport, excessCount);
            
            newsRepository.deleteAll(oldestNews);
            deleteCount += oldestNews.size();
            log.info("[{}] 최대 개수({}) 초과 - {}개 삭제", 
                sport.getDisplayName(), MAX_NEWS_PER_SPORT, oldestNews.size());
        }

        // 2. 30일 이상 된 뉴스 삭제 (최소 개수는 보장)
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(DELETE_AFTER_DAYS);
        long currentCount = newsRepository.countBySport(sport);
        
        if (currentCount > MIN_NEWS_PER_SPORT) {
            List<News> oldNews = newsRepository
                .findBySportAndPublishedAtBefore(sport, cutoffDate);
            
            int canDelete = (int)Math.min(oldNews.size(), 
                currentCount - MIN_NEWS_PER_SPORT);
            
            if (canDelete > 0) {
                List<News> toDelete = oldNews.subList(0, canDelete);
                newsRepository.deleteAll(toDelete);
                deleteCount += toDelete.size();
                log.info("[{}] {}일 이상 뉴스 {}개 삭제", 
                    sport.getDisplayName(), DELETE_AFTER_DAYS, toDelete.size());
            }
        }

        log.info("[{}] 정리 완료 - {}개 삭제, 남은 개수: {}", 
            sport.getDisplayName(), deleteCount, 
            newsRepository.countBySport(sport));

        return deleteCount;
    }

    /**
     * 전체에서 가장 오래된 뉴스 삭제 (각 종목 최소 개수는 보장)
     */
    private int deleteOldestNewsGlobally(int count) {
        List<Sport> sports = sportRepository.findAll();
        int deleted = 0;

        // 각 종목에서 최소 개수를 초과하는 뉴스 중 가장 오래된 것부터 삭제
        List<News> candidateNews = newsRepository.findAllByOrderByPublishedAtAsc();
        
        for (News news : candidateNews) {
            if (deleted >= count) break;
            
            Sport sport = news.getSport();
            long sportCount = newsRepository.countBySport(sport);
            
            // 해당 종목이 최소 개수보다 많으면 삭제 가능
            if (sportCount > MIN_NEWS_PER_SPORT) {
                newsRepository.delete(news);
                deleted++;
                log.info("[전체 정리] [{}] 오래된 뉴스 삭제: {}", 
                    sport.getDisplayName(), news.getTitle());
            }
        }

        return deleted;
    }

    /**
     * 수동 실행용 (테스트)
     */
    @Transactional
    public String manualCleanup() {
        cleanupOldNews();
        
        long totalCount = newsRepository.count();
        StringBuilder result = new StringBuilder();
        result.append("뉴스 정리 완료!\n");
        result.append("전체 뉴스 개수: ").append(totalCount).append("개\n\n");
        
        List<Sport> sports = sportRepository.findAll();
        result.append("종목별 현황:\n");
        for (Sport sport : sports) {
            long count = newsRepository.countBySport(sport);
            result.append(String.format("- %s: %d개\n", sport.getDisplayName(), count));
        }
        
        return result.toString();
    }
}