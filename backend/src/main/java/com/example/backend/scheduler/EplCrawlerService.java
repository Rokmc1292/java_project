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
public class EplCrawlerService extends BaseCrawlerService {

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

        // 진행 중인 경기
        // 1) 시간 표시: "1'", "45'", "45'+2'" 등
        // 2) 하프타임: "HT", "Half Time"
        // 3) 전후반: "전반", "후반"
        // 4) 추가시간: "AT" (Additional Time)
        if (statusText.matches("\\d+'.*")
                || "HT".equals(statusText)
                || "Half Time".equals(statusText)
                || "전반".equals(statusText)
                || "후반".equals(statusText)
                || "AT".equals(statusText)
                || statusText.contains("전반")
                || statusText.contains("후반")) {
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