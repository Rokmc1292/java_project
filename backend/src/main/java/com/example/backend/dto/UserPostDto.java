package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 사용자 작성 게시글 DTO
 */
@Data
public class UserPostDto {
    private Long postId;                    // 게시글 ID
    private String categoryName;            // 카테고리 이름
    private String title;                   // 게시글 제목
    private Integer viewCount;              // 조회수
    private Integer likeCount;              // 추천수
    private Integer commentCount;           // 댓글 수
    private Boolean isPopular;              // 인기글 여부
    private Boolean isBest;                 // 베스트 게시글 여부
    private LocalDateTime createdAt;        // 작성일
}