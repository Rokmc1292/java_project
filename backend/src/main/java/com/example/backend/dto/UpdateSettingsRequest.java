package com.example.backend.dto;

import lombok.Data;

/**
 * 알림 설정 변경 요청 DTO
 */
@Data
public class UpdateSettingsRequest {
    private Boolean commentNotification;            // 댓글 알림
    private Boolean replyNotification;              // 대댓글 알림
    private Boolean popularPostNotification;        // 인기글 진입 알림
    private Boolean predictionResultNotification;   // 예측 결과 알림
}