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
     * 날짜 범위로 검색하여 정확한 날짜 매칭 보장
     */
    @Query("SELECT m FROM Match m WHERE m.matchDate >= :startOfDay AND m.matchDate < :startOfNextDay ORDER BY m.matchDate ASC")
    List<Match> findByMatchDate(@Param("startOfDay") LocalDateTime startOfDay, @Param("startOfNextDay") LocalDateTime startOfNextDay);

    /**
     * 특정 날짜의 경기 조회 (종목별)
     */
    @Query("SELECT m FROM Match m WHERE DATE(m.matchDate) = DATE(:date) AND m.league.sport.sportName = :sportName ORDER BY m.matchDate ASC")
    List<Match> findByMatchDateAndSport(@Param("date") LocalDateTime date, @Param("sportName") String sportName);

    /**
     * 날짜 범위로 경기 조회 (전체 종목)
     */
    @Query("SELECT m FROM Match m WHERE m.matchDate BETWEEN :startDate AND :endDate ORDER BY m.matchDate ASC")
    List<Match> findByMatchDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 날짜 범위로 경기 조회 (별칭)
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
     * 상태별 경기 조회 (내림차순)
     */
    @Query("SELECT m FROM Match m WHERE m.status = :status ORDER BY m.matchDate DESC")
    List<Match> findByStatusOrderByMatchDateDesc(@Param("status") String status);

    /**
     * 특정 상태의 경기 조회
     */
    List<Match> findByStatus(String status);

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

    /**
     * EPL 리그의 LIVE 경기 조회 (실시간 점수 업데이트용)
     * JOIN FETCH로 팀 정보를 함께 로드하여 LazyInitializationException 방지
     */
    @Query("SELECT m FROM Match m JOIN FETCH m.homeTeam JOIN FETCH m.awayTeam WHERE m.league.leagueId = :leagueId AND m.status = 'LIVE'")
    List<Match> findLiveMatchesByLeague(@Param("leagueId") Long leagueId);

    /**
     * 특정 리그의 오늘 경기 조회
     */
    @Query("SELECT m FROM Match m " +
            "WHERE m.league.leagueId = :leagueId " +
            "AND DATE(m.matchDate) = DATE(:date) " +
            "ORDER BY m.matchDate ASC")
    List<Match> findTodayMatchesByLeague(
            @Param("leagueId") long leagueId,
            @Param("date") LocalDateTime date
    );
}