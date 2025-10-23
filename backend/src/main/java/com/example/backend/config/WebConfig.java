package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정
 * 정적 리소스 핸들러 설정
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 정적 리소스 핸들러 설정
     * /leagues/**, /teams/**, /fighters/** 경로를 static 폴더로 매핑
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 리그 로고
        registry.addResourceHandler("/leagues/**")
                .addResourceLocations("classpath:/static/leagues/");

        // 팀 로고
        registry.addResourceHandler("/teams/**")
                .addResourceLocations("classpath:/static/teams/");

        // 파이터 이미지
        registry.addResourceHandler("/fighters/**")
                .addResourceLocations("classpath:/static/fighters/");

        // 기본 정적 리소스
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
    }
}
