package com.example.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

/**
 * Football-Data.org API 설정
 */
@Configuration
@ConfigurationProperties(prefix = "football.data.api")
@Getter
@Setter
public class FootballDataConfig {
    private String key;
    private String url;
}