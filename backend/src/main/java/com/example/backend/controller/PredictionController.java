package com.example.backend.controller;

import com.example.backend.dto.PredictionDto;
import com.example.backend.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 승부예측 Controller
 */
@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PredictionController {

    private final PredictionService predictionService;

    /**
     * 경기의 모든 예측 조회
     * GET /api/predictions/match/1
     */
    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<PredictionDto>> getPredictionsByMatch(@PathVariable Long matchId) {
        List<PredictionDto> predictions = predictionService.getPredictionsByMatch(matchId);
        return ResponseEntity.ok(predictions);
    }

    /**
     * 사용자의 예측 내역 조회
     * GET /api/predictions/user/john
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<PredictionDto>> getUserPredictions(@PathVariable String username) {
        List<PredictionDto> predictions = predictionService.getUserPredictions(username);
        return ResponseEntity.ok(predictions);
    }

    /**
     * 예측 통계 조회
     * GET /api/predictions/match/1/statistics
     */
    @GetMapping("/match/{matchId}/statistics")
    public ResponseEntity<Map<String, Object>> getPredictionStatistics(@PathVariable Long matchId) {
        Map<String, Object> statistics = predictionService.getPredictionStatistics(matchId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 예측 생성
     * POST /api/predictions
     */
    @PostMapping
    public ResponseEntity<PredictionDto> createPrediction(@RequestBody Map<String, String> request) {
        Long matchId = Long.parseLong(request.get("matchId"));
        String username = request.get("username");
        String predictedResult = request.get("predictedResult");
        String comment = request.get("comment");

        PredictionDto prediction = predictionService.createPrediction(matchId, username, predictedResult, comment);
        return ResponseEntity.ok(prediction);
    }
}
