package com.example.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

/**
 * The Odds API 설정 (UFC/MMA)
 */
@Configuration
@ConfigurationProperties(prefix = "theodds.api")
@Getter
@Setter
public class TheOddsConfig {
    private String key;
    private String url;
}