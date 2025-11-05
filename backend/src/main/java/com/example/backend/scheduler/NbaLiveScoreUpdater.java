package com.example.backend.scheduler;

import com.example.backend.entity.Match;
import com.example.backend.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * NBA 실시간 점수 업데이터
 * 30초마다 LIVE 상태 경기의 점수를 크롤링하여 업데이트
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NbaLiveScoreUpdater {

    private final MatchRepository matchRepository;
    private final NbaCrawlerService crawlerService;

    /**
     * 30초마다 실시간 점수 업데이트
     * fixedDelay: 이전 실행이 끝난 후 30초 대기
     * initialDelay: 서버 시작 후 10초 뒤 첫 실행
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    @Transactional
    public void updateLiveScores() {
        // NBA 리그의 LIVE 경기 조회 (league_id = 2)
        List<Match> liveMatches = matchRepository.findLiveMatchesByLeague(2L);

        if (liveMatches.isEmpty()) {
            // LIVE 경기가 없으면 로그 출력 안함 (너무 많은 로그 방지)
            return;
        }

        log.info("🏀 [실시간 업데이트] LIVE 경기 {}개 발견, 점수 업데이트 시작", liveMatches.size());

        WebDriver driver = null;

        try {
            driver = crawlerService.setupDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // 네이버 스포츠 NBA 일정 페이지 (오늘 날짜)
            String baseUrl = "https://m.sports.naver.com/basketball/schedule/index?category=nba";
            driver.get(baseUrl);
            Thread.sleep(1500);  // 페이지 로딩 대기

            // 오늘 경기 목록 찾기
            List<WebElement> matchElements = driver.findElements(By.cssSelector(".MatchBox_match_item__WiPhj"));

            int updatedCount = 0;
            int finishedCount = 0;

            for (Match liveMatch : liveMatches) {
                try {
                    // 웹에서 해당 경기 찾기
                    WebElement matchElement = findMatchElement(matchElements, liveMatch);

                    if (matchElement != null) {
                        // 점수 및 상태 업데이트
                        boolean updated = updateMatchScore(matchElement, liveMatch);

                        if (updated) {
                            updatedCount++;

                            // 경기가 종료되었는지 확인
                            if ("FINISHED".equals(liveMatch.getStatus())) {
                                finishedCount++;
                                log.info("🏁 경기 종료: {} {} - {} {}",
                                        liveMatch.getHomeTeam().getTeamName(),
                                        liveMatch.getHomeScore(),
                                        liveMatch.getAwayScore(),
                                        liveMatch.getAwayTeam().getTeamName());
                            }
                        }
                    }

                } catch (Exception e) {
                    log.warn("⚠️ 경기 업데이트 실패: {}", liveMatch.getMatchId(), e);
                }
            }

            if (updatedCount > 0) {
                log.info("✅ [실시간 업데이트] {}개 경기 업데이트 완료 (종료: {}개)", updatedCount, finishedCount);
            }

        } catch (Exception e) {
            log.error("❌ [실시간 업데이트] 실패", e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * 웹 페이지에서 DB의 경기와 일치하는 요소 찾기
     * NBA는 첫 번째 팀이 원정팀, 두 번째 팀이 홈팀
     */
    private WebElement findMatchElement(List<WebElement> matchElements, Match dbMatch) {
        String homeTeamName = dbMatch.getHomeTeam().getTeamName();
        String awayTeamName = dbMatch.getAwayTeam().getTeamName();

        for (WebElement matchElement : matchElements) {
            try {
                List<WebElement> teamItems = matchElement.findElements(By.cssSelector(".MatchBoxHeadToHeadArea_team_item__9ZknX"));

                if (teamItems.size() >= 2) {
                    // NBA는 첫 번째 팀이 원정팀, 두 번째 팀이 홈팀
                    String webAwayTeam = teamItems.get(0).findElement(By.cssSelector(".MatchBoxHeadToHeadArea_team__l2ZxP")).getText();
                    String webHomeTeam = teamItems.get(1).findElement(By.cssSelector(".MatchBoxHeadToHeadArea_team__l2ZxP")).getText();

                    // 팀 이름이 일치하는지 확인 (짧은 이름으로 비교)
                    if (isMatchingTeam(homeTeamName, webHomeTeam) && isMatchingTeam(awayTeamName, webAwayTeam)) {
                        return matchElement;
                    }
                }
            } catch (Exception e) {
                // 요소 찾기 실패시 다음 경기로
                continue;
            }
        }

        return null;
    }

    /**
     * 팀 이름 매칭 확인
     * DB의 전체 이름과 웹의 짧은 이름을 비교
     */
    private boolean isMatchingTeam(String dbTeamName, String webTeamName) {
        // 완전 일치
        if (dbTeamName.equals(webTeamName)) {
            return true;
        }

        // DB 이름이 웹 이름을 포함하는지 확인 (예: "보스턴 셀틱스" contains "보스턴")
        if (dbTeamName.contains(webTeamName)) {
            return true;
        }

        return false;
    }

    /**
     * 경기 점수 및 상태 업데이트
     * @return 업데이트 성공 여부
     */
    private boolean updateMatchScore(WebElement matchElement, Match match) {
        try {
            // 경기 상태 확인
            String statusText = matchElement.findElement(By.cssSelector(".MatchBox_status__xU6\\+d")).getText().strip();
            String newStatus = crawlerService.convertStatus(statusText);

            // 점수 추출
            List<WebElement> scores = matchElement.findElements(By.cssSelector(".MatchBoxHeadToHeadArea_score__TChmp"));

            if (scores.size() >= 2) {
                try {
                    // NBA는 첫 번째가 원정팀 점수, 두 번째가 홈팀 점수
                    String awayScoreText = scores.get(0).getText().trim();
                    String homeScoreText = scores.get(1).getText().trim();

                    if (homeScoreText.isEmpty() || awayScoreText.isEmpty()) {
                        log.warn("⚠️ 점수 텍스트가 비어있음");
                        return false;
                    }

                    Integer newHomeScore = Integer.parseInt(homeScoreText);
                    Integer newAwayScore = Integer.parseInt(awayScoreText);

                    // 점수나 상태가 변경되었는지 확인
                    Integer currentHomeScore = match.getHomeScore();
                    Integer currentAwayScore = match.getAwayScore();

                    boolean scoreChanged = (currentHomeScore == null || !currentHomeScore.equals(newHomeScore))
                            || (currentAwayScore == null || !currentAwayScore.equals(newAwayScore));
                    boolean statusChanged = !newStatus.equals(match.getStatus());

                    // ⚠️ 중요: FINISHED 경기는 상태를 변경하지 않음 (보호)
                    String currentStatus = match.getStatus();

                    if ("FINISHED".equals(currentStatus) && !"FINISHED".equals(newStatus)) {
                        // FINISHED 경기를 다른 상태로 변경하려는 시도 차단
                        log.warn("⚠️ FINISHED 경기 보호: {} vs {} (크롤링 상태: {} → 무시)",
                                match.getHomeTeam().getTeamName(),
                                match.getAwayTeam().getTeamName(),
                                newStatus);
                        return false;
                    }

                    if (scoreChanged || statusChanged) {
                        // 점수 및 상태 업데이트
                        match.setHomeScore(newHomeScore);
                        match.setAwayScore(newAwayScore);
                        match.setStatus(newStatus);
                        match.setUpdatedAt(LocalDateTime.now());

                        matchRepository.save(match);

                        log.info("🔄 점수 업데이트: {} {} - {} {} (상태: {})",
                                match.getHomeTeam().getTeamName(),
                                newHomeScore,
                                newAwayScore,
                                match.getAwayTeam().getTeamName(),
                                newStatus);

                        return true;
                    }

                } catch (NumberFormatException e) {
                    log.warn("⚠️ 점수 파싱 실패: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.warn("⚠️ 점수 업데이트 실패", e);
        }

        return false;
    }

    /**
     * 경기 시작 시간 기준으로 SCHEDULED -> LIVE 상태 변경
     * 5분마다 실행하여 경기 시작 확인
     * NBA 리그만 처리
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 60000)
    @Transactional
    public void checkMatchStartTime() {
        LocalDateTime now = LocalDateTime.now();

        // SCHEDULED 상태이면서 NBA 리그(league_id = 2)인 경기 조회
        List<Match> scheduledMatches = matchRepository.findByStatus("SCHEDULED");

        int updatedCount = 0;

        for (Match match : scheduledMatches) {
            // NBA 경기만 처리
            if (match.getLeague().getLeagueId().equals(2L)) {
                // 경기 시작 시간이 현재 시간보다 이전이면 LIVE로 변경
                if (match.getMatchDate().isBefore(now)) {
                    match.setStatus("LIVE");
                    matchRepository.save(match);
                    updatedCount++;

                    log.info("🟢 경기 시작: {} vs {} ({})",
                            match.getHomeTeam().getTeamName(),
                            match.getAwayTeam().getTeamName(),
                            match.getMatchDate());
                }
            }
        }

        if (updatedCount > 0) {
            log.info("✅ {}개 NBA 경기가 LIVE 상태로 변경됨", updatedCount);
        }
    }
}
