package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
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
    private Integer likeCount;
    private Boolean isLiked; // 현재 사용자가 좋아요 눌렀는지
}
