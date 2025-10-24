package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 알림 엔티티
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 알림 받을 사용자

    @Column(name = "type", nullable = false, length = 50)
    private String type; // COMMENT, REPLY, POPULAR_POST, LIKE, etc.

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content; // 알림 내용

    @Column(name = "related_post_id")
    private Long relatedPostId; // 관련 게시글 ID

    @Column(name = "related_comment_id")
    private Long relatedCommentId; // 관련 댓글 ID

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
