package com.example.backend.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * DataSource 설정
 * MySQL 연결 끊김 방지를 위한 HikariCP 설정 및 JDBC URL 파라미터 자동 추가
 */
@Configuration
@Slf4j
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    @org.springframework.context.annotation.Primary
    public DataSource dataSource() {
        // MySQL 연결 유지를 위한 필수 파라미터 추가
        String enhancedUrl = enhanceJdbcUrl(datasourceUrl);

        log.info("📊 DataSource 초기화");
        log.info("  원본 URL: {}", maskPassword(datasourceUrl));
        log.info("  개선된 URL: {}", maskPassword(enhancedUrl));

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(enhancedUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);

        // HikariCP 최적화 설정
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setValidationTimeout(5000);
        config.setKeepaliveTime(300000);
        config.setLeakDetectionThreshold(60000);
        config.setAutoCommit(true);

        // 연결 테스트 쿼리
        config.setConnectionTestQuery("SELECT 1");

        // Pool 이름 설정
        config.setPoolName("Sports-HikariCP");

        log.info("✅ HikariCP 설정 완료");
        log.info("  최대 연결 수: {}", config.getMaximumPoolSize());
        log.info("  최소 유휴 연결: {}", config.getMinimumIdle());
        log.info("  Keepalive 시간: {}ms ({}분)", config.getKeepaliveTime(), config.getKeepaliveTime() / 60000);
        log.info("  연결 최대 수명: {}ms ({}분)", config.getMaxLifetime(), config.getMaxLifetime() / 60000);

        return new HikariDataSource(config);
    }

    /**
     * JDBC URL에 MySQL 연결 유지를 위한 필수 파라미터 추가
     */
    private String enhanceJdbcUrl(String originalUrl) {
        if (originalUrl == null || !originalUrl.startsWith("jdbc:mysql")) {
            return originalUrl;
        }

        // 이미 파라미터가 있는지 확인
        boolean hasParams = originalUrl.contains("?");
        StringBuilder urlBuilder = new StringBuilder(originalUrl);

        // 필수 파라미터들 (serverTimezone 제외 - DB 타임존과 충돌 방지)
        String[] requiredParams = {
            "autoReconnect=true",              // 자동 재연결
            "useSSL=false",                     // SSL 설정 (필요시 true로 변경)
            "allowPublicKeyRetrieval=true",    // MySQL 8.0+ 인증
            "useUnicode=true",                 // 유니코드 사용
            "characterEncoding=UTF-8",         // 문자 인코딩
            "cachePrepStmts=true",            // PreparedStatement 캐싱
            "useServerPrepStmts=true",        // 서버 측 PreparedStatement 사용
            "rewriteBatchedStatements=true",  // Batch 쿼리 최적화
            "maintainTimeStats=false"          // 성능 향상을 위해 시간 통계 비활성화
        };

        for (String param : requiredParams) {
            String paramName = param.split("=")[0];

            // 해당 파라미터가 이미 URL에 있는지 확인
            if (!originalUrl.contains(paramName + "=")) {
                if (!hasParams) {
                    urlBuilder.append("?");
                    hasParams = true;
                } else {
                    urlBuilder.append("&");
                }
                urlBuilder.append(param);
            }
        }

        return urlBuilder.toString();
    }

    /**
     * 로그 출력 시 비밀번호 마스킹
     */
    private String maskPassword(String url) {
        if (url == null) {
            return null;
        }
        // password= 파라미터가 있으면 마스킹
        return url.replaceAll("password=[^&]*", "password=****");
    }
}
