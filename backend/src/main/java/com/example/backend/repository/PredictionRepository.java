package com.example.backend.repository;

import com.example.backend.entity.Prediction;
import com.example.backend.entity.Match;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    Optional<Prediction> findByMatchAndUser(Match match, User user);
    List<Prediction> findByMatchOrderByLikeCountDescCreatedAtDesc(Match match);
    List<Prediction> findByUserOrderByCreatedAtDesc(User user);
    long countByMatchAndPredictedResult(Match match, String predictedResult);
}
