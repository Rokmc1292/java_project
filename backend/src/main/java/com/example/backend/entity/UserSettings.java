package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long settingId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default  // 이거 추가
    @Column(name = "comment_notification", nullable = false)
    private Boolean commentNotification = true;

    @Builder.Default  // 이거 추가
    @Column(name = "reply_notification", nullable = false)
    private Boolean replyNotification = true;

    @Builder.Default  // 이거 추가
    @Column(name = "popular_post_notification", nullable = false)
    private Boolean popularPostNotification = true;

    @Builder.Default  // 이거 추가
    @Column(name = "prediction_result_notification", nullable = false)
    private Boolean predictionResultNotification = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}