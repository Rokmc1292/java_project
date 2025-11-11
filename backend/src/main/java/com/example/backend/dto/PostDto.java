package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostDto {
    private Long postId;
    private String categoryName;
    private String username;
    private String nickname;
    private String title;
    private String content;
    private Integer viewCount;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer commentCount;
    private Boolean isNotice;
    private Boolean isPopular;
    private Boolean isBest;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
