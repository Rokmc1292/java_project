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
 * EPL 전체 시즌 일정 크롤러
 * 매일 새벽 3시에 2025-2026 시즌 전체 일정 크롤링
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EplScheduleCrawler {

    private final MatchRepository matchRepository;
    private final EplCrawlerService crawlerService;
    private final EntityManager entityManager;

    /**
     * 매일 새벽 3시에 EPL 전체 시즌 일정 크롤링
     * cron: "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledCrawling() {
        log.info("⏰ [스케줄] EPL 전체 시즌 일정 크롤링 시작 (매일 새벽 3시)");
        crawlFullSeason();
    }

    /**
     * 전체 시즌 크롤링 실행
     * 2025년 8월 ~ 2026년 5월
     */
    @Transactional
    public void crawlFullSeason() {
        log.info("=".repeat(60));
        log.info("EPL 2025-2026 시즌 전체 일정 크롤링 시작");
        log.info("기간: 2025년 8월 ~ 2026년 5월");
        log.info("=".repeat(60));

        WebDriver driver = null;
        List<MatchCrawlDto> allMatches = new ArrayList<>();

        try {
            driver = crawlerService.setupDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // 네이버 스포츠 EPL 일정 페이지
            String baseUrl = "https://sports.news.naver.com/wfootball/schedule/index?category=epl";
            driver.get(baseUrl);
            Thread.sleep(2000);  // 초기 페이지 로딩 대기

            // 2025년 8월~12월 크롤링
            for (int month = 8; month <= 12; month++) {
                List<MatchCrawlDto> monthMatches = crawlMonthSchedule(driver, wait, 2025, month);
                allMatches.addAll(monthMatches);
                Thread.sleep(1000);  // 월 간 전환 대기
            }

            // 2026년 1월~5월 크롤링
            for (int month = 1; month <= 5; month++) {
                List<MatchCrawlDto> monthMatches = crawlMonthSchedule(driver, wait, 2026, month);
                allMatches.addAll(monthMatches);
                Thread.sleep(1000);  // 월 간 전환 대기
            }

            log.info("🎉 전체 시즌 크롤링 완료!");
            log.info("📊 총 수집 경기: {}경기", allMatches.size());

            // DB에 저장
            saveMatchesToDatabase(allMatches);

        } catch (Exception e) {
            log.error("❌ 크롤링 실패", e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * 특정 월의 경기 일정 크롤링
     */
    private List<MatchCrawlDto> crawlMonthSchedule(WebDriver driver, WebDriverWait wait, int year, int month) {
        log.info("📅 {}년 {}월 크롤링 중...", year, month);
        List<MatchCrawlDto> monthMatches = new ArrayList<>();

        try {
            // 월 탭 클릭
            String monthXpath = String.format("//button[contains(@class, 'CalendarDate_tab__WFXXe')]//em[text()='%d']", month);
            WebElement monthButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(monthXpath)));
            monthButton.click();
            Thread.sleep(2000);  // 페이지 로딩 대기

            // 경기 일정 그룹 찾기
            List<WebElement> dateGroups = driver.findElements(By.cssSelector(".ScheduleLeagueType_match_list_group__\\+\\+HQY"));
            log.info("  ✅ {}개의 날짜 발견", dateGroups.size());

            for (WebElement group : dateGroups) {
                try {
                    // 날짜 제목
                    String dateText = group.findElement(By.cssSelector(".ScheduleLeagueType_title__K0rhC")).getText();

                    // 경기 목록
                    List<WebElement> matches = group.findElements(By.cssSelector(".MatchBox_match_item__WiPhj"));

                    for (WebElement match : matches) {
                        try {
                            MatchCrawlDto matchDto = extractMatchData(match, dateText, year);
                            if (matchDto != null) {
                                monthMatches.add(matchDto);
                            }
                        } catch (Exception e) {
                            log.warn("⚠️ 개별 경기 추출 실패", e);
                        }
                    }
                } catch (Exception e) {
                    log.warn("⚠️ 날짜 그룹 처리 실패", e);
                }
            }

            log.info("  ✅ {}년 {}월: {}경기 수집 완료", year, month, monthMatches.size());

        } catch (Exception e) {
            log.error("  ❌ {}년 {}월 크롤링 실패", year, month, e);
        }

        return monthMatches;
    }

    /**
     * 개별 경기 데이터 추출
     */
    private MatchCrawlDto extractMatchData(WebElement matchElement, String dateText, int year) {
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

            if (scores.size() >= 2 && ("FINISHED".equals(status) || "LIVE".equals(status))) {
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

            // 날짜 파싱
            LocalDateTime matchDate = parseDate(dateText, matchTime, year);
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
     */
    private LocalDateTime parseDate(String dateText, String timeText, int year) {
        try {
            // "3월 15일(토)" -> 3, 15 추출
            String datePart = dateText.split("\\(")[0].strip();
            int month = Integer.parseInt(datePart.split("월")[0]);
            int day = Integer.parseInt(datePart.split("월")[1].replace("일", "").strip());

            // "14:30" -> 14, 30 추출
            timeText = crawlerService.cleanText(timeText);
            String[] timeParts = timeText.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            return LocalDateTime.of(year, month, day, hour, minute);

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
        log.info("💾 DB 저장 시작...");

        int savedCount = 0;
        int skippedCount = 0;

        // EPL 리그 조회 (league_id = 1)
        League eplLeague = entityManager.getReference(League.class, 1L);

        for (MatchCrawlDto dto : matchDtos) {
            try {
                // 팀 ID 조회
                Long homeTeamId = crawlerService.getTeamId(dto.getHomeTeamName());
                Long awayTeamId = crawlerService.getTeamId(dto.getAwayTeamName());

                if (homeTeamId == null || awayTeamId == null) {
                    log.warn("⚠️ 팀 매핑 실패: {} vs {}", dto.getHomeTeamName(), dto.getAwayTeamName());
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
                        existingMatch.setHomeScore(dto.getHomeScore());
                        existingMatch.setAwayScore(dto.getAwayScore());
                        existingMatch.setVenue(dto.getVenue());
                        matchRepository.save(existingMatch);
                        log.debug("✅ 경기 업데이트: {} vs {} ({})",
                            dto.getHomeTeamName(), dto.getAwayTeamName(), dto.getStatus());
                    } else if ("FINISHED".equals(dto.getStatus())) {
                        // 둘 다 FINISHED인 경우는 점수만 업데이트 (점수 수정 가능성)
                        existingMatch.setHomeScore(dto.getHomeScore());
                        existingMatch.setAwayScore(dto.getAwayScore());
                        matchRepository.save(existingMatch);
                        log.debug("✅ 종료 경기 점수 업데이트: {} {} - {} {}",
                            dto.getHomeTeamName(), dto.getHomeScore(), dto.getAwayScore(), dto.getAwayTeamName());
                    } else {
                        // 기존 FINISHED 경기를 다른 상태로 변경하려는 시도 차단
                        log.warn("⚠️ FINISHED 경기 보호: {} vs {} (크롤링 상태: {} → 무시)",
                            dto.getHomeTeamName(), dto.getAwayTeamName(), dto.getStatus());
                    }
                } else {
                    // 새 경기 생성
                    Match match = new Match();
                    match.setLeague(eplLeague);
                    match.setHomeTeam(homeTeam);
                    match.setAwayTeam(awayTeam);
                    match.setMatchDate(dto.getMatchDate());
                    match.setVenue(dto.getVenue());
                    match.setStatus(dto.getStatus());
                    match.setHomeScore(dto.getHomeScore());
                    match.setAwayScore(dto.getAwayScore());

                    matchRepository.save(match);
                }

                savedCount++;

            } catch (Exception e) {
                log.warn("⚠️ 경기 저장 실패: {} vs {}", dto.getHomeTeamName(), dto.getAwayTeamName(), e);
                skippedCount++;
            }
        }

        log.info("✅ DB 저장 완료!");
        log.info("📊 저장/업데이트된 경기: {}경기", savedCount);
        log.info("⚠️ 스킵된 경기: {}경기", skippedCount);
    }
}