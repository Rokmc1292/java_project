package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 예측 통계 DTO
 */
@Data
public class AdminPredictionStatisticsDto {
    private String period;
    private List<PredictionStatData> data;

    @Data
    public static class PredictionStatData {
        private String date;
        private Integer newPredictions;      // 신규 예측
        private Integer totalPredictions;    // 누적 예측
        private Integer correctPredictions;  // 적중 예측
        private Double accuracy;             // 정확도
    }
}
