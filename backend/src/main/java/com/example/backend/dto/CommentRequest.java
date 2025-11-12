package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {
    private Long parentCommentId;

    @NotBlank(message = "댓글 내용을 입력해주세요.")
    @Size(max = 300, message = "댓글은 최대 300자까지 입력 가능합니다.")
    private String content;
}