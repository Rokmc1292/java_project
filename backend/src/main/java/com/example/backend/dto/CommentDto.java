package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 댓글 DTO
 * 댓글 정보를 클라이언트와 주고받기 위한 데이터 전송 객체
 *
 * 파일 위치: backend/src/main/java/com/example/backend/dto/CommentDto.java
 */
@Getter
@Setter
public class CommentDto {
    private Long commentId;           // 댓글 ID
    private Long postId;              // 게시글 ID
    private String username;          // 작성자 아이디
    private String nickname;          // 작성자 닉네임
    private String content;           // 댓글 내용
    private Integer likeCount;        // 추천 수
    private Integer dislikeCount;     // 비추천 수
    private Boolean isDeleted;        // 삭제 여부
    private LocalDateTime createdAt;  // 작성일
    private LocalDateTime updatedAt;  // 수정일
    private List<CommentDto> replies; // 대댓글 목록
}