package com.example.backend.repository;

import com.example.backend.entity.Post;
import com.example.backend.entity.PostScrap;
import com.example.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 게시글 스크랩 Repository
 */
@Repository
public interface PostScrapRepository extends JpaRepository<PostScrap, Long> {
    // 특정 게시글에 대한 사용자의 스크랩 조회
    Optional<PostScrap> findByPostAndUser(Post post, User user);

    // 사용자의 스크랩 목록 조회 (최신순)
    Page<PostScrap> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}