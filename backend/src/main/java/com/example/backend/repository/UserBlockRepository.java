package com.example.backend.repository;

import com.example.backend.entity.UserBlock;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
    Optional<UserBlock> findByBlockerAndBlocked(User blocker, User blocked);
    boolean existsByBlockerAndBlocked(User blocker, User blocked);
    List<UserBlock> findByBlocker(User blocker);
    void deleteByBlockerAndBlocked(User blocker, User blocked);
}
