package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 사용자 작성 댓글 DTO
 */
@Data
public class UserCommentDto {
    private Long commentId;                 // 댓글 ID
    private Long postId;                    // 게시글 ID
    private String postTitle;               // 게시글 제목
    private String content;                 // 댓글 내용
    private Integer likeCount;              // 추천수
    private Boolean isDeleted;              // 삭제 여부
    private LocalDateTime createdAt;        // 작성일
}