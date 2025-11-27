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
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:*",
                        "https://sportscommunity-production-bb23.up.railway.app",
                        "https://*.railway.app"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}