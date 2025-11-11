package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자용 게시글 목록 DTO
 */
@Data
public class AdminPostDto {
    private Long postId;
    private String categoryName;
    private String title;
    private String authorUsername;
    private String authorNickname;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer reportCount;             // 신고 받은 횟수
    private Boolean isBlinded;               // 블라인드 여부
    private Boolean isNotice;
    private LocalDateTime createdAt;
}
