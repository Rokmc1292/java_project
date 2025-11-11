package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 메인 애플리케이션
 * @EnableScheduling: 스케줄러 기능 활성화
 */
@SpringBootApplication
@EnableScheduling  // ⭐ 스케줄러 활성화 추가
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}