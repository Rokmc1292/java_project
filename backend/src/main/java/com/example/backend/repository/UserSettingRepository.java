package com.example.backend.repository;

import com.example.backend.entity.User;
import com.example.backend.entity.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 설정 Repository
 * 알림 설정 관리
 */
@Repository
public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {

    // 사용자의 설정 조회
    Optional<UserSetting> findByUser(User user);
}