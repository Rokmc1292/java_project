package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PredictionDto {
    private Long predictionId;
    private Long matchId;
    private String username;
    private String nickname;
    private String userTier;
    private String predictedResult;  // HOME, DRAW, AWAY
    private String comment;
    private Boolean isCorrect;
    private Integer likeCount;
    private Integer dislikeCount;
    private LocalDateTime createdAt;
}
