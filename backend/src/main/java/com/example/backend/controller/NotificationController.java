package com.example.backend.controller;

import com.example.backend.dto.NotificationDto;
import com.example.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 알림 Controller
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 사용자 알림 목록 조회
     * GET /api/notifications?username=user1&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getUserNotifications(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notifications = notificationService.getUserNotifications(username, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 읽지 않은 알림 목록 조회
     * GET /api/notifications/unread?username=user1
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(
            @RequestParam String username) {
        List<NotificationDto> notifications = notificationService.getUnreadNotifications(username);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 읽지 않은 알림 개수 조회
     * GET /api/notifications/unread/count?username=user1
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadNotificationCount(
            @RequestParam String username) {
        long count = notificationService.getUnreadNotificationCount(username);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * 알림 읽음 처리
     * POST /api/notifications/1/read
     */
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            @RequestBody Map<String, String> request) {
        String username = request.get("username");
        notificationService.markAsRead(notificationId, username);
        return ResponseEntity.ok().build();
    }

    /**
     * 모든 알림 읽음 처리
     * POST /api/notifications/read-all
     */
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        notificationService.markAllAsRead(username);
        return ResponseEntity.ok().build();
    }

    /**
     * 알림 삭제
     * DELETE /api/notifications/1?username=user1
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId,
            @RequestParam String username) {
        notificationService.deleteNotification(notificationId, username);
        return ResponseEntity.ok().build();
    }
}
