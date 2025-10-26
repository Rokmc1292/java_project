package com.example.backend.config;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT 인증 필터
 * - 모든 HTTP 요청에서 JWT 토큰을 검증
 * - 유효한 토큰이면 SecurityContext에 인증 정보 설정
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    /**
     * HTTP 요청마다 실행되는 필터 메서드
     * Authorization 헤더에서 JWT 토큰을 추출하고 검증
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Authorization 헤더에서 토큰 추출
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // Bearer 토큰 형식 확인 및 추출
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // "Bearer " 이후의 토큰 부분

            try {
                // 토큰에서 사용자명 추출
                username = jwtUtil.getUsernameFromToken(token);
            } catch (Exception e) {
                // 토큰 파싱 실패
                logger.error("JWT 토큰 파싱 실패: " + e.getMessage());
            }
        }

        // 토큰이 유효하고 SecurityContext에 인증 정보가 없으면
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 토큰 유효성 검증
            if (jwtUtil.validateToken(token)) {

                // 사용자 정보 조회
                User user = userRepository.findByUsername(username).orElse(null);

                if (user != null && user.getIsActive()) {
                    // 인증 토큰 생성
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    new ArrayList<>() // 권한 목록 (필요시 추가)
                            );

                    // 요청 세부 정보 설정
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // SecurityContext에 인증 정보 저장
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}