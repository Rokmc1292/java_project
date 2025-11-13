package com.example.backend.repository;

import com.example.backend.entity.News;
import com.example.backend.entity.Sport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    Page<News> findAllByOrderByPublishedAtDesc(Pageable pageable);

    Page<News> findBySportOrderByPublishedAtDesc(Sport sport, Pageable pageable);

    List<News> findTop10ByOrderByViewCountDescPublishedAtDesc();

    boolean existsBySourceUrl(String sourceUrl);

    // 검색 기능 추가
    @Query("SELECT n FROM News n WHERE " +
            "LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY n.publishedAt DESC")
    Page<News> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT n FROM News n WHERE n.sport = :sport AND " +
            "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY n.publishedAt DESC")
    Page<News> searchBySportAndKeyword(@Param("sport") Sport sport,
                                       @Param("keyword") String keyword,
                                       Pageable pageable);

    // 전체 뉴스 오래된 순으로 조회
    List<News> findAllByOrderByPublishedAtAsc();

    // 종목별 개수 조회
    long countBySport(Sport sport);

    // 종목별 오래된 순으로 N개 조회
    @Query("SELECT n FROM News n WHERE n.sport = :sport ORDER BY n.publishedAt ASC")
    List<News> findTopNBySportOrderByPublishedAtAsc(@Param("sport") Sport sport, Pageable pageable);

    // 종목별 특정 날짜 이전 뉴스 조회
    List<News> findBySportAndPublishedAtBefore(Sport sport, LocalDateTime date);

    default List<News> findTopNBySportOrderByPublishedAtAsc(Sport sport, int limit) {
        return findTopNBySportOrderByPublishedAtAsc(sport, PageRequest.of(0, limit));
    }
}
