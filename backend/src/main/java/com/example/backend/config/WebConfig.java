package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 전역 CORS 설정
 * - 프론트엔드에서 백엔드 API 호출 허용
 * - 개발 환경과 프로덕션 환경 모두 지원
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * CORS 매핑 설정
     * - 모든 API 엔드포인트에 대해 CORS 허용
     * - 프론트엔드 개발 서버 및 배포 URL 허용
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // /api로 시작하는 모든 경로
                .allowedOrigins(
                        "http://localhost:5173",     // 개발 환경 (Vite)
                        "http://localhost:3000",     // 대체 개발 환경
                        "https://yourdomain.com"     // 프로덕션 환경 (배포 시 수정)
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 허용할 HTTP 메서드
                .allowedHeaders("*")           // 모든 헤더 허용
                .allowCredentials(true)        // 쿠키, 인증 헤더 허용
                .maxAge(3600);                 // Preflight 요청 캐시 시간 (1시간)
    }
}