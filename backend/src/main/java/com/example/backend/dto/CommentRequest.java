package com.example.backend.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private Long parentCommentId;
    private String content;
}