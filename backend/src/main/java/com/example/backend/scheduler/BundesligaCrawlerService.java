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
 * 분데스리가 크롤러 공통 서비스
 * Selenium WebDriver 설정 및 공통 유틸리티 제공
 */
@Service
@Slf4j
public class BundesligaCrawlerService extends BaseCrawlerService {

    /**
     * 팀 이름과 DB team_id 매핑
     * 크롤링한 팀 이름을 DB의 team_id로 변환
     */
    public static final Map<String, Long> TEAM_NAME_TO_ID = new HashMap<>() {{
        put("바이에른 뮌헨", 71L);
        put("레버쿠젠", 72L);
        put("프랑크푸르트", 73L);
        put("도르트문트", 74L);
        put("프라이부르크", 75L);
        put("마인츠", 76L);
        put("라이프치히", 77L);
        put("브레멘", 78L);
        put("슈투트가르트", 79L);
        put("글라트바흐", 80L);
        put("볼프스부르크", 81L);
        put("아우크스부르크", 82L);
        put("우니온 베를린", 83L);
        put("상 파울리", 84L);
        put("호펜하임", 85L);
        put("하이덴하임", 86L);
        put("함부르크", 87L);
        put("쾰른", 88L);
    }};

    /**
     * Chrome WebDriver 설정 및 생성
     * @return 설정된 WebDriver 인스턴스
     */

    /**
     * 텍스트 정리 (줄바꿈 및 공백 제거)
     */

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
