package com.example.backend.controller;

import com.example.backend.dto.MatchDto;
import com.example.backend.dto.PredictionDto;
import com.example.backend.dto.PredictionRequest;
import com.example.backend.dto.PredictionStatisticsDto;
import com.example.backend.dto.PredictionRankingDto;
import com.example.backend.service.PredictionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 승부예측 관련 API Controller
 * 세션 기반 인증
 */
@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    /**
     * 세션에서 사용자명 추출 헬퍼 메서드
     */
    private String getUsernameFromSession(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }
        return username;
    }

    // ========== 예측 경기 목록 ==========

    /**
     * 예측 가능한 경기 목록 조회 (D-30 경기)
     */
    @GetMapping("/matches")
    public ResponseEntity<Page<MatchDto>> getPredictableMatches(
            @RequestParam(required = false, defaultValue = "ALL") String sport,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MatchDto> matches = predictionService.getPredictableMatches(sport, pageable);
        return ResponseEntity.ok(matches);
    }

    /**
     * 사용자가 이미 예측했는지 확인
     */
    @GetMapping("/match/{matchId}/check")
    public ResponseEntity<?> checkUserPrediction(
            @PathVariable Long matchId,
            HttpSession session
    ) {
        String username = getUsernameFromSession(session);
        boolean hasPredicted = predictionService.hasUserPredicted(matchId, username);
        return ResponseEntity.ok(new CheckResponse(hasPredicted));
    }

    // ========== 예측 참여 ==========

    /**
     * 승부예측 생성
     */
    @PostMapping
    public ResponseEntity<PredictionDto> createPrediction(
            @RequestBody PredictionRequest request,
            HttpSession session
    ) {
        String username = getUsernameFromSession(session);
        PredictionDto prediction = predictionService.createPrediction(username, request);
        return ResponseEntity.ok(prediction);
    }

    // ========== 예측 조회 ==========

    /**
     * 특정 경기의 모든 예측 조회
     */
    @GetMapping("/match/{matchId}")
    public ResponseEntity<Page<PredictionDto>> getPredictionsByMatch(
            @PathVariable Long matchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PredictionDto> predictions = predictionService.getPredictionsByMatch(matchId, pageable);
        return ResponseEntity.ok(predictions);
    }

    /**
     * 특정 경기의 예측 통계 조회
     */
    @GetMapping("/match/{matchId}/statistics")
    public ResponseEntity<PredictionStatisticsDto> getPredictionStatistics(@PathVariable Long matchId) {
        PredictionStatisticsDto statistics = predictionService.getPredictionStatistics(matchId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 사용자의 예측 내역 조회
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<Page<PredictionDto>> getUserPredictions(
            @PathVariable String username,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PredictionDto> predictions = predictionService.getUserPredictions(username, status, pageable);
        return ResponseEntity.ok(predictions);
    }

    // ========== 코멘트 추천/비추천 ==========

    /**
     * 예측 코멘트 추천
     */
    @PostMapping("/{predictionId}/like")
    public ResponseEntity<?> likePrediction(
            @PathVariable Long predictionId,
            HttpSession session
    ) {
        String username = getUsernameFromSession(session);
        predictionService.likePrediction(predictionId, username);
        return ResponseEntity.ok(new MessageResponse("추천했습니다."));
    }

    /**
     * 예측 코멘트 비추천
     */
    @PostMapping("/{predictionId}/dislike")
    public ResponseEntity<?> dislikePrediction(
            @PathVariable Long predictionId,
            HttpSession session
    ) {
        String username = getUsernameFromSession(session);
        predictionService.dislikePrediction(predictionId, username);
        return ResponseEntity.ok(new MessageResponse("비추천했습니다."));
    }

    // ========== 랭킹 시스템 ==========

    /**
     * 전체 랭킹 조회
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<PredictionRankingDto>> getOverallRanking(
            @RequestParam(defaultValue = "100") int limit
    ) {
        List<PredictionRankingDto> ranking = predictionService.getOverallRanking(limit);
        return ResponseEntity.ok(ranking);
    }

    /**
     * 종목별 랭킹 조회
     */
    @GetMapping("/ranking/{sportName}")
    public ResponseEntity<List<PredictionRankingDto>> getSportRanking(
            @PathVariable String sportName,
            @RequestParam(defaultValue = "100") int limit
    ) {
        List<PredictionRankingDto> ranking = predictionService.getSportRanking(sportName, limit);
        return ResponseEntity.ok(ranking);
    }

    // ========== 통계/분석 ==========

    /**
     * 사용자 전체 통계 조회
     */
    @GetMapping("/statistics/{username}")
    public ResponseEntity<PredictionRankingDto> getUserStatistics(@PathVariable String username) {
        PredictionRankingDto statistics = predictionService.getUserStatistics(username);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 주간 통계 조회
     */
    @GetMapping("/statistics/{username}/weekly")
    public ResponseEntity<PredictionRankingDto> getWeeklyStatistics(@PathVariable String username) {
        PredictionRankingDto statistics = predictionService.getWeeklyStatistics(username);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 월간 통계 조회
     */
    @GetMapping("/statistics/{username}/monthly")
    public ResponseEntity<PredictionRankingDto> getMonthlyStatistics(@PathVariable String username) {
        PredictionRankingDto statistics = predictionService.getMonthlyStatistics(username);
        return ResponseEntity.ok(statistics);
    }

    // ========== 관리자 기능 ==========

    /**
     * 경기 결과 판정 (관리자 전용)
     */
    @PostMapping("/match/{matchId}/judge")
    public ResponseEntity<?> judgePredictions(@PathVariable Long matchId) {
        predictionService.judgePredictions(matchId);
        return ResponseEntity.ok(new MessageResponse("예측 결과가 판정되었습니다."));
    }

    // ========== 응답 DTO ==========

    private static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private static class CheckResponse {
        private boolean hasPredicted;

        public CheckResponse(boolean hasPredicted) {
            this.hasPredicted = hasPredicted;
        }

        public boolean isHasPredicted() {
            return hasPredicted;
        }
    }
}