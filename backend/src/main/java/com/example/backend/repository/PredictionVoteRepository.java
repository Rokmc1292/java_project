package com.example.backend.repository;

import com.example.backend.entity.Prediction;
import com.example.backend.entity.PredictionVote;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 예측 코멘트 추천/비추천 Repository
 * - 1인 1회 제한 관리
 */
@Repository
public interface PredictionVoteRepository extends JpaRepository<PredictionVote, Long> {

    // 특정 예측에 대한 사용자의 투표 조회 (중복 방지)
    Optional<PredictionVote> findByPredictionAndUser(Prediction prediction, User user);
}