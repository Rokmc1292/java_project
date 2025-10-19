package com.example.backend.repository;

import com.example.backend.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 경기 Repository
 * 축구, 농구, 야구, 롤 경기 데이터 조회
 */
@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    /**
     * 특정 날짜의 경기 조회
     */
    @Query("SELECT m FROM Match m " +
            "JOIN FETCH m.league l " +
            "JOIN FETCH l.sport s " +
            "JOIN FETCH m.homeTeam ht " +
            "JOIN FETCH m.awayTeam at " +
            "WHERE DATE(m.matchDate) = :date " +
            "ORDER BY m.matchDate ASC")
    List<Match> findByMatchDate(@Param("date") LocalDate date);

    /**
     * 특정 날짜 + 특정 종목의 경기 조회
     */
    @Query("SELECT m FROM Match m " +
            "JOIN FETCH m.league l " +
            "JOIN FETCH l.sport s " +
            "JOIN FETCH m.homeTeam ht " +
            "JOIN FETCH m.awayTeam at " +
            "WHERE DATE(m.matchDate) = :date " +
            "AND s.sportName = :sportName " +
            "ORDER BY m.matchDate ASC")
    List<Match> findByMatchDateAndSport(@Param("date") LocalDate date,
                                        @Param("sportName") String sportName);

    /**
     * 날짜 범위로 경기 조회
     */
    @Query("SELECT m FROM Match m " +
            "JOIN FETCH m.league l " +
            "JOIN FETCH l.sport s " +
            "JOIN FETCH m.homeTeam ht " +
            "JOIN FETCH m.awayTeam at " +
            "WHERE m.matchDate BETWEEN :startDate AND :endDate " +
            "ORDER BY m.matchDate ASC")
    List<Match> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);
}