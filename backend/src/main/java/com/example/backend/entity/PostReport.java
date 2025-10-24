package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 게시글 신고 엔티티
 */
@Entity
@Table(name = "post_reports")
@Getter
@Setter
public class PostReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(name = "reason", nullable = false, length = 50)
    private String reason; // 신고 사유 (욕설, 음란물, 도배, 기타 등)

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 상세 설명

    @Column(name = "status", length = 20)
    private String status = "PENDING"; // PENDING, PROCESSED, REJECTED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
