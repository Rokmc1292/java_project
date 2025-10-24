package com.example.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreatePostRequest {
    private Long categoryId;
    private String title;
    private String content;
    private List<PostAttachmentDto> attachments;
}
