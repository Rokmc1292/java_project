package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDto {
    private Long postId;
    private String categoryName;
    private String username;
    private String nickname;
    private String userTier;
    private String profileImage;
    private String title;
    private String content;
    private Integer viewCount;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer commentCount;
    private Boolean isNotice;
    private Boolean isPopular;
    private Boolean isBest;
    private Boolean isBlinded;
    private Boolean isScraped; // 현재 사용자가 스크랩했는지 여부
    private String userLikeStatus; // "LIKE", "DISLIKE", "NONE"
    private List<PostAttachmentDto> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
