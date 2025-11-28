package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDetailDto {
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
    private Boolean isReported;  // 현재 사용자가 신고했는지 여부
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PostAttachmentDto> attachments;
}