package com.example.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class PostRequest {
    private Long categoryId;
    private String categoryName;
    private String title;
    private String content;
    private List<AttachmentRequest> attachments;
}