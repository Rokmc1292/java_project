package com.example.backend.repository;

import com.example.backend.entity.BoardCategory;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 전체 게시글 조회 (블라인드 제외)
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Post> findByIsBlindedFalseOrderByIsNoticeDescCreatedAtDesc(Pageable pageable);

    // 카테고리별 게시글 조회
    Page<Post> findByCategoryOrderByCreatedAtDesc(BoardCategory category, Pageable pageable);
    Page<Post> findByCategoryAndIsBlindedFalseOrderByIsNoticeDescCreatedAtDesc(BoardCategory category, Pageable pageable);

    // 사용자별 게시글 조회
    Page<Post> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 검색
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword% ORDER BY p.createdAt DESC")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 인기글 (전체)
    Page<Post> findByIsPopularTrueOrderByLikeCountDescCreatedAtDesc(Pageable pageable);
    Page<Post> findByIsPopularTrueAndIsBlindedFalseOrderByLikeCountDescCreatedAtDesc(Pageable pageable);

    // 인기글 (카테고리별)
    Page<Post> findByCategoryAndIsPopularTrueOrderByLikeCountDescCreatedAtDesc(BoardCategory category, Pageable pageable);
    Page<Post> findByCategoryAndIsPopularTrueAndIsBlindedFalseOrderByLikeCountDescCreatedAtDesc(BoardCategory category, Pageable pageable);

    // 공지사항
    List<Post> findByIsNoticeTrueOrderByCreatedAtDesc();

    // 베스트 게시글
    List<Post> findByIsBestTrueOrderByLikeCountDescCreatedAtDesc(Pageable pageable);

    // 기간별 베스트
    @Query("SELECT p FROM Post p WHERE p.createdAt >= :startDate ORDER BY p.likeCount DESC, p.createdAt DESC")
    List<Post> findBestPostsByPeriod(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    // 주간 베스트
    @Query("SELECT p FROM Post p WHERE p.createdAt >= :weekAgo AND p.isBlinded = false ORDER BY p.likeCount DESC, p.createdAt DESC")
    List<Post> findWeeklyBest(@Param("weekAgo") LocalDateTime weekAgo, Pageable pageable);

    // 월간 베스트
    @Query("SELECT p FROM Post p WHERE p.createdAt >= :monthAgo AND p.isBlinded = false ORDER BY p.likeCount DESC, p.createdAt DESC")
    List<Post> findMonthlyBest(@Param("monthAgo") LocalDateTime monthAgo, Pageable pageable);

    // 사용자 통계
    long countByUser(User user);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user = :user AND p.createdAt >= :startDate")
    long countByUserAndCreatedAtAfter(@Param("user") User user, @Param("startDate") LocalDateTime startDate);

    Page<Post> findByUser(User user, Pageable pageable);
}