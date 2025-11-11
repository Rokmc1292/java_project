package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 승부예측 생성 요청 DTO
 * - 프론트엔드에서 예측 제출할 때 사용
 */
@Getter
@Setter
public class PredictionRequest {
    private Long matchId; // 경기 ID
    private String predictedResult; // 예측 결과 (HOME, DRAW, AWAY)
    private String comment; // 예측 코멘트 (필수)
}