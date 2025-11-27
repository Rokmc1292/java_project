package com.example.backend.repository;

import com.example.backend.entity.MmaFight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * UFC 경기 Repository
 */
@Repository
public interface MmaFightRepository extends JpaRepository<MmaFight, Long> {

    /**
     * 특정 날짜의 UFC 경기 조회
     */
    @Query("SELECT f FROM MmaFight f " +
            "JOIN FETCH f.league l " +
            "JOIN FETCH f.fighter1 f1 " +
            "JOIN FETCH f.fighter2 f2 " +
            "LEFT JOIN FETCH f.winner w " +
            "WHERE DATE(f.fightDate) = :date " +
            "ORDER BY f.fightDate ASC")
    List<MmaFight> findByFightDate(@Param("date") LocalDate date);

    /**
     * 날짜 범위로 UFC 경기 조회
     */
    @Query("SELECT f FROM MmaFight f " +
            "JOIN FETCH f.league l " +
            "JOIN FETCH f.fighter1 f1 " +
            "JOIN FETCH f.fighter2 f2 " +
            "LEFT JOIN FETCH f.winner w " +
            "WHERE f.fightDate BETWEEN :startDate AND :endDate " +
            "ORDER BY f.fightDate ASC")
    List<MmaFight> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 경기 상태로 UFC 경기 조회 (스케줄러용)
     */
    @Query("SELECT f FROM MmaFight f " +
            "JOIN FETCH f.league l " +
            "JOIN FETCH f.fighter1 f1 " +
            "JOIN FETCH f.fighter2 f2 " +
            "LEFT JOIN FETCH f.winner w " +
            "WHERE f.status = :status " +
            "ORDER BY f.fightDate ASC")
    List<MmaFight> findByStatus(@Param("status") String status);
}