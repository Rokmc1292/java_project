package com.example.backend.repository;

import com.example.backend.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    /**
     * 특정 날짜의 경기 조회 (전체 종목)
     */
    @Query("SELECT m FROM Match m WHERE DATE(m.matchDate) = DATE(:date) ORDER BY m.matchDate ASC")
    List<Match> findByMatchDate(@Param("date") LocalDateTime date);

    /**
     * 특정 날짜의 경기 조회 (종목별)
     */
    @Query("SELECT m FROM Match m WHERE DATE(m.matchDate) = DATE(:date) AND m.league.sport.sportName = :sportName ORDER BY m.matchDate ASC")
    List<Match> findByMatchDateAndSport(@Param("date") LocalDateTime date, @Param("sportName") String sportName);

    /**
     * 날짜 범위로 경기 조회 (전체 종목) - findByDateRange 별칭
     */
    @Query("SELECT m FROM Match m WHERE m.matchDate BETWEEN :startDate AND :endDate ORDER BY m.matchDate ASC")
    List<Match> findByMatchDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 날짜 범위로 경기 조회 (전체 종목) - 별칭 메서드
     */
    @Query("SELECT m FROM Match m WHERE m.matchDate BETWEEN :startDate AND :endDate ORDER BY m.matchDate ASC")
    List<Match> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 날짜 범위로 경기 조회 (종목별)
     */
    @Query("SELECT m FROM Match m WHERE m.matchDate BETWEEN :startDate AND :endDate AND m.league.sport.sportName = :sportName ORDER BY m.matchDate ASC")
    List<Match> findByMatchDateBetweenAndSport(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("sportName") String sportName);

    /**
     * 진행 중인 경기 조회 (LIVE 상태)
     */
    @Query("SELECT m FROM Match m WHERE m.status = 'LIVE' ORDER BY m.matchDate ASC")
    List<Match> findLiveMatches();

    /**
     * 상태별 경기 조회 (내림차순) - LiveService용
     */
    @Query("SELECT m FROM Match m WHERE m.status = :status ORDER BY m.matchDate DESC")
    List<Match> findByStatusOrderByMatchDateDesc(@Param("status") String status);

    /**
     * 특정 상태의 경기 조회 (스케줄러용)
     */
    List<Match> findByStatus(String status);

    // ========== 승부예측용 메서드 ==========

    /**
     * 예측 가능한 경기 조회 (D-2 경기, 전체 종목)
     */
    @Query("SELECT m FROM Match m WHERE m.matchDate BETWEEN :startDate AND :endDate AND m.status = 'SCHEDULED' ORDER BY m.matchDate ASC")
    Page<Match> findPredictableMatches(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * 예측 가능한 경기 조회 (D-2 경기, 종목별)
     */
    @Query("SELECT m FROM Match m WHERE m.league.sport.sportName = :sportName AND m.matchDate BETWEEN :startDate AND :endDate AND m.status = 'SCHEDULED' ORDER BY m.matchDate ASC")
    Page<Match> findPredictableMatchesBySport(
            @Param("sportName") String sportName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}