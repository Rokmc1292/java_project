package com.example.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

/**
 * Ball Don't Lie API 설정 (농구)
 * API 키 불필요
 */
@Configuration
@ConfigurationProperties(prefix = "basketball.api")
@Getter
@Setter
public class BasketballConfig {
    private String url;
}