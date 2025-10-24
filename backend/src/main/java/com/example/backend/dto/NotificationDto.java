package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Long notificationId;
    private String type; // COMMENT, REPLY, POPULAR_POST, LIKE
    private String content;
    private Long relatedPostId;
    private Long relatedCommentId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
