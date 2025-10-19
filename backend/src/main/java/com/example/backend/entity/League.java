package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 리그 엔티티
 * EPL, NBA, KBO, LCK, UFC 등의 리그 정보
 */
@Entity
@Table(name = "leagues")
@Getter
@Setter
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "league_id")
    private Long leagueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @Column(name = "league_name", nullable = false, length = 100)
    private String leagueName;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "league_logo", length = 500)
    private String leagueLogo;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}