package com.example.backend.dto;

import lombok.Data;

@Data
public class PostStatisticsDto {
    private Long postId;
    private String title;
    private String categoryName;
    private String authorNickname;
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;
    private String period; // WEEKLY, MONTHLY
    private Integer rank;
}
