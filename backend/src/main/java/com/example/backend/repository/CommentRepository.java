package com.example.backend.repository;

import com.example.backend.entity.Comment;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 기존 메서드들...
    List<Comment> findByPostAndIsDeletedFalseOrderByCreatedAtAsc(Post post);
    List<Comment> findByPostAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtAsc(Post post);

    // 추가 메서드들

    // 대댓글 조회
    List<Comment> findByParentCommentAndIsDeletedFalseOrderByCreatedAtAsc(Comment parentComment);

    // 사용자가 작성한 댓글 조회
    Page<Comment> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 특정 기간 이후 사용자가 작성한 댓글 수
    long countByUserAndCreatedAtAfter(User user, LocalDateTime after);

    Page<Comment> findByUser(User user, Pageable pageable);
}