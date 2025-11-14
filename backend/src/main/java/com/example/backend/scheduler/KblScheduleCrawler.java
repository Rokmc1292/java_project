package com.example.backend.scheduler;

import com.example.backend.dto.MatchCrawlDto;
import com.example.backend.entity.League;
import com.example.backend.entity.Match;
import com.example.backend.entity.Team;
import com.example.backend.repository.MatchRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * KBL 전체 시즌 일정 크롤러
 * AdminController API를 통해 수동 실행
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KblScheduleCrawler {

    private final MatchRepository matchRepository;
    private final KblCrawlerService crawlerService;
    private final EntityManager entityManager;

    /**
     * 전체 시즌 크롤링 실행
     * 2025년 9월 ~ 2026년 4월
     */
    @Transactional
    public void crawlFullSeason() {
        log.info("=".repeat(60));
        log.info("KBL 2025-2026 시즌 전체 일정 크롤링 시작");
        log.info("기간: 2025년 9월 ~ 2026년 4월");
        log.info("=".repeat(60));

        WebDriver driver = null;
        List<MatchCrawlDto> allMatches = new ArrayList<>();
        int successMonths = 0;
        int failedMonths = 0;

        try {
            driver = crawlerService.setupDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // 네이버 스포츠 KBL 일정 페이지
            String baseUrl = "https://sports.news.naver.com/basketball/schedule/index?category=kbl";
            log.info("🌐 페이지 로드 중: {}", baseUrl);

            // 페이지 로드 재시도 로직
            boolean pageLoaded = false;
            for (int retry = 0; retry < 3; retry++) {
                try {
                    driver.get(baseUrl);
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body")));
                    Thread.sleep(2000);  // 동적 콘텐츠 로딩 대기

                    // 캘린더 탭이 로드되었는지 확인
                    wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".CalendarDate_tab__WFXXe")));

                    pageLoaded = true;
                    log.info("✅ 페이지 로드 완료");
                    break;
                } catch (Exception e) {
                    log.warn("⚠️ 페이지 로딩 실패 (시도 {}/3): {}", retry + 1, e.getMessage());
                    if (retry < 2) {
                        Thread.sleep(2000);
                    }
                }
            }

            if (!pageLoaded) {
                throw new RuntimeException("페이지 로드 실패: 3번 시도 후에도 실패");
            }

            // 2025년 9월~12월 크롤링
            for (int month = 9; month <= 12; month++) {
                try {
                    List<MatchCrawlDto> monthMatches = crawlMonthSchedule(driver, wait, 2025, month);
                    allMatches.addAll(monthMatches);
                    successMonths++;
                    Thread.sleep(1500);  // 월 간 전환 대기
                } catch (Exception e) {
                    log.error("  ❌ {}년 {}월 크롤링 실패 - 다음 달로 계속 진행", 2025, month, e);
                    failedMonths++;
                }
            }

            // 2026년 1월~4월 크롤링
            for (int month = 1; month <= 4; month++) {
                try {
                    List<MatchCrawlDto> monthMatches = crawlMonthSchedule(driver, wait, 2026, month);
                    allMatches.addAll(monthMatches);
                    successMonths++;
                    Thread.sleep(1500);  // 월 간 전환 대기
                } catch (Exception e) {
                    log.error("  ❌ {}년 {}월 크롤링 실패 - 다음 달로 계속 진행", 2026, month, e);
                    failedMonths++;
                }
            }

            log.info("🎉 전체 시즌 크롤링 완료!");
            log.info("📊 총 수집 경기: {}경기", allMatches.size());
            log.info("📈 성공한 월: {}개, 실패한 월: {}개", successMonths, failedMonths);

            // DB에 저장
            if (!allMatches.isEmpty()) {
                saveMatchesToDatabase(allMatches);
            } else {
                log.warn("⚠️ 수집된 경기가 없습니다. DB 저장을 건너뜁니다.");
            }

        } catch (Exception e) {
            log.error("❌ 크롤링 전체 실패", e);
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                    log.info("🔌 WebDriver 종료 완료");
                } catch (Exception e) {
                    log.warn("⚠️ WebDriver 종료 중 오류: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 특정 월의 경기 일정 크롤링
     */
    private List<MatchCrawlDto> crawlMonthSchedule(WebDriver driver, WebDriverWait wait, int year, int month) {
        log.info("📅 {}년 {}월 크롤링 중...", year, month);
        List<MatchCrawlDto> monthMatches = new ArrayList<>();
        int retries = 0;
        int maxRetries = 3;

        while (retries < maxRetries) {
            try {
                // 월 탭 클릭 - 재시도 로직 포함
                String monthXpath = String.format("//button[contains(@class, 'CalendarDate_tab__WFXXe')]//em[text()='%d']", month);
                WebElement monthButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(monthXpath)));
                monthButton.click();

                // 페이지가 업데이트될 때까지 명시적 대기
                Thread.sleep(2000);  // 초기 대기
                wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".ScheduleLeagueType_match_list_group__\\+\\+HQY")));
                Thread.sleep(500);  // 추가 안정화

                // 경기 일정 그룹 찾기
                List<WebElement> dateGroups = driver.findElements(By.cssSelector(".ScheduleLeagueType_match_list_group__\\+\\+HQY"));

                if (dateGroups.isEmpty()) {
                    log.warn("  ⚠️ {}년 {}월: 날짜 그룹을 찾을 수 없습니다. (시도 {}/{})", year, month, retries + 1, maxRetries);

                    // 디버깅 정보 출력
                    List<WebElement> anySchedule = driver.findElements(By.cssSelector("[class*='Schedule']"));
                    log.debug("  🔍 Schedule 관련 요소 수: {}", anySchedule.size());

                    retries++;
                    if (retries < maxRetries) {
                        Thread.sleep(2000);
                    }
                    continue;
                }

                log.info("  ✅ {}개의 날짜 발견", dateGroups.size());

                for (WebElement group : dateGroups) {
                    try {
                        // 날짜 제목
                        String dateText = group.findElement(By.cssSelector(".ScheduleLeagueType_title__K0rhC")).getText();

                        // 경기 목록
                        List<WebElement> matches = group.findElements(By.cssSelector(".MatchBox_match_item__WiPhj"));
                        log.debug("    {} - {}경기", dateText, matches.size());

                        for (WebElement match : matches) {
                            try {
                                MatchCrawlDto matchDto = extractMatchData(match, dateText, year, month);
                                if (matchDto != null) {
                                    monthMatches.add(matchDto);
                                }
                            } catch (Exception e) {
                                log.warn("    ⚠️ 개별 경기 추출 실패: {}", e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        log.warn("    ⚠️ 날짜 그룹 처리 실패: {}", e.getMessage());
                    }
                }

                log.info("  ✅ {}년 {}월: {}경기 수집 완료", year, month, monthMatches.size());
                break;  // 성공 시 루프 탈출

            } catch (Exception e) {
                retries++;
                log.error("  ❌ {}년 {}월 크롤링 실패 (시도 {}/{}): {}", year, month, retries, maxRetries, e.getMessage());

                if (retries >= maxRetries) {
                    log.error("  ❌ {}년 {}월: 최대 재시도 횟수 초과", year, month);
                    throw new RuntimeException("월별 크롤링 실패: " + year + "년 " + month + "월", e);
                }

                try {
                    Thread.sleep(3000);  // 재시도 전 대기
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return monthMatches;
    }

    /**
     * 개별 경기 데이터 추출
     */
    private MatchCrawlDto extractMatchData(WebElement matchElement, String dateText, int year, int targetMonth) {
        try {
            // 경기 시간
            String matchTime = matchElement.findElement(By.cssSelector(".MatchBox_time__Zt5-d")).getText().strip();

            // 경기장
            String venue = crawlerService.cleanText(matchElement.findElement(By.cssSelector(".MatchBox_stadium__3mzGU")).getText());

            // 경기 상태
            String statusText = matchElement.findElement(By.cssSelector(".MatchBox_status__xU6\\+d")).getText().strip();
            String status = crawlerService.convertStatus(statusText);

            // 팀 정보
            List<WebElement> teamItems = matchElement.findElements(By.cssSelector(".MatchBoxHeadToHeadArea_team_item__9ZknX"));

            if (teamItems.size() < 2) {
                return null;
            }

            // 홈팀, 원정팀 이름
            String homeTeam = teamItems.get(0).findElement(By.cssSelector(".MatchBoxHeadToHeadArea_team__l2ZxP")).getText();
            String awayTeam = teamItems.get(1).findElement(By.cssSelector(".MatchBoxHeadToHeadArea_team__l2ZxP")).getText();

            // 점수 (LIVE 또는 FINISHED 경기만 점수 있음)
            List<WebElement> scores = matchElement.findElements(By.cssSelector(".MatchBoxHeadToHeadArea_score__TChmp"));
            Integer homeScore = null;
            Integer awayScore = null;

            // SCHEDULED 상태가 아닐 때만 점수 파싱
            if ("FINISHED".equals(status) || "LIVE".equals(status)) {
                if (scores.size() >= 2) {
                    try {
                        String homeScoreText = scores.get(0).getText().trim();
                        String awayScoreText = scores.get(1).getText().trim();

                        if (!homeScoreText.isEmpty() && !awayScoreText.isEmpty()) {
                            homeScore = Integer.parseInt(homeScoreText);
                            awayScore = Integer.parseInt(awayScoreText);
                        }
                    } catch (NumberFormatException e) {
                        // 점수 파싱 실패시 null 유지
                        log.warn("⚠️ 점수 파싱 실패: {} vs {}", homeTeam, awayTeam);
                    }
                }
            }
            // SCHEDULED, POSTPONED 등은 무조건 점수를 null로 유지

            // 날짜 파싱
            LocalDateTime matchDate = parseDate(dateText, matchTime, year, targetMonth);
            if (matchDate == null) {
                return null;
            }

            // DTO 생성
            return MatchCrawlDto.builder()
                    .matchDate(matchDate)
                    .homeTeamName(homeTeam)
                    .awayTeamName(awayTeam)
                    .homeScore(homeScore)
                    .awayScore(awayScore)
                    .status(status)
                    .venue(venue)
                    .build();

        } catch (Exception e) {
            log.warn("⚠️ 경기 데이터 추출 실패", e);
            return null;
        }
    }

    /**
     * 날짜 파싱
     * "3월 15일(토)" + "14:30" -> LocalDateTime
     * targetMonth를 기준으로 연도를 자동 조정 (연말/연초 경기 처리)
     */
    private LocalDateTime parseDate(String dateText, String timeText, int year, int targetMonth) {
        try {
            // "3월 15일(토)" -> 3, 15 추출
            String datePart = dateText.split("\\(")[0].strip();
            int parsedMonth = Integer.parseInt(datePart.split("월")[0]);
            int day = Integer.parseInt(datePart.split("월")[1].replace("일", "").strip());

            // "14:30" -> 14, 30 추출
            timeText = crawlerService.cleanText(timeText);
            String[] timeParts = timeText.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // 연도 자동 조정
            // 예: 12월 크롤링 중 1월 경기가 나오면 다음 해로 조정
            // 예: 1월 크롤링 중 12월 경기가 나오면 이전 해로 조정
            int adjustedYear = year;
            if (targetMonth == 12 && parsedMonth == 1) {
                adjustedYear = year + 1;
            } else if (targetMonth == 1 && parsedMonth == 12) {
                adjustedYear = year - 1;
            }

            return LocalDateTime.of(adjustedYear, parsedMonth, day, hour, minute);

        } catch (Exception e) {
            log.warn("⚠️ 날짜 파싱 오류: {} | {}", dateText, timeText);
            return null;
        }
    }

    /**
     * 크롤링한 데이터를 DB에 저장
     */
    @Transactional
    public void saveMatchesToDatabase(List<MatchCrawlDto> matchDtos) {
        log.info("💾 DB 저장 시작... (총 {}경기)", matchDtos.size());

        int newMatchCount = 0;
        int updatedMatchCount = 0;
        int skippedCount = 0;

        // KBL 리그 조회 (league_id = 10)
        League kblLeague;
        try {
            kblLeague = entityManager.getReference(League.class, 10L);
        } catch (Exception e) {
            log.error("❌ KBL 리그 정보를 찾을 수 없습니다. DB에 리그 데이터가 있는지 확인하세요.", e);
            return;
        }

        for (MatchCrawlDto dto : matchDtos) {
            try {
                // 팀 ID 조회
                Long homeTeamId = crawlerService.getTeamId(dto.getHomeTeamName());
                Long awayTeamId = crawlerService.getTeamId(dto.getAwayTeamName());

                if (homeTeamId == null || awayTeamId == null) {
                    log.debug("  ⚠️ 팀 매핑 실패: {} vs {} (DB에 팀 정보 없음)", dto.getHomeTeamName(), dto.getAwayTeamName());
                    skippedCount++;
                    continue;
                }

                // 팀 엔티티 참조
                Team homeTeam = entityManager.getReference(Team.class, homeTeamId);
                Team awayTeam = entityManager.getReference(Team.class, awayTeamId);

                // 이미 존재하는 경기인지 확인 (중복 방지)
                List<Match> existingMatches = matchRepository.findByMatchDate(dto.getMatchDate());
                Match existingMatch = existingMatches.stream()
                        .filter(m -> m.getHomeTeam().getTeamId().equals(homeTeamId)
                                && m.getAwayTeam().getTeamId().equals(awayTeamId))
                        .findFirst()
                        .orElse(null);

                if (existingMatch != null) {
                    // 기존 경기 업데이트
                    // ⚠️ 중요: FINISHED 경기는 상태를 변경하지 않음 (보호)
                    String currentStatus = existingMatch.getStatus();

                    if (!"FINISHED".equals(currentStatus)) {
                        // FINISHED가 아닌 경우에만 상태 업데이트
                        existingMatch.setStatus(dto.getStatus());
                        existingMatch.setVenue(dto.getVenue());

                        // SCHEDULED 상태는 무조건 점수를 null로 설정
                        if ("SCHEDULED".equals(dto.getStatus()) || "POSTPONED".equals(dto.getStatus())) {
                            existingMatch.setHomeScore(null);
                            existingMatch.setAwayScore(null);
                        } else {
                            // LIVE나 다른 상태는 크롤링된 점수 사용
                            existingMatch.setHomeScore(dto.getHomeScore());
                            existingMatch.setAwayScore(dto.getAwayScore());
                        }

                        matchRepository.save(existingMatch);
                        updatedMatchCount++;
                        log.debug("  ✅ 업데이트: {} vs {} ({})",
                            dto.getHomeTeamName(), dto.getAwayTeamName(), dto.getStatus());
                    } else if ("FINISHED".equals(dto.getStatus())) {
                        // 둘 다 FINISHED인 경우는 점수만 업데이트 (점수 수정 가능성)
                        existingMatch.setHomeScore(dto.getHomeScore());
                        existingMatch.setAwayScore(dto.getAwayScore());
                        matchRepository.save(existingMatch);
                        updatedMatchCount++;
                        log.debug("  ✅ 점수 업데이트: {} {} - {} {}",
                            dto.getHomeTeamName(), dto.getHomeScore(), dto.getAwayScore(), dto.getAwayTeamName());
                    } else {
                        // 기존 FINISHED 경기를 다른 상태로 변경하려는 시도 차단
                        log.debug("  ⏭️ FINISHED 경기 보호: {} vs {}", dto.getHomeTeamName(), dto.getAwayTeamName());
                        skippedCount++;
                    }
                } else {
                    // 새 경기 생성
                    Match match = new Match();
                    match.setLeague(kblLeague);
                    match.setHomeTeam(homeTeam);
                    match.setAwayTeam(awayTeam);
                    match.setMatchDate(dto.getMatchDate());
                    match.setVenue(dto.getVenue());
                    match.setStatus(dto.getStatus());

                    // SCHEDULED 상태는 무조건 점수를 null로 설정
                    if ("SCHEDULED".equals(dto.getStatus()) || "POSTPONED".equals(dto.getStatus())) {
                        match.setHomeScore(null);
                        match.setAwayScore(null);
                    } else {
                        match.setHomeScore(dto.getHomeScore());
                        match.setAwayScore(dto.getAwayScore());
                    }

                    matchRepository.save(match);
                    newMatchCount++;
                    log.debug("  ✨ 새 경기: {} vs {} ({})",
                        dto.getHomeTeamName(), dto.getAwayTeamName(), dto.getMatchDate().toLocalDate());
                }

            } catch (Exception e) {
                log.warn("  ⚠️ 경기 저장 실패: {} vs {} - {}",
                    dto.getHomeTeamName(), dto.getAwayTeamName(), e.getMessage());
                skippedCount++;
            }
        }

        log.info("✅ DB 저장 완료!");
        log.info("  ✨ 새 경기: {}개", newMatchCount);
        log.info("  🔄 업데이트: {}개", updatedMatchCount);
        log.info("  ⏭️ 스킵: {}개", skippedCount);
    }
}
