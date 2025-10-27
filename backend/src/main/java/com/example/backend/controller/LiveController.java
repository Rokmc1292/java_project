package com.example.backend.controller;

import com.example.backend.dto.ChatMessageDto;
import com.example.backend.dto.MatchDto;
import com.example.backend.entity.Chatroom;
import com.example.backend.service.LiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/live")
@RequiredArgsConstructor
public class LiveController {

    private final LiveService liveService;

    /**
     * 진행 중인 경기 조회
     */
    @GetMapping("/matches")
    public ResponseEntity<List<MatchDto>> getLiveMatches() {
        List<MatchDto> matches = liveService.getLiveMatches();
        return ResponseEntity.ok(matches);
    }

    /**
     * 채팅방 조회 또는 생성
     */
    @GetMapping("/chatroom/match/{matchId}")
    public ResponseEntity<Map<String, Object>> getChatroom(@PathVariable Long matchId) {
        Chatroom chatroom = liveService.getOrCreateChatroom(matchId);

        Map<String, Object> response = new HashMap<>();
        response.put("chatroomId", chatroom.getChatroomId());
        response.put("matchId", chatroom.getMatch().getMatchId());
        response.put("viewerCount", chatroom.getViewerCount());

        return ResponseEntity.ok(response);
    }

    /**
     * 채팅 메시지 조회
     */
    @GetMapping("/chatroom/{chatroomId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getChatMessages(@PathVariable Long chatroomId) {
        List<ChatMessageDto> messages = liveService.getChatMessages(chatroomId);
        return ResponseEntity.ok(messages);
    }

    /**
     * 채팅 메시지 전송
     */
    @PostMapping("/chatroom/{chatroomId}/messages")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @PathVariable Long chatroomId,
            @RequestBody Map<String, String> request
    ) {
        String username = request.get("username");
        String message = request.get("message");

        ChatMessageDto chatMessage = liveService.sendMessage(chatroomId, username, message);
        return ResponseEntity.ok(chatMessage);
    }
}