package com.example.backend.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증 유틸리티
 * - 로그인 시 JWT 토큰 생성
 * - API 요청 시 토큰 검증
 */
@Component
public class JwtUtil {

    // application.properties에서 비밀키 주입
    @Value("${jwt.secret:mySecretKeyForJwtTokenGenerationAndValidation12345}")
    private String secret;

    // 토큰 유효 기간 (7일)
    @Value("${jwt.expiration:604800000}")
    private Long expiration;

    /**
     * JWT 토큰 생성
     * @param username 사용자 아이디
     * @return JWT 토큰 문자열
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        // SecretKey 생성
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        // JWT 토큰 생성
        return Jwts.builder()
                .setSubject(username)           // 사용자 식별자
                .setIssuedAt(now)               // 발급 시간
                .setExpiration(expiryDate)      // 만료 시간
                .signWith(key)                  // 서명
                .compact();
    }

    /**
     * JWT 토큰에서 사용자명 추출
     * @param token JWT 토큰
     * @return 사용자명
     */
    public String getUsernameFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * JWT 토큰 유효성 검증
     * @param token JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰이 유효하지 않음
            return false;
        }
    }
}