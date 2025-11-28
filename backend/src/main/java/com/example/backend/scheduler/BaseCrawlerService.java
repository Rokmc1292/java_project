package com.example.backend.scheduler;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * 크롤러 베이스 서비스
 * 모든 크롤러가 공통으로 사용하는 WebDriver 설정
 */
@Slf4j
public abstract class BaseCrawlerService {

    /**
     * Chrome WebDriver 설정 및 생성
     * Alpine Linux 및 로컬 환경 모두 지원
     * @return 설정된 WebDriver 인스턴스
     */
    protected WebDriver setupDriver() {
        try {
            // Alpine Linux에서 시스템 chromedriver 사용
            String chromeDriverPath = System.getenv("CHROME_DRIVER");
            if (chromeDriverPath != null && !chromeDriverPath.isEmpty()) {
                System.setProperty("webdriver.chrome.driver", chromeDriverPath);
                log.info("🔧 시스템 ChromeDriver 사용: {}", chromeDriverPath);
            } else {
                // 로컬 개발 환경에서는 WebDriverManager 사용
                WebDriverManager.chromedriver().setup();
                log.info("🔧 WebDriverManager로 ChromeDriver 설정");
            }

            ChromeOptions options = new ChromeOptions();

            // Alpine Linux Chromium 바이너리 경로 설정
            String chromeBin = System.getenv("CHROME_BIN");
            if (chromeBin != null && !chromeBin.isEmpty()) {
                options.setBinary(chromeBin);
                log.info("🔧 Chromium 바이너리: {}", chromeBin);
            }

            // Headless 모드 및 최적화 옵션
            options.addArguments("--headless=new");
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
            options.addArguments("--disable-web-security");
            options.addArguments("--allow-running-insecure-content");

            // 페이지 로드 전략
            options.setPageLoadStrategy(org.openqa.selenium.PageLoadStrategy.NORMAL);

            // User-Agent 설정
            options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            WebDriver driver = new ChromeDriver(options);

            // 타임아웃 설정
            driver.manage().timeouts().pageLoadTimeout(java.time.Duration.ofSeconds(15));
            driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(3));

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
    protected String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    /**
     * WebDriver 안전하게 종료 (메모리 누수 방지)
     * @param driver 종료할 WebDriver 인스턴스
     */
    protected void safeQuitDriver(WebDriver driver) {
        if (driver != null) {
            try {
                driver.quit();
                log.info("✅ WebDriver 정상 종료");
            } catch (Exception e) {
                log.warn("⚠️ WebDriver 종료 중 오류 (무시됨): {}", e.getMessage());
            }
        }
    }
}
