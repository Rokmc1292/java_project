package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Long notificationId;
    private String notificationType;
    private String content;
    private String relatedType;
    private Long relatedId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
