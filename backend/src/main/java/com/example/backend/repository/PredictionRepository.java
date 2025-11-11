package com.example.backend.repository;

import com.example.backend.entity.Prediction;
import com.example.backend.entity.Match;
import com.example.backend.entity.User;
import org.springframework.data.domain.Page;  // ⭐ 추가
import org.springframework.data.domain.Pageable;  // ⭐ 추가
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 승부예측 Repository
 * - 예측 데이터 조회, 저장, 통계 등
 */
@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    // 특정 경기와 사용자의 예측 조회 (중복 예측 방지)
    Optional<Prediction> findByMatchAndUser(Match match, User user);

    // 특정 경기의 모든 예측 조회 (추천순 -> 최신순)
    List<Prediction> findByMatchOrderByLikeCountDescCreatedAtDesc(Match match);

    // 특정 경기의 모든 예측 조회 (페이징)
    Page<Prediction> findByMatchOrderByLikeCountDescCreatedAtDesc(Match match, Pageable pageable);

    // 사용자의 예측 내역 조회 (최신순)
    Page<Prediction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 사용자의 예측 내역 조회 (경기 종료 여부 필터링)
    @Query("SELECT p FROM Prediction p WHERE p.user = :user AND p.isCorrect IS NULL ORDER BY p.createdAt DESC")
    Page<Prediction> findByUserAndIsCorrectIsNull(@Param("user") User user, Pageable pageable);

    @Query("SELECT p FROM Prediction p WHERE p.user = :user AND p.isCorrect IS NOT NULL ORDER BY p.createdAt DESC")
    Page<Prediction> findByUserAndIsCorrectIsNotNull(@Param("user") User user, Pageable pageable);

    // 특정 경기의 예측 결과별 개수 (통계용)
    long countByMatchAndPredictedResult(Match match, String predictedResult);

    // 사용자의 정답/오답 개수
    long countByUserAndIsCorrect(User user, Boolean isCorrect);

    // 사용자의 전체 예측 개수
    long countByUser(User user);

    // 종목별 사용자 예측 통계
    @Query("SELECT COUNT(p) FROM Prediction p WHERE p.user = :user AND p.match.league.sport.sportName = :sportName")
    long countByUserAndSport(@Param("user") User user, @Param("sportName") String sportName);

    @Query("SELECT COUNT(p) FROM Prediction p WHERE p.user = :user AND p.match.league.sport.sportName = :sportName AND p.isCorrect = true")
    long countByUserAndSportAndIsCorrect(@Param("user") User user, @Param("sportName") String sportName);

    // 주간/월간 예측 통계
    @Query("SELECT COUNT(p) FROM Prediction p WHERE p.user = :user AND p.createdAt >= :startDate")
    long countByUserAndCreatedAtAfter(@Param("user") User user, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(p) FROM Prediction p WHERE p.user = :user AND p.createdAt >= :startDate AND p.isCorrect = true")
    long countByUserAndCreatedAtAfterAndIsCorrect(@Param("user") User user, @Param("startDate") LocalDateTime startDate);

    // ========== 스케줄러용 추가 메서드 ==========

    /**
     * 특정 경기의 판정되지 않은 예측 개수 조회 (스케줄러용)
     */
    long countByMatchAndIsCorrectIsNull(Match match);

    List<Prediction> findByUser(User user);
    Page<Prediction> findByUser(User user, Pageable pageable);
    Page<Prediction> findByUserAndIsCorrectNotNull(User user, Pageable pageable);
}