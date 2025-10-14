package com.example.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * API-SPORTS.IO 직접 API 설정
 * 각 사이트에서 개별 API 키 발급
 */
@Configuration
@Getter
@Setter
public class ApiSportsConfig {

    // 축구 API 설정
    @org.springframework.beans.factory.annotation.Value("${api.football.key}")
    private String footballApiKey;

    @org.springframework.beans.factory.annotation.Value("${api.football.url}")
    private String footballUrl;

    // 농구 API 설정
    @org.springframework.beans.factory.annotation.Value("${api.basketball.key}")
    private String basketballApiKey;

    @org.springframework.beans.factory.annotation.Value("${api.basketball.url}")
    private String basketballUrl;

    // 야구 API 설정
    @org.springframework.beans.factory.annotation.Value("${api.baseball.key}")
    private String baseballApiKey;

    @org.springframework.beans.factory.annotation.Value("${api.baseball.url}")
    private String baseballUrl;

    // MMA API 설정
    @org.springframework.beans.factory.annotation.Value("${api.mma.key}")
    private String mmaApiKey;

    @org.springframework.beans.factory.annotation.Value("${api.mma.url}")
    private String mmaUrl;

    // e스포츠 API 설정
    @org.springframework.beans.factory.annotation.Value("${pandascore.token}")
    private String pandascoreToken;

    @org.springframework.beans.factory.annotation.Value("${pandascore.url}")
    private String pandascoreUrl;

    /**
     * 주요 축구 리그 ID
     * EPL(39), 분데스리가(78), 라리가(140), 세리에A(135)
     */
    public static final int[] FOOTBALL_LEAGUE_IDS = {39, 78, 140, 135};

    /**
     * NBA 리그 ID
     */
    public static final int NBA_LEAGUE_ID = 12;

    /**
     * MLB 리그 ID
     */
    public static final int MLB_LEAGUE_ID = 1;

    /**
     * UFC 리그 ID (MMA)
     */
    public static final int UFC_LEAGUE_ID = 1;

    // ============================================
    // 🔥 여기만 수정하면 됩니다!
    // ============================================

    /**
     * 유료 플랜 여부
     * true = 유료 플랜 (현재 시즌 사용)
     * false = 무료 플랜 (2023 시즌 고정)
     */
    private static final boolean IS_PREMIUM_PLAN = false;  // ✅ 유료 전환 시 true로 변경

    /**
     * 무료 플랜에서 사용 가능한 고정 시즌
     */
    private static final int FREE_FOOTBALL_SEASON = 2023;
    private static final String FREE_BASKETBALL_SEASON = "2022-2023";
    private static final int FREE_BASEBALL_SEASON = 2023;

    /**
     * 현재 축구 시즌 계산 (유료 플랜용)
     * 예: 2024년 8월 = 2024/2025 시즌 → 2024 반환
     */
    private static int getCurrentFootballSeason() {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        // 8월 이후면 새 시즌 시작 (예: 2024년 8월 = 2024/2025 시즌)
        if (month >= 8) {
            return year;
        } else {
            return year - 1;
        }
    }

    /**
     * 현재 농구 시즌 계산 (유료 플랜용)
     * 예: 2024년 10월 = 2024-2025 시즌
     */
    private static String getCurrentBasketballSeason() {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        // 10월 이후면 새 시즌 시작 (예: 2024년 10월 = 2024-2025 시즌)
        if (month >= 10) {
            return year + "-" + (year + 1);
        } else {
            return (year - 1) + "-" + year;
        }
    }

    /**
     * 현재 야구 시즌 계산 (유료 플랜용)
     * 예: 2024년 = 2024 시즌
     */
    private static int getCurrentBaseballSeason() {
        return LocalDate.now().getYear();
    }

    /**
     * 축구 시즌 가져오기 (무료/유료 자동 선택)
     */
    public static int getFootballSeason() {
        return IS_PREMIUM_PLAN ? getCurrentFootballSeason() : FREE_FOOTBALL_SEASON;
    }

    /**
     * 농구 시즌 가져오기 (무료/유료 자동 선택)
     */
    public static String getBasketballSeason() {
        return IS_PREMIUM_PLAN ? getCurrentBasketballSeason() : FREE_BASKETBALL_SEASON;
    }

    /**
     * 야구 시즌 가져오기 (무료/유료 자동 선택)
     */
    public static int getBaseballSeason() {
        return IS_PREMIUM_PLAN ? getCurrentBaseballSeason() : FREE_BASEBALL_SEASON;
    }

    /**
     * 플랜 상태 확인
     */
    public static boolean isPremiumPlan() {
        return IS_PREMIUM_PLAN;
    }
}