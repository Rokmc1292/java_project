package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * CORS 매핑 설정
     * - 모든 API 엔드포인트에 대해 CORS 허용
     * - 프론트엔드 개발 서버 및 배포 URL 허용
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 모든 경로 (API뿐만 아니라 WebSocket도 포함)
                .allowedOrigins(
                        "http://localhost:5173",     // 개발 환경 (Vite)
                        "http://localhost:3000",     // 대체 개발 환경
                        "https://sportscommunity-production-bb23.up.railway.app",  // Railway 프론트엔드
                        frontendUrl                  // 환경 변수로 설정된 URL
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}