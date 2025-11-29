package com.example.backend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

/**
 * Spring Boot 메인 애플리케이션
 */
@SpringBootApplication
public class BackendApplication {

    /**
     * 애플리케이션 시작 시 기본 시간대를 한국 시간(KST)으로 설정
     * 서버가 UTC로 설정되어 있어도 애플리케이션 내부에서는 한국 시간을 사용
     */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        System.out.println("🌏 애플리케이션 기본 시간대: " + TimeZone.getDefault().getID());
    }

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    /**
     * 스케줄러 조건부 활성화
     * 환경 변수 ENABLE_SCHEDULING=false로 명시하지 않으면 기본적으로 활성화
     */
    @Configuration
    @EnableScheduling
    @ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
    static class SchedulingConfiguration {
    }
}