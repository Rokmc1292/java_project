package com.example.backend.repository;

import com.example.backend.entity.MmaPrediction;
import com.example.backend.entity.MmaFight;
import com.example.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MMA 승부예측 Repository
 * - MMA 예측 데이터 조회, 저장, 통계 등
 */
@Repository
public interface MmaPredictionRepository extends JpaRepository<MmaPrediction, Long> {

    // 특정 경기와 사용자의 예측 조회 (중복 예측 방지)
    Optional<MmaPrediction> findByFightAndUser(MmaFight fight, User user);

    // 특정 경기의 모든 예측 조회 (추천순 -> 최신순)
    Page<MmaPrediction> findByFightOrderByLikeCountDescCreatedAtDesc(MmaFight fight, Pageable pageable);

    // 특정 경기의 모든 예측 조회 (Pageable 없는 버전)
    List<MmaPrediction> findByFightOrderByLikeCountDescCreatedAtDesc(MmaFight fight);

    // 사용자의 예측 내역 조회 (최신순)
    Page<MmaPrediction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 사용자의 예측 내역 조회 (경기 종료 여부 필터링)
    @Query("SELECT p FROM MmaPrediction p WHERE p.user = :user AND p.isCorrect IS NULL ORDER BY p.createdAt DESC")
    Page<MmaPrediction> findByUserAndIsCorrectIsNull(@Param("user") User user, Pageable pageable);

    @Query("SELECT p FROM MmaPrediction p WHERE p.user = :user AND p.isCorrect IS NOT NULL ORDER BY p.createdAt DESC")
    Page<MmaPrediction> findByUserAndIsCorrectIsNotNull(@Param("user") User user, Pageable pageable);

    // 특정 경기의 예측 결과별 개수 (통계용)
    long countByFightAndPredictedResult(MmaFight fight, String predictedResult);

    // 사용자의 정답/오답 개수
    long countByUserAndIsCorrect(User user, Boolean isCorrect);

    // 사용자의 전체 예측 개수
    long countByUser(User user);

    // 주간/월간 예측 통계
    @Query("SELECT COUNT(p) FROM MmaPrediction p WHERE p.user = :user AND p.createdAt >= :startDate")
    long countByUserAndCreatedAtAfter(@Param("user") User user, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(p) FROM MmaPrediction p WHERE p.user = :user AND p.createdAt >= :startDate AND p.isCorrect = true")
    long countByUserAndCreatedAtAfterAndIsCorrect(@Param("user") User user, @Param("startDate") LocalDateTime startDate);

    // ========== 스케줄러용 추가 메서드 ==========

    /**
     * 특정 경기의 판정되지 않은 예측 개수 조회 (스케줄러용)
     */
    long countByFightAndIsCorrectIsNull(MmaFight fight);

    // 사용자별 예측 목록 조회 (페이징)
    Page<MmaPrediction> findByUser(User user, Pageable pageable);

    // 완료된 예측만 조회
    Page<MmaPrediction> findByUserAndIsCorrectNotNull(User user, Pageable pageable);

    // ========== 관리자 페이지용 추가 메서드 ==========

    /**
     * 특정 날짜 이후 생성된 예측 개수 (대시보드 통계용)
     */
    long countByCreatedAtAfter(LocalDateTime date);
}
