package com.example.backend.repository;

import com.example.backend.entity.PredictionStatistics;
import com.example.backend.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 예측 통계 Repository
 * - 경기별 예측 투표 통계 관리
 */
@Repository
public interface PredictionStatisticsRepository extends JpaRepository<PredictionStatistics, Long> {

    // 특정 경기의 예측 통계 조회
    Optional<PredictionStatistics> findByMatch(Match match);
}