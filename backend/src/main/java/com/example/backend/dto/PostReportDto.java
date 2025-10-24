package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostReportDto {
    private Long reportId;
    private Long postId;
    private String reporterUsername;
    private String reason;
    private String description;
    private String status; // PENDING, PROCESSED, REJECTED
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
