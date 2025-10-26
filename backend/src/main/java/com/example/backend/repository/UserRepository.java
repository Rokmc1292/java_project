package com.example.backend.repository;

import com.example.backend.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 기존 메서드들...
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    // 추가 메서드

    // 활동 왕성 유저 조회 (게시글 + 댓글 수 기준)
    @Query("SELECT u FROM User u " +
            "WHERE (SELECT COUNT(p) FROM Post p WHERE p.user = u AND p.createdAt >= :since) + " +
            "(SELECT COUNT(c) FROM Comment c WHERE c.user = u AND c.createdAt >= :since) > 0 " +
            "ORDER BY " +
            "(SELECT COUNT(p) FROM Post p WHERE p.user = u AND p.createdAt >= :since) + " +
            "(SELECT COUNT(c) FROM Comment c WHERE c.user = u AND c.createdAt >= :since) DESC")
    List<User> findTopActiveUsers(@Param("since") LocalDateTime since, Pageable pageable);
}