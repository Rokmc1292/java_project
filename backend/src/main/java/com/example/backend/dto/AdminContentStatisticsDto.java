package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 콘텐츠 통계 DTO
 */
@Data
public class AdminContentStatisticsDto {
    private String period;
    private List<ContentStatData> data;

    @Data
    public static class ContentStatData {
        private String date;
        private Integer newPosts;            // 신규 게시글
        private Integer newComments;         // 신규 댓글
        private Integer totalPosts;          // 누적 게시글
        private Integer totalComments;       // 누적 댓글
    }
}
