package com.example.backend.dto;

import lombok.Data;

@Data
public class PostAttachmentDto {
    private Long attachmentId;
    private String fileType;
    private String fileUrl;
    private String fileName;
}