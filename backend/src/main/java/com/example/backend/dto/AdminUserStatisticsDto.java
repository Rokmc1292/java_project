package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 통계 DTO
 */
@Data
public class AdminUserStatisticsDto {
    private String period;                   // daily, weekly, monthly
    private List<UserStatData> data;

    @Data
    public static class UserStatData {
        private String date;                 // 날짜
        private Integer newUsers;            // 신규 가입자
        private Integer activeUsers;         // 활성 사용자 (로그인/활동한 사용자)
        private Integer totalUsers;          // 누적 사용자 수
    }
}
