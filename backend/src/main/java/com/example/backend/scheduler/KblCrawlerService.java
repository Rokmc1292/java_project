package com.example.backend.scheduler;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * KBL 크롤러 공통 서비스
 * Selenium WebDriver 설정 및 공통 유틸리티 제공
 */
@Service
@Slf4j
public class KblCrawlerService extends BaseCrawlerService {

    /**
     * 팀 이름과 DB team_id 매핑
     * 크롤링한 팀 이름을 DB의 team_id로 변환
     */
    public static final Map<String, Long> TEAM_NAME_TO_ID = new HashMap<>() {{
        put("서울 SK", 147L);
        put("창원 LG", 148L);
        put("울산 현대모비스", 149L);
        put("수원 KT", 150L);
        put("대구 한국가스공사", 151L);
        put("안양 정관장", 152L);
        put("원주 DB", 153L);
        put("고양 소노", 154L);
        put("부산 KCC", 155L);
        put("서울 삼성", 156L);
    }};

    /**
     * Chrome WebDriver 설정 및 생성
     * @return 설정된 WebDriver 인스턴스
     */
    public WebDriver setupDriver() {
        try {
            // ChromeDriver 자동 설치 및 설정
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");  // 새로운 headless 모드
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-blink-features=AutomationControlled");

            // 추가 안정성 옵션
            options.addArguments("--disable-software-rasterizer");
            options.addArguments("--disable-background-timer-throttling");
            options.addArguments("--disable-backgrounding-occluded-windows");
            options.addArguments("--disable-renderer-backgrounding");
            options.addArguments("--disable-features=IsolateOrigins,site-per-process");
            options.addArguments("--disable-web-security");  // CORS 이슈 방지
            options.addArguments("--allow-running-insecure-content");

            // 페이지 로드 전략 (eager: DOM이 로드되면 바로 반환)
            options.setPageLoadStrategy(org.openqa.selenium.PageLoadStrategy.NORMAL);

            // User-Agent 설정
            options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            WebDriver driver = new ChromeDriver(options);

            // 타임아웃 설정 (비용 절감 최적화: 대기 시간 단축)
            driver.manage().timeouts().pageLoadTimeout(java.time.Duration.ofSeconds(15));  // 30→15초
            driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(3));    // 5→3초

            log.info("✅ WebDriver 초기화 성공 (KBL)");
            return driver;
        } catch (Exception e) {
            log.error("❌ WebDriver 초기화 실패: {}", e.getMessage());
            throw new RuntimeException("WebDriver 초기화 실패", e);
        }
    }

    /**
     * 텍스트 정리 (줄바꿈 및 공백 제거)
     */
    public String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String[] lines = text.strip().split("\n");
        return lines[lines.length - 1].strip();
    }

    /**
     * 경기 상태 텍스트 변환
     * 네이버 스포츠의 다양한 상태 텍스트를 정확하게 판단
     */
    public String convertStatus(String statusText) {
        if (statusText == null || statusText.isEmpty()) {
            return "SCHEDULED";
        }

        statusText = statusText.trim();

        // 종료된 경기
        if ("종료".equals(statusText) || "FT".equals(statusText) || "Full Time".equals(statusText)) {
            return "FINISHED";
        }

        // 예정된 경기
        if ("예정".equals(statusText) || "VS".equals(statusText)) {
            return "SCHEDULED";
        }

        // 연기/취소된 경기
        if ("연기".equals(statusText) || "취소".equals(statusText) || "POSTPONED".equals(statusText)) {
            return "POSTPONED";
        }

        // 진행 중인 경기 (농구)
        // 1) 쿼터 표시: "1Q", "2Q", "3Q", "4Q"
        // 2) 하프타임: "HT", "Half Time"
        // 3) 연장: "OT", "Overtime"
        if (statusText.matches("\\dQ")
                || "1Q".equals(statusText)
                || "2Q".equals(statusText)
                || "3Q".equals(statusText)
                || "4Q".equals(statusText)
                || "HT".equals(statusText)
                || "Half Time".equals(statusText)
                || "OT".equals(statusText)
                || statusText.contains("연장")
                || statusText.contains("쿼터")) {
            return "LIVE";
        }

        // 알 수 없는 상태 - 일단 로그만 남기고 그대로 반환하여 호출측에서 판단하도록
        log.warn("⚠️ 알 수 없는 경기 상태: '{}'", statusText);
        // 기본값을 LIVE로 변경 - 점수가 있는데 알 수 없는 상태면 대부분 경기중
        return "LIVE";
    }

    /**
     * 팀 이름으로 팀 ID 조회
     */
    public Long getTeamId(String teamName) {
        return TEAM_NAME_TO_ID.get(teamName);
    }
}
