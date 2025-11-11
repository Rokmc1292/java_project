package com.example.backend.dto;

import lombok.Data;

@Data
public class BoardCategoryDto {
    private Long categoryId;
    private String categoryName;
    private Integer displayOrder;
}