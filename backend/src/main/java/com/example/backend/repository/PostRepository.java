package com.example.backend.repository;

import com.example.backend.entity.Post;
import com.example.backend.entity.BoardCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByIsBlindedFalseOrderByIsNoticeDescCreatedAtDesc(Pageable pageable);
    Page<Post> findByCategoryAndIsBlindedFalseOrderByIsNoticeDescCreatedAtDesc(BoardCategory category, Pageable pageable);
    Page<Post> findByIsPopularTrueAndIsBlindedFalseOrderByLikeCountDescCreatedAtDesc(Pageable pageable);
    Page<Post> findByCategoryAndIsPopularTrueAndIsBlindedFalseOrderByLikeCountDescCreatedAtDesc(BoardCategory category, Pageable pageable);
}
