package com.example.backend.dto;

import lombok.Data;

@Data
public class UserActivityDto {
    private Long userId;
    private String username;
    private String nickname;
    private String tier;
    private String profileImage;
    private Integer postCount;
    private Integer commentCount;
    private Integer totalLikes;
    private Integer activityScore; // postCount * 5 + commentCount * 2 + totalLikes
    private Integer rank;
}
