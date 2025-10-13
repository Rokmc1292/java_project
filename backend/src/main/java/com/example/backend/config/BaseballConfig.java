package com.example.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

/**
 * MLB Stats API 설정 (야구)
 * API 키 불필요
 */
@Configuration
@ConfigurationProperties(prefix = "baseball.api")
@Getter
@Setter
public class BaseballConfig {
    private String url;
}