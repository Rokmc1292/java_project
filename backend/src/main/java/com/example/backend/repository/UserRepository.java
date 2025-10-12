package com.example.backend.repository;

import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User 엔티티를 위한 JPA Repository
 * 데이터베이스 CRUD 작업을 위한 인터페이스
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // username으로 사용자 찾기 (로그인 시 사용)
    Optional<User> findByUsername(String username);

    // email로 사용자 찾기 (이메일 중복 체크)
    Optional<User> findByEmail(String email);

    // nickname으로 사용자 찾기 (닉네임 중복 체크)
    Optional<User> findByNickname(String nickname);

    // username 존재 여부 확인
    boolean existsByUsername(String username);

    // email 존재 여부 확인
    boolean existsByEmail(String email);

    // nickname 존재 여부 확인
    boolean existsByNickname(String nickname);
}