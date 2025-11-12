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

    // 게시글의 모든 댓글 조회 (삭제되지 않은 것만)
    List<Comment> findByPostAndIsDeletedFalseOrderByCreatedAtAsc(Post post);

    // 추가 메서드들
    // ⭐ 새로 추가: 삭제된 댓글도 포함
    List<Comment> findByPostAndParentCommentIsNullOrderByCreatedAtAsc(Post post);
    List<Comment> findByParentCommentOrderByCreatedAtAsc(Comment parentComment);

    // 대댓글 조회
    List<Comment> findByParentCommentAndIsDeletedFalseOrderByCreatedAtAsc(Comment parentComment);

    // 사용자가 작성한 댓글 조회
    Page<Comment> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    Page<Comment> findByUser(User user, Pageable pageable);

    // 특정 기간 이후 사용자가 작성한 댓글 수
    long countByUserAndCreatedAtAfter(User user, LocalDateTime after);

    // 사용자의 전체 댓글 수
    long countByUser(User user);

    // ========== 관리자 페이지용 추가 메서드 ==========

    /**
     * 특정 날짜 이후 작성된 댓글 수 (대시보드 통계용)
     */
    long countByCreatedAtAfter(LocalDateTime date);
}