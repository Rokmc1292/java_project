package com.example.backend.controller;

import com.example.backend.dto.ChatMessageDto;
import com.example.backend.entity.ChatMessage;
import com.example.backend.entity.Chatroom;
import com.example.backend.entity.User;
import com.example.backend.repository.ChatMessageRepository;
import com.example.backend.repository.ChatroomRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * WebSocket 채팅 컨트롤러
 * STOMP over WebSocket을 사용한 실시간 채팅
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatroomRepository chatroomRepository;
    private final UserRepository userRepository;

    /**
     * 채팅 메시지 전송
     * 클라이언트가 /app/chat/{chatroomId} 로 메시지를 보내면
     * /topic/chatroom/{chatroomId} 를 구독하는 모든 클라이언트에게 브로드캐스트
     */
    @MessageMapping("/chat/{chatroomId}")
    @SendTo("/topic/chatroom/{chatroomId}")
    public ChatMessageDto sendMessage(
            @DestinationVariable Long chatroomId,
            @Payload Map<String, String> payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        try {
            String username = payload.get("username");
            String message = payload.get("message");

            log.info("WebSocket 메시지 수신 - chatroomId: {}, username: {}, message: {}",
                    chatroomId, username, message);

            // 채팅방 조회
            Chatroom chatroom = chatroomRepository.findById(chatroomId)
                    .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

            // 사용자 조회
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 메시지 저장
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setChatroom(chatroom);
            chatMessage.setUser(user);
            chatMessage.setMessage(message);
            chatMessage.setMessageType("USER");

            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

            // DTO로 변환하여 반환
            ChatMessageDto dto = new ChatMessageDto();
            dto.setMessageId(savedMessage.getMessageId());
            dto.setChatroomId(chatroomId);
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());
            dto.setUserTier(user.getTier());
            dto.setIsAdmin(user.getIsAdmin());
            dto.setMessage(savedMessage.getMessage());
            dto.setMessageType(savedMessage.getMessageType());
            dto.setCreatedAt(savedMessage.getCreatedAt());

            log.info("WebSocket 메시지 전송 완료 - messageId: {}", dto.getMessageId());

            return dto;

        } catch (Exception e) {
            log.error("WebSocket 메시지 처리 중 오류 발생", e);

            // 에러 메시지 반환
            ChatMessageDto errorDto = new ChatMessageDto();
            errorDto.setMessageType("ERROR");
            errorDto.setMessage("메시지 전송에 실패했습니다.");
            errorDto.setCreatedAt(LocalDateTime.now());
            return errorDto;
        }
    }
}
