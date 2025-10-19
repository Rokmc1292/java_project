package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 스포츠 종목 엔티티
 * 축구, 농구, 야구, 롤, UFC 등의 종목 정보
 */
@Entity
@Table(name = "sports")
@Getter
@Setter
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sport_id")
    private Long sportId;

    @Column(name = "sport_name", nullable = false, unique = true, length = 50)
    private String sportName;  // FOOTBALL, BASKETBALL, BASEBALL, LOL, MMA

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;  // 축구, 농구, 야구, 롤, UFC

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}