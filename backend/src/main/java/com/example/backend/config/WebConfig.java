// backend/src/main/java/com/example/backend/config/WebConfig.java
package com.example.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/admin-page/**")  // 관리자 페이지 경로
                .excludePathPatterns("/api/auth/**");   // 인증 경로는 제외
    }

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