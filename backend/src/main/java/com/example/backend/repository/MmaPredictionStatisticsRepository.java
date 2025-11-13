package com.example.backend.repository;

import com.example.backend.entity.MmaPredictionStatistics;
import com.example.backend.entity.MmaFight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MMA 예측 통계 Repository
 * - MMA 경기별 예측 투표 통계 관리
 */
@Repository
public interface MmaPredictionStatisticsRepository extends JpaRepository<MmaPredictionStatistics, Long> {

    // 특정 경기의 예측 통계 조회
    Optional<MmaPredictionStatistics> findByFight(MmaFight fight);
}
