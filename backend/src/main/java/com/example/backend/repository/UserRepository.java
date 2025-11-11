package com.example.backend.repository;

import com.example.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 사용자명으로 조회
    Optional<User> findByUsername(String username);

    // 이메일로 조회
    Optional<User> findByEmail(String email);

    // 닉네임으로 조회
    Optional<User> findByNickname(String nickname);

    // 사용자명 중복 체크
    boolean existsByUsername(String username);

    // 이메일 중복 체크
    boolean existsByEmail(String email);

    // 닉네임 중복 체크
    boolean existsByNickname(String nickname);

    // 활동 왕성 유저 조회 (특정 기간 이후)
    @Query("SELECT u FROM User u WHERE u.userId IN " +
            "(SELECT p.user.userId FROM Post p WHERE p.createdAt >= :since " +
            "UNION SELECT c.user.userId FROM Comment c WHERE c.createdAt >= :since) " +
            "ORDER BY (SELECT COUNT(p2) FROM Post p2 WHERE p2.user = u AND p2.createdAt >= :since) + " +
            "(SELECT COUNT(c2) FROM Comment c2 WHERE c2.user = u AND c2.createdAt >= :since) DESC")
    Page<User> findTopActiveUsers(@Param("since") LocalDateTime since, Pageable pageable);

    // ========== 승부예측 랭킹용 추가 메서드 ==========

    /**
     * 티어 점수 기준 내림차순 정렬
     * - 랭킹 시스템에 사용
     */
    Page<User> findAllByOrderByTierScoreDescTierAsc(Pageable pageable);

    // ========== 관리자 페이지용 추가 메서드 ==========

    /**
     * 특정 날짜 이후 가입한 사용자 수 (대시보드 통계용)
     */
    long countByCreatedAtAfter(LocalDateTime date);
}