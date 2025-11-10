package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 신고 엔티티
 */
@Entity
@Table(name = "reports")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter; // 신고자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType; // 신고 유형 (게시글, 댓글, 사용자)

    @Column(nullable = false)
    private Long targetId; // 신고 대상 ID

    @Column(nullable = false, length = 100)
    private String reason; // 신고 사유

    @Column(length = 1000)
    private String description; // 상세 설명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING; // 처리 상태

    @Column
    private LocalDateTime resolvedAt; // 처리 일시

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

/**
 * 신고 유형
 */
enum ReportType {
    POST,    // 게시글
    COMMENT, // 댓글
    USER     // 사용자
}

/**
 * 신고 상태
 */
enum ReportStatus {
    PENDING,  // 미처리
    RESOLVED  // 처리 완료
}