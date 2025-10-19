package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * UFC 경기 엔티티
 */
@Entity
@Table(name = "mma_fights")
@Getter
@Setter
public class MmaFight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fight_id")
    private Long fightId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fighter1_id", nullable = false)
    private Fighter fighter1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fighter2_id", nullable = false)
    private Fighter fighter2;

    @Column(name = "fight_date", nullable = false)
    private LocalDateTime fightDate;

    @Column(name = "venue", length = 200)
    private String venue;

    @Column(name = "event_name", length = 200)
    private String eventName;

    @Column(name = "status", length = 20)
    private String status;  // SCHEDULED, LIVE, FINISHED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private Fighter winner;

    @Column(name = "method", length = 50)
    private String method;

    @Column(name = "round")
    private Integer round;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}