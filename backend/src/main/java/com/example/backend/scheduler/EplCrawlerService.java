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
 * EPL 크롤러 공통 서비스
 * Selenium WebDriver 설정 및 공통 유틸리티 제공
 */
@Service
@Slf4j
public class EplCrawlerService {

    /**
     * 팀 이름과 DB team_id 매핑
     * 크롤링한 팀 이름을 DB의 team_id로 변환
     */
    public static final Map<String, Long> TEAM_NAME_TO_ID = new HashMap<>() {{
        put("노팅엄", 1L);
        put("뉴캐슬", 2L);
        put("리버풀", 3L);
        put("리즈", 4L);
        put("맨시티", 5L);
        put("맨유", 6L);
        put("번리", 7L);
        put("본머스", 8L);
        put("브라이턴", 9L);
        put("브렌트퍼드", 10L);
        put("선덜랜드", 11L);
        put("아스널", 12L);
        put("애스턴 빌라", 13L);
        put("에버턴", 14L);
        put("울버햄튼", 15L);
        put("웨스트햄", 16L);
        put("첼시", 17L);
        put("토트넘", 18L);
        put("팰리스", 19L);
        put("풀럼", 20L);
    }};

    /**
     * Chrome WebDriver 설정 및 생성
     * @return 설정된 WebDriver 인스턴스
     */
    public WebDriver setupDriver() {
        // ChromeDriver 자동 설치 및 설정
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");  // 백그라운드 실행
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        return new ChromeDriver(options);
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
     * "종료" -> FINISHED, "예정" -> SCHEDULED, 기타 -> LIVE
     */
    public String convertStatus(String statusText) {
        if ("종료".equals(statusText)) {
            return "FINISHED";
        } else if ("예정".equals(statusText)) {
            return "SCHEDULED";
        } else {
            return "LIVE";
        }
    }

    /**
     * 팀 이름으로 팀 ID 조회
     */
    public Long getTeamId(String teamName) {
        return TEAM_NAME_TO_ID.get(teamName);
    }
}