package com.example.backend.controller;

import com.example.backend.dto.ChatMessageDto;
import com.example.backend.dto.MatchDto;
import com.example.backend.service.LiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 실시간 페이지 Controller
 * 실시간 경기 및 채팅 관련 API
 */
@RestController
@RequestMapping("/api/live")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class LiveController {

    private final LiveService liveService;

    /**
     * 현재 진행 중인 경기 조회
     * GET /api/live/matches
     */
    @GetMapping("/matches")
    public ResponseEntity<List<MatchDto>> getLiveMatches() {
        List<MatchDto> matches = liveService.getLiveMatches();
        return ResponseEntity.ok(matches);
    }

    /**
     * 경기의 채팅방 조회 또는 생성
     * GET /api/live/chatroom/match/1
     */
    @GetMapping("/chatroom/match/{matchId}")
    public ResponseEntity<Map<String, Long>> getOrCreateChatroom(@PathVariable Long matchId) {
        Long chatroomId = liveService.getOrCreateChatroom(matchId);
        return ResponseEntity.ok(Map.of("chatroomId", chatroomId));
    }

    /**
     * 채팅 메시지 조회
     * GET /api/live/chatroom/1/messages
     */
    @GetMapping("/chatroom/{chatroomId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getChatMessages(@PathVariable Long chatroomId) {
        List<ChatMessageDto> messages = liveService.getChatMessages(chatroomId);
        return ResponseEntity.ok(messages);
    }

    /**
     * 채팅 메시지 전송
     * POST /api/live/chatroom/1/messages
     */
    @PostMapping("/chatroom/{chatroomId}/messages")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @PathVariable Long chatroomId,
            @RequestBody Map<String, String> request) {
        String username = request.get("username");
        String message = request.get("message");

        ChatMessageDto chatMessage = liveService.sendMessage(chatroomId, username, message);
        return ResponseEntity.ok(chatMessage);
    }
}
