package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long commentId;
    private Long postId;
    private String username;
    private String nickname;
    private String userTier;
    private Long parentCommentId;
    private String content;
    private Integer likeCount;
    private Integer dislikeCount;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
