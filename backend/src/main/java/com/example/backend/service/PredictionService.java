package com.example.backend.service;

import com.example.backend.dto.PredictionDto;
import com.example.backend.dto.PredictionRequest;
import com.example.backend.dto.PredictionStatisticsDto;
import com.example.backend.dto.PredictionRankingDto;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 승부예측 Service
 * - 예측 생성, 조회, 투표, 통계, 랭킹 등 모든 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final PredictionStatisticsRepository predictionStatisticsRepository;
    private final PredictionVoteRepository predictionVoteRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    // NotificationService 제거

    // ========== 예측 경기 목록 (D-2 경기) ==========

    /**
     * 예측 가능한 경기 목록 조회 (이틀 전 경기)
     */
    @Transactional(readOnly = true)
    public Page<Match> getPredictableMatches(String sportName, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysLater = now.plusDays(2);
        LocalDateTime endOfTwoDays = twoDaysLater.plusDays(1);

        if (sportName != null && !sportName.equals("ALL")) {
            return matchRepository.findPredictableMatchesBySport(sportName, twoDaysLater, endOfTwoDays, pageable);
        } else {
            return matchRepository.findPredictableMatches(twoDaysLater, endOfTwoDays, pageable);
        }
    }

    /**
     * 사용자가 이미 예측한 경기인지 확인
     */
    @Transactional(readOnly = true)
    public boolean hasUserPredicted(Long matchId, String username) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("경기를 찾을 수 없습니다."));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return predictionRepository.findByMatchAndUser(match, user).isPresent();
    }

    // ========== 예측 참여 ==========

    /**
     * 승부예측 생성
     */
    @Transactional
    public PredictionDto createPrediction(String username, PredictionRequest request) {
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new RuntimeException("경기를 찾을 수 없습니다."));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 경기 시작 시간 확인
        if (match.getMatchDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("이미 시작된 경기는 예측할 수 없습니다.");
        }

        // 중복 예측 방지
        predictionRepository.findByMatchAndUser(match, user)
                .ifPresent(p -> {
                    throw new RuntimeException("이미 이 경기에 대한 예측을 하셨습니다.");
                });

        // 예측 생성
        Prediction prediction = new Prediction();
        prediction.setMatch(match);
        prediction.setUser(user);
        prediction.setPredictedResult(request.getPredictedResult());
        prediction.setComment(request.getComment());

        Prediction savedPrediction = predictionRepository.save(prediction);

        // 예측 통계 업데이트
        updatePredictionStatistics(match, request.getPredictedResult());

        return convertToDto(savedPrediction);
    }

    /**
     * 예측 통계 업데이트
     */
    private void updatePredictionStatistics(Match match, String predictedResult) {
        PredictionStatistics stats = predictionStatisticsRepository.findByMatch(match)
                .orElseGet(() -> {
                    PredictionStatistics newStats = new PredictionStatistics();
                    newStats.setMatch(match);
                    return newStats;
                });

        switch (predictedResult) {
            case "HOME":
                stats.setHomeVotes(stats.getHomeVotes() + 1);
                break;
            case "DRAW":
                stats.setDrawVotes(stats.getDrawVotes() + 1);
                break;
            case "AWAY":
                stats.setAwayVotes(stats.getAwayVotes() + 1);
                break;
        }
        stats.setTotalVotes(stats.getTotalVotes() + 1);

        predictionStatisticsRepository.save(stats);
    }

    // ========== 예측 조회 ==========

    /**
     * 특정 경기의 모든 예측 조회
     */
    @Transactional(readOnly = true)
    public Page<PredictionDto> getPredictionsByMatch(Long matchId, Pageable pageable) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("경기를 찾을 수 없습니다."));

        return predictionRepository.findByMatchOrderByLikeCountDescCreatedAtDesc(match, pageable)
                .map(this::convertToDto);
    }

    /**
     * 특정 경기의 예측 통계 조회
     */
    @Transactional(readOnly = true)
    public PredictionStatisticsDto getPredictionStatistics(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("경기를 찾을 수 없습니다."));

        PredictionStatistics stats = predictionStatisticsRepository.findByMatch(match)
                .orElseGet(() -> {
                    PredictionStatistics newStats = new PredictionStatistics();
                    newStats.setMatch(match);
                    newStats.setHomeVotes(0);
                    newStats.setDrawVotes(0);
                    newStats.setAwayVotes(0);
                    newStats.setTotalVotes(0);
                    return newStats;
                });

        return convertStatisticsToDto(stats);
    }

    /**
     * 사용자의 예측 내역 조회
     */
    @Transactional(readOnly = true)
    public Page<PredictionDto> getUserPredictions(String username, String status, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Page<Prediction> predictions;

        if ("ongoing".equals(status)) {
            predictions = predictionRepository.findByUserAndIsCorrectIsNull(user, pageable);
        } else if ("completed".equals(status)) {
            predictions = predictionRepository.findByUserAndIsCorrectIsNotNull(user, pageable);
        } else {
            predictions = predictionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        }

        return predictions.map(this::convertToDto);
    }

    // ========== 코멘트 추천/비추천 ==========

    /**
     * 예측 코멘트 추천
     */
    @Transactional
    public void likePrediction(Long predictionId, String username) {
        Prediction prediction = predictionRepository.findById(predictionId)
                .orElseThrow(() -> new RuntimeException("예측을 찾을 수 없습니다."));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (prediction.getUser().equals(user)) {
            throw new RuntimeException("자신의 예측은 추천할 수 없습니다.");
        }

        predictionVoteRepository.findByPredictionAndUser(prediction, user)
                .ifPresent(vote -> {
                    throw new RuntimeException("이미 투표하셨습니다.");
                });

        PredictionVote vote = new PredictionVote();
        vote.setPrediction(prediction);
        vote.setUser(user);
        vote.setVoteType("LIKE");
        predictionVoteRepository.save(vote);

        prediction.setLikeCount(prediction.getLikeCount() + 1);
        predictionRepository.save(prediction);
    }

    /**
     * 예측 코멘트 비추천
     */
    @Transactional
    public void dislikePrediction(Long predictionId, String username) {
        Prediction prediction = predictionRepository.findById(predictionId)
                .orElseThrow(() -> new RuntimeException("예측을 찾을 수 없습니다."));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (prediction.getUser().equals(user)) {
            throw new RuntimeException("자신의 예측은 비추천할 수 없습니다.");
        }

        predictionVoteRepository.findByPredictionAndUser(prediction, user)
                .ifPresent(vote -> {
                    throw new RuntimeException("이미 투표하셨습니다.");
                });

        PredictionVote vote = new PredictionVote();
        vote.setPrediction(prediction);
        vote.setUser(user);
        vote.setVoteType("DISLIKE");
        predictionVoteRepository.save(vote);

        prediction.setDislikeCount(prediction.getDislikeCount() + 1);
        predictionRepository.save(prediction);
    }

    // ========== 결과 처리 (경기 종료 후) ==========

    /**
     * 경기 종료 후 예측 결과 판정
     */
    @Transactional
    public void judgePredictions(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("경기를 찾을 수 없습니다."));

        if (!match.getStatus().equals("FINISHED")) {
            throw new RuntimeException("아직 종료되지 않은 경기입니다.");
        }

        String actualResult = determineMatchResult(match);
        List<Prediction> predictions = predictionRepository.findByMatchOrderByLikeCountDescCreatedAtDesc(match);

        for (Prediction prediction : predictions) {
            boolean isCorrect = prediction.getPredictedResult().equals(actualResult);
            prediction.setIsCorrect(isCorrect);
            predictionRepository.save(prediction);

            User user = prediction.getUser();
            if (isCorrect) {
                user.setTierScore(user.getTierScore() + 10);
            } else {
                user.setTierScore(Math.max(0, user.getTierScore() - 10));
            }

            updateUserTier(user);
            userRepository.save(user);

            // 알림 기능은 나중에 구현 (현재는 주석 처리)
        }
    }

    /**
     * 경기 결과 판정
     */
    private String determineMatchResult(Match match) {
        Integer homeScore = match.getHomeScore();
        Integer awayScore = match.getAwayScore();

        if (homeScore == null || awayScore == null) {
            throw new RuntimeException("경기 점수가 없습니다. matchId=" + match.getMatchId());
        }

        if (homeScore > awayScore) {
            return "HOME";
        } else if (homeScore < awayScore) {
            return "AWAY";
        } else {
            return "DRAW";
        }
    }

    /**
     * 티어 점수에 따른 티어 업데이트
     */
    private void updateUserTier(User user) {
        int score = user.getTierScore();

        if (score >= 1000) {
            user.setTier("DIAMOND");
        } else if (score >= 600) {
            user.setTier("PLATINUM");
        } else if (score >= 300) {
            user.setTier("GOLD");
        } else if (score >= 100) {
            user.setTier("SILVER");
        } else {
            user.setTier("BRONZE");
        }
    }

    // ========== 랭킹 시스템 ==========

    @Transactional(readOnly = true)
    public List<PredictionRankingDto> getOverallRanking(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<User> topUsers = userRepository.findAllByOrderByTierScoreDescTierAsc(pageable).getContent();

        return topUsers.stream()
                .map((user) -> createRankingDto(user, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PredictionRankingDto> getSportRanking(String sportName, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<User> topUsers = userRepository.findAllByOrderByTierScoreDescTierAsc(pageable).getContent();

        return topUsers.stream()
                .map((user) -> createRankingDto(user, sportName))
                .collect(Collectors.toList());
    }

    private PredictionRankingDto createRankingDto(User user, String sportName) {
        PredictionRankingDto dto = new PredictionRankingDto();
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setTier(user.getTier());
        dto.setTierScore(user.getTierScore());

        if (sportName != null) {
            long totalPredictions = predictionRepository.countByUserAndSport(user, sportName);
            long correctPredictions = predictionRepository.countByUserAndSportAndIsCorrect(user, sportName);
            dto.setTotalPredictions((int) totalPredictions);
            dto.setCorrectPredictions((int) correctPredictions);
        } else {
            long totalPredictions = predictionRepository.countByUser(user);
            long correctPredictions = predictionRepository.countByUserAndIsCorrect(user, true);
            dto.setTotalPredictions((int) totalPredictions);
            dto.setCorrectPredictions((int) correctPredictions);
        }

        if (dto.getTotalPredictions() > 0) {
            dto.setAccuracy((double) dto.getCorrectPredictions() / dto.getTotalPredictions() * 100);
        } else {
            dto.setAccuracy(0.0);
        }

        dto.setConsecutiveCorrect(calculateConsecutiveCorrect(user));

        return dto;
    }

    private Integer calculateConsecutiveCorrect(User user) {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Prediction> recentPredictions = predictionRepository
                .findByUserAndIsCorrectIsNotNull(user, pageable);

        int consecutive = 0;
        for (Prediction prediction : recentPredictions) {
            if (Boolean.TRUE.equals(prediction.getIsCorrect())) {
                consecutive++;
            } else {
                break;
            }
        }
        return consecutive;
    }

    // ========== 통계/분석 ==========

    @Transactional(readOnly = true)
    public PredictionRankingDto getUserStatistics(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return createRankingDto(user, null);
    }

    @Transactional(readOnly = true)
    public PredictionRankingDto getWeeklyStatistics(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);

        PredictionRankingDto dto = new PredictionRankingDto();
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setTier(user.getTier());
        dto.setTierScore(user.getTierScore());

        long totalPredictions = predictionRepository.countByUserAndCreatedAtAfter(user, weekAgo);
        long correctPredictions = predictionRepository.countByUserAndCreatedAtAfterAndIsCorrect(user, weekAgo);

        dto.setTotalPredictions((int) totalPredictions);
        dto.setCorrectPredictions((int) correctPredictions);

        if (totalPredictions > 0) {
            dto.setAccuracy((double) correctPredictions / totalPredictions * 100);
        } else {
            dto.setAccuracy(0.0);
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public PredictionRankingDto getMonthlyStatistics(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);

        PredictionRankingDto dto = new PredictionRankingDto();
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setTier(user.getTier());
        dto.setTierScore(user.getTierScore());

        long totalPredictions = predictionRepository.countByUserAndCreatedAtAfter(user, monthAgo);
        long correctPredictions = predictionRepository.countByUserAndCreatedAtAfterAndIsCorrect(user, monthAgo);

        dto.setTotalPredictions((int) totalPredictions);
        dto.setCorrectPredictions((int) correctPredictions);

        if (totalPredictions > 0) {
            dto.setAccuracy((double) correctPredictions / totalPredictions * 100);
        } else {
            dto.setAccuracy(0.0);
        }

        return dto;
    }

    // ========== DTO 변환 ==========

    private PredictionDto convertToDto(Prediction prediction) {
        PredictionDto dto = new PredictionDto();
        dto.setPredictionId(prediction.getPredictionId());
        dto.setMatchId(prediction.getMatch().getMatchId());
        dto.setUsername(prediction.getUser().getUsername());
        dto.setNickname(prediction.getUser().getNickname());
        dto.setUserTier(prediction.getUser().getTier());
        dto.setPredictedResult(prediction.getPredictedResult());
        dto.setComment(prediction.getComment());
        dto.setIsCorrect(prediction.getIsCorrect());
        dto.setLikeCount(prediction.getLikeCount());
        dto.setDislikeCount(prediction.getDislikeCount());
        dto.setCreatedAt(prediction.getCreatedAt());
        return dto;
    }

    private PredictionStatisticsDto convertStatisticsToDto(PredictionStatistics stats) {
        PredictionStatisticsDto dto = new PredictionStatisticsDto();
        dto.setMatchId(stats.getMatch().getMatchId());
        dto.setHomeVotes(stats.getHomeVotes());
        dto.setDrawVotes(stats.getDrawVotes());
        dto.setAwayVotes(stats.getAwayVotes());
        dto.setTotalVotes(stats.getTotalVotes());

        if (stats.getTotalVotes() > 0) {
            dto.setHomePercentage((double) stats.getHomeVotes() / stats.getTotalVotes() * 100);
            dto.setDrawPercentage((double) stats.getDrawVotes() / stats.getTotalVotes() * 100);
            dto.setAwayPercentage((double) stats.getAwayVotes() / stats.getTotalVotes() * 100);
        } else {
            dto.setHomePercentage(0.0);
            dto.setDrawPercentage(0.0);
            dto.setAwayPercentage(0.0);
        }

        return dto;
    }
}