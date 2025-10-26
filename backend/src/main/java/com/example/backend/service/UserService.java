package com.example.backend.service;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.SignupRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 Service
 * 회원가입, 로그인, 중복 체크 등의 기능 제공
 *
 * 파일 위치: backend/src/main/java/com/example/backend/service/UserService.java
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 처리
     * 1. 중복 체크 (아이디, 이메일, 닉네임)
     * 2. 비밀번호 확인 일치 체크
     * 3. 비밀번호 암호화
     * 4. 사용자 정보 저장
     */
    @Transactional
    public UserResponse signup(SignupRequest request) {
        // 아이디 중복 체크
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }

        // 비밀번호 확인 일치 체크
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // User 엔티티 생성 및 저장
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // 비밀번호 암호화
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());

        User savedUser = userRepository.save(user);

        // UserResponse로 변환하여 반환
        return convertToUserResponse(savedUser);
    }

    /**
     * 로그인 처리
     * 1. 아이디로 사용자 조회
     * 2. 비밀번호 일치 확인
     * 3. 사용자 정보 반환
     */
    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        // 아이디로 사용자 찾기
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // 계정 활성화 여부 확인
        if (!user.getIsActive()) {
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }

        // UserResponse로 변환하여 반환
        return convertToUserResponse(user);
    }

    /**
     * 아이디 중복 체크
     */
    @Transactional(readOnly = true)
    public boolean checkUsernameDuplicate(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 이메일 중복 체크
     */
    @Transactional(readOnly = true)
    public boolean checkEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 닉네임 중복 체크
     */
    @Transactional(readOnly = true)
    public boolean checkNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    /**
     * 사용자명으로 사용자 정보 조회 (JWT 토큰 검증 후 사용)
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return convertToUserResponse(user);
    }

    /**
     * User 엔티티를 UserResponse DTO로 변환
     */
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setEmail(user.getEmail());
        response.setProfileImage(user.getProfileImage());
        response.setTier(user.getTier());
        response.setTierScore(user.getTierScore());
        response.setIsAdmin(user.getIsAdmin());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}