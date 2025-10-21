package com.example.backend.service;

import com.example.backend.dto.PredictionDto;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    /**
     * 경기의 모든 예측 조회
     */
    @Transactional(readOnly = true)
    public List<PredictionDto> getPredictionsByMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("경기를 찾을 수 없습니다."));

        return predictionRepository.findByMatchOrderByLikeCountDescCreatedAtDesc(match)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 예측 내역 조회
     */
    @Transactional(readOnly = true)
    public List<PredictionDto> getUserPredictions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return predictionRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 예측 통계 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPredictionStatistics(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("경기를 찾을 수 없습니다."));

        long homeVotes = predictionRepository.countByMatchAndPredictedResult(match, "HOME");
        long drawVotes = predictionRepository.countByMatchAndPredictedResult(match, "DRAW");
        long awayVotes = predictionRepository.countByMatchAndPredictedResult(match, "AWAY");
        long totalVotes = homeVotes + drawVotes + awayVotes;

        Map<String, Object> stats = new HashMap<>();
        stats.put("homeVotes", homeVotes);
        stats.put("drawVotes", drawVotes);
        stats.put("awayVotes", awayVotes);
        stats.put("totalVotes", totalVotes);

        if (totalVotes > 0) {
            stats.put("homePercentage", (homeVotes * 100.0) / totalVotes);
            stats.put("drawPercentage", (drawVotes * 100.0) / totalVotes);
            stats.put("awayPercentage", (awayVotes * 100.0) / totalVotes);
        } else {
            stats.put("homePercentage", 0.0);
            stats.put("drawPercentage", 0.0);
            stats.put("awayPercentage", 0.0);
        }

        return stats;
    }

    /**
     * 예측 생성
     */
    @Transactional
    public PredictionDto createPrediction(Long matchId, String username, String predictedResult, String comment) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("경기를 찾을 수 없습니다."));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미 예측했는지 확인
        predictionRepository.findByMatchAndUser(match, user)
                .ifPresent(p -> {
                    throw new RuntimeException("이미 이 경기에 대한 예측을 하셨습니다.");
                });

        Prediction prediction = new Prediction();
        prediction.setMatch(match);
        prediction.setUser(user);
        prediction.setPredictedResult(predictedResult);
        prediction.setComment(comment);

        Prediction savedPrediction = predictionRepository.save(prediction);
        return convertToDto(savedPrediction);
    }

    /**
     * Entity를 DTO로 변환
     */
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
}
