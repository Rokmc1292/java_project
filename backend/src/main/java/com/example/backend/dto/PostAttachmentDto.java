package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostAttachmentDto {
    private Long attachmentId;
    private String fileType; // IMAGE, VIDEO, LINK
    private String fileUrl;
    private String fileName;
    private LocalDateTime createdAt;
}
