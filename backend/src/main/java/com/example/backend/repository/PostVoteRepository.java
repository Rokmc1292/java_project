package com.example.backend.repository;

import com.example.backend.entity.Post;
import com.example.backend.entity.PostVote;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 게시글 추천/비추천 Repository
 */
@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Long> {
    // 특정 게시글에 대한 사용자의 투표 조회
    Optional<PostVote> findByPostAndUser(Post post, User user);

    // 게시글의 전체 투표 수 조회
    long countByPost(Post post);

    void deleteByPost(Post post);  // ⭐ 이 줄 추가
}