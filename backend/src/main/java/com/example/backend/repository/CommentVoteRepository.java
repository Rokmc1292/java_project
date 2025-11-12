package com.example.backend.repository;

import com.example.backend.entity.Comment;
import com.example.backend.entity.CommentVote;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 댓글 추천/비추천 Repository
 */
@Repository
public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {
    // 특정 댓글에 대한 사용자의 투표 조회
    Optional<CommentVote> findByCommentAndUser(Comment comment, User user);

    @Transactional
    void deleteByComment(Comment comment);
}