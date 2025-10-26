package com.example.backend.repository;

import com.example.backend.entity.User;
import com.example.backend.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 차단 Repository
 */
@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
    // 특정 사용자 간의 차단 관계 조회
    Optional<UserBlock> findByBlockerAndBlocked(User blocker, User blocked);

    // 내가 차단한 사용자 목록
    List<UserBlock> findByBlocker(User blocker);

    // 나를 차단한 사용자 목록
    List<UserBlock> findByBlocked(User blocked);
}