package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NewsDto {
    private Long newsId;
    private String sportName;
    private String sportDisplayName;
    private String title;
    private String content;
    private String thumbnailUrl;
    private String sourceUrl;
    private String sourceName;
    private LocalDateTime publishedAt;
    private Integer viewCount;
}
