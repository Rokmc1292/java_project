package com.example.backend.service;

import com.example.backend.dto.ChatMessageDto;
import com.example.backend.dto.MatchDto;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LiveService {

    private final MatchRepository matchRepository;
    private final ChatroomRepository chatroomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final MatchService matchService;

    /**
     * 현재 진행 중인 경기 조회
     */
    @Transactional(readOnly = true)
    public List<MatchDto> getLiveMatches() {
        List<Match> liveMatches = matchRepository.findByStatusOrderByMatchDateDesc("LIVE");
        return liveMatches.stream()
                .map(matchService::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 경기의 채팅방 조회 또는 생성
     */
    @Transactional
    public Long getOrCreateChatroom(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("경기를 찾을 수 없습니다."));

        Chatroom chatroom = chatroomRepository.findByMatch(match)
                .orElseGet(() -> {
                    Chatroom newChatroom = new Chatroom();
                    newChatroom.setMatch(match);
                    newChatroom.setIsActive(true);
                    newChatroom.setViewerCount(0);
                    return chatroomRepository.save(newChatroom);
                });

        return chatroom.getChatroomId();
    }

    /**
     * 채팅 메시지 조회
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatMessages(Long chatroomId) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        return chatMessageRepository.findByChatroomOrderByCreatedAtDesc(chatroom)
                .stream()
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
     * Entity를 DTO로 변환
     */
    private ChatMessageDto convertToDto(ChatMessage chatMessage) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setMessageId(chatMessage.getMessageId());
        dto.setChatroomId(chatMessage.getChatroom().getChatroomId());
        dto.setUsername(chatMessage.getUser().getUsername());
        dto.setNickname(chatMessage.getUser().getNickname());
        dto.setUserTier(chatMessage.getUser().getTier());
        dto.setMessage(chatMessage.getMessage());
        dto.setMessageType(chatMessage.getMessageType());
        dto.setCreatedAt(chatMessage.getCreatedAt());
        return dto;
    }
}
