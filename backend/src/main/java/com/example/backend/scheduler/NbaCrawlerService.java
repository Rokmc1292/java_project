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
 * NBA 크롤러 공통 서비스
 * Selenium WebDriver 설정 및 공통 유틸리티 제공
 */
@Service
@Slf4j
public class NbaCrawlerService {

    /**
     * NBA 팀 이름과 DB team_id 매핑
     * 크롤링한 팀 이름을 DB의 team_id로 변환
     * DB의 실제 팀 이름과 네이버 스포츠 크롤링 이름 모두 매핑
     */
    public static final Map<String, Long> TEAM_NAME_TO_ID = new HashMap<>() {{
        // 네이버 스포츠 크롤링 이름 (짧은 이름)
        put("보스턴", 21L);
        put("브루클린", 22L);
        put("뉴욕", 23L);
        put("필라델피아", 24L);
        put("토론토", 25L);
        put("시카고", 26L);
        put("클리블랜드", 27L);
        put("디트로이트", 28L);
        put("인디애나", 29L);
        put("밀워키", 30L);
        put("애틀랜타", 31L);
        put("샬럿", 32L);
        put("마이애미", 33L);
        put("올랜도", 34L);
        put("워싱턴", 35L);
        put("덴버", 36L);
        put("미네소타", 37L);
        put("오클라호마", 38L);
        put("오클라호마시티", 38L);
        put("포틀랜드", 39L);
        put("유타", 40L);
        put("골든스테이트", 41L);
        put("LA클리퍼스", 42L);
        put("LA레이커스", 43L);
        put("피닉스", 44L);
        put("새크라멘토", 45L);
        put("댈러스", 46L);
        put("휴스턴", 47L);
        put("멤피스", 48L);
        put("뉴올리언스", 49L);
        put("샌안토니오", 50L);

        // DB의 전체 팀 이름 (백업용)
        put("보스턴 셀틱스", 21L);
        put("브루클린 네츠", 22L);
        put("뉴욕 닉스", 23L);
        put("필라델피아 76ers", 24L);
        put("토론토 랩터스", 25L);
        put("시카고 불스", 26L);
        put("클리블랜드 캐벌리어스", 27L);
        put("디트로이트 피스톤즈", 28L);
        put("인디애나 페이서스", 29L);
        put("밀워키 벅스", 30L);
        put("애틀랜타 호크스", 31L);
        put("샬럿 호넷츠", 32L);
        put("마이애미 히트", 33L);
        put("올랜도 매직", 34L);
        put("워싱턴 위저즈", 35L);
        put("덴버 너기츠", 36L);
        put("미네소타 팀버울브스", 37L);
        put("오클라호마시티 썬더", 38L);
        put("포틀랜드 트레일블레이저스", 39L);
        put("유타 재즈", 40L);
        put("골든스테이트 워리어스", 41L);
        put("LA 클리퍼스", 42L);
        put("LA 레이커스", 43L);
        put("피닉스 선스", 44L);
        put("새크라멘토 킹스", 45L);
        put("댈러스 매버릭스", 46L);
        put("휴스턴 로키츠", 47L);
        put("멤피스 그리즐리스", 48L);
        put("뉴올리언스 펠리컨스", 49L);
        put("샌안토니오 스퍼스", 50L);
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

            log.info("✅ WebDriver 초기화 성공");
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

        // 진행 중인 경기 (쿼터 표시 등)
        // "1Q", "2Q", "3Q", "4Q", "OT" 등
        if (statusText.matches("\\d+Q") || "OT".equals(statusText) || statusText.contains("연장")) {
            return "LIVE";
        }

        // 알 수 없는 상태는 SCHEDULED로 처리 (안전한 기본값)
        log.warn("⚠️ 알 수 없는 경기 상태: '{}', SCHEDULED로 처리", statusText);
        return "SCHEDULED";
    }

    /**
     * 팀 이름으로 팀 ID 조회
     */
    public Long getTeamId(String teamName) {
        return TEAM_NAME_TO_ID.get(teamName);
    }
}
