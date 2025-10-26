package com.example.backend.repository;

import com.example.backend.entity.Post;
import com.example.backend.entity.BoardCategory;
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
    // 기존 메서드들...
    Page<Post> findByIsBlindedFalseOrderByIsNoticeDescCreatedAtDesc(Pageable pageable);
    Page<Post> findByCategoryAndIsBlindedFalseOrderByIsNoticeDescCreatedAtDesc(BoardCategory category, Pageable pageable);
    Page<Post> findByIsPopularTrueAndIsBlindedFalseOrderByLikeCountDescCreatedAtDesc(Pageable pageable);
    Page<Post> findByCategoryAndIsPopularTrueAndIsBlindedFalseOrderByLikeCountDescCreatedAtDesc(BoardCategory category, Pageable pageable);

    // 추가 메서드들

    // 게시글 검색 (제목, 내용, 작성자 닉네임)
    @Query("SELECT p FROM Post p JOIN p.user u WHERE " +
            "(p.title LIKE %:keyword% OR p.content LIKE %:keyword% OR u.nickname LIKE %:keyword%) " +
            "AND p.isBlinded = false " +
            "ORDER BY p.isNotice DESC, p.createdAt DESC")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 주간 베스트 게시글 (최근 7일, 추천수 기준)
    @Query("SELECT p FROM Post p WHERE p.createdAt >= :weekAgo AND p.isBlinded = false " +
            "ORDER BY p.likeCount DESC, p.createdAt DESC")
    List<Post> findWeeklyBest(@Param("weekAgo") LocalDateTime weekAgo, Pageable pageable);

    // 월간 베스트 게시글 (최근 30일, 추천수 기준)
    @Query("SELECT p FROM Post p WHERE p.createdAt >= :monthAgo AND p.isBlinded = false " +
            "ORDER BY p.likeCount DESC, p.createdAt DESC")
    List<Post> findMonthlyBest(@Param("monthAgo") LocalDateTime monthAgo, Pageable pageable);

    // 사용자가 작성한 게시글 조회
    Page<Post> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 특정 기간 이후 사용자가 작성한 게시글 수
    long countByUserAndCreatedAtAfter(User user, LocalDateTime after);
}