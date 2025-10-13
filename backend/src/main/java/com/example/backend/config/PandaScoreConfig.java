package com.example.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

/**
 * PandaScore API 설정 (e스포츠)
 */
@Configuration
@ConfigurationProperties(prefix = "pandascore")
@Getter
@Setter
public class PandaScoreConfig {
    private String token;
    private String url;
}