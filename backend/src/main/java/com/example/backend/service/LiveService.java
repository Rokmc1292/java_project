package com.example.backend.service;

import com.example.backend.dto.ChatMessageDto;
import com.example.backend.dto.MatchDto;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 실시간 경기 및 채팅 Service
 */
@Service
@RequiredArgsConstructor
public class LiveService {

    private final MatchRepository matchRepository;
    private final ChatroomRepository chatroomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final MatchService matchService;

    /**
     * 진행 중인 경기 조회 (LIVE 상태)
     */
    @Transactional(readOnly = true)
    public List<MatchDto> getLiveMatches() {
        List<Match> liveMatches = matchRepository.findByStatusOrderByMatchDateDesc("LIVE");

        return liveMatches.stream()
                .map(matchService::convertToDto)  // ⭐ 이제 정상 작동
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 조회 또는 생성
     */
    @Transactional
    public Chatroom getOrCreateChatroom(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("경기를 찾을 수 없습니다."));

        return chatroomRepository.findByMatch(match)
                .orElseGet(() -> {
                    Chatroom chatroom = new Chatroom();
                    chatroom.setMatch(match);
                    chatroom.setIsActive(true);
                    chatroom.setViewerCount(0);
                    return chatroomRepository.save(chatroom);
                });
    }

    /**
     * 채팅 메시지 조회
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatMessages(Long chatroomId) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        List<ChatMessage> messages = chatMessageRepository.findByChatroomOrderByCreatedAtAsc(chatroom);

        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 채팅 메시지 전송
     */
    @Transactional
    public ChatMessageDto sendMessage(Long chatroomId, String username, String message) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatroom(chatroom);
        chatMessage.setUser(user);
        chatMessage.setMessage(message);
        chatMessage.setMessageType("USER");

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        return convertToDto(savedMessage);
    }

    /**
     * ChatMessage를 DTO로 변환
     */
    private ChatMessageDto convertToDto(ChatMessage chatMessage) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setMessageId(chatMessage.getMessageId());
        dto.setChatroomId(chatMessage.getChatroom().getChatroomId());
        dto.setUsername(chatMessage.getUser().getUsername());
        dto.setNickname(chatMessage.getUser().getNickname());
        dto.setUserTier(chatMessage.getUser().getTier());
        dto.setIsAdmin(chatMessage.getUser().getIsAdmin());  // 관리자 여부 추가
        dto.setMessage(chatMessage.getMessage());
        dto.setMessageType(chatMessage.getMessageType());
        dto.setCreatedAt(chatMessage.getCreatedAt());
        return dto;
    }
}