package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageDto {
    private Long messageId;
    private Long chatroomId;
    private String username;
    private String nickname;
    private String userTier;
    private Boolean isAdmin;  // 관리자 여부
    private String message;
    private String messageType;  // USER, SYSTEM
    private LocalDateTime createdAt;
}
