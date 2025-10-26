package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자 설정 Entity
 */
@Entity
@Table(name = "user_settings")
@Getter
@Setter
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long settingId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "comment_notification")
    private Boolean commentNotification = true;

    @Column(name = "reply_notification")
    private Boolean replyNotification = true;

    @Column(name = "popular_post_notification")
    private Boolean popularPostNotification = true;

    @Column(name = "prediction_result_notification")
    private Boolean predictionResultNotification = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}