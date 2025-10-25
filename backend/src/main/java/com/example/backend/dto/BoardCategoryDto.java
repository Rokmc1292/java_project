package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 게시판 카테고리 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardCategoryDto {
    private Long categoryId;
    private String categoryName;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
