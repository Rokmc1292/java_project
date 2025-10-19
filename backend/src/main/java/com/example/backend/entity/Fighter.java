package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * UFC 파이터 엔티티
 */
@Entity
@Table(name = "fighters")
@Getter
@Setter
public class Fighter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fighter_id")
    private Long fighterId;

    @Column(name = "fighter_name", nullable = false, length = 100)
    private String fighterName;

    @Column(name = "fighter_image", length = 500)
    private String fighterImage;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "weight_class", length = 50)
    private String weightClass;

    @Column(name = "record", length = 50)
    private String record;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}